package org.clean.order.config;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.connector.postgresql.PostgresConnector;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import io.debezium.relational.history.FileDatabaseHistory;
import lombok.SneakyThrows;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
import org.apache.kafka.connect.storage.MemoryOffsetBackingStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableConfigurationProperties(DebeziumProperties.class)
public class DebeziumConfig {
    private static final Logger logger = LoggerFactory.getLogger(DebeziumConfig.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

//    private final DebeziumProperties debeziumProperties;
//    private final ChangeEventHandler changeEventHandler;
//
    private DebeziumEngine<ChangeEvent<String, String>> engine;
//
//    @Autowired
//    public DebeziumConfig(DebeziumProperties debeziumProperties,
//                         ChangeEventHandler changeEventHandler) {
//        this.debeziumProperties = debeziumProperties;
//        this.changeEventHandler = changeEventHandler;
//    }


    @SneakyThrows
    public void createFile(String filePath) {
        Path path = Paths.get(filePath);

        // 创建父目录（如果不存在）
        Files.createDirectories(path.getParent());

        // 如果文件不存在，则创建
        if (!Files.exists(path)) {
            Files.createFile(path);
            System.out.println("Offset 文件已创建: " + path.toAbsolutePath());
        } else {
            System.out.println("Offset 文件已存在: " + path.toAbsolutePath());
        }
    }

    @Bean
    public DebeziumEngine<ChangeEvent<String, String>> debeziumEngine() {

        // 创建Debezium配置
        Properties props = new Properties();
        //必填
        props.setProperty("name", "xx");
        props.setProperty("connector.class", PostgresConnector.class.getCanonicalName());
        props.setProperty("offset.storage.file.filename", "./debezium/offset.dat");
        props.setProperty("offset.storage",  FileOffsetBackingStore.class.getCanonicalName());

        this.createFile("./debezium/offset.dat");
        this.createFile("./debezium/history.dat");

        //5秒间隔记录偏移量,默认是5秒
        props.setProperty("offset.flush.interval.ms", "5000");

        // PostgreSQL 连接配置
        props.setProperty("database.hostname", "localhost");
        props.setProperty("database.port", "5432");
        props.setProperty("database.user", "postgres");
        props.setProperty("database.password", "123456");
        props.setProperty("database.dbname", "postgres");
        props.setProperty("database.server.name", "db1");
        
        // 验证用户权限配置
        props.setProperty("logical.decoding.mode", "logical");
        
        props.setProperty("database.history", FileDatabaseHistory.class.getCanonicalName());
        props.setProperty("database.history.file.filename", "./debezium/history.dat");

        // 表过滤配置
        props.setProperty("table.include.list", "public.local_category,public.test,public.newtable2,public.newtable_2,public.newtable_1,public.newtable_3,public.newtable_4");
        props.setProperty("plugin.name", "pgoutput");
        props.setProperty("slot.name", "mac_db_");
        props.setProperty("publication.name", "dbz_publication");

        // 快照配置 initial, never, when_needed
        props.setProperty("snapshot.mode", "never");


        // 创建Debezium引擎
        engine = DebeziumEngine.create(Json.class)
            .using(props)
            .notifying(this::handleEvent)
            .build();
        
        // 在单独线程中启动引擎
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
        
        return engine;
    }

    public void handleEvent(ChangeEvent<String, String> event) {

//        logger.info(" event: {}", event.toString());

        String key = event.key();// 可能是一个字段,可能是多个字段,主键,唯一key
        String value = event.value();
        String destination = event.destination();

        // 添加JSON解析
        try {
            if(value==null) return;

            JsonNode eventJson = objectMapper.readTree(value);
            // 提取核心字段
            JsonNode payload = eventJson.path("payload");
            String operation = payload.path("op").asText();
            // 事务ID
            String txId = payload.path("txId").asText();
            String lsn = payload.path("source").path("lsn").asText();
            String table = payload.path("source").path("table").asText();

            //极端情况去重复: 构建唯一事件ID (LSN + 事务ID + 操作类型)
            String eventId = String.format("%s_%s_%s", lsn, txId, operation);


            String[] split = destination.split("\\.");
            if(split.length!=3) return;
            String serverName = split[0];
            String schemaName = split[1];
            String tableName = split[2];

            // 提取新旧数据
            JsonNode before = payload.path("before");
            JsonNode after = payload.path("after");

            logger.info("操作类型: {}, LSN: {}, serverName:{},schema:{}, tableName: {}", operation, lsn,serverName, schemaName,tableName);
            logger.info("更新前数据: {}", before);
            logger.info("更新后数据: {}", after);

        } catch (Exception e) {
            logger.error("JSON解析失败: {}", value, e);
        }
    }

    @PreDestroy
    public void stop() throws IOException {
        if (engine != null) {
            engine.close();
        }
    }
}