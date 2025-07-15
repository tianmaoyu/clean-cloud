package org.clean.order.config;

import cn.hutool.core.io.FileUtil;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.format.Json;
import lombok.SneakyThrows;
import org.apache.kafka.connect.storage.FileOffsetBackingStore;
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

    private final DebeziumProperties debeziumProperties;
    private final ChangeEventHandler changeEventHandler;
    
    private DebeziumEngine<ChangeEvent<String, String>> engine;
    
    @Autowired
    public DebeziumConfig(DebeziumProperties debeziumProperties, 
                         ChangeEventHandler changeEventHandler) {
        this.debeziumProperties = debeziumProperties;
        this.changeEventHandler = changeEventHandler;
    }


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
        props.setProperty("name", debeziumProperties.getConnectorName());
        props.setProperty("connector.class", "io.debezium.connector.postgresql.PostgresConnector");
//        props.setProperty("offset.storage", "io.debezium.storage.file.history.FileSchemaHistory");
        props.setProperty("offset.storage.file.filename", debeziumProperties.getOffsetFile());
        // 使用内存偏移量存储（替代Kafka的FileOffsetBackingStore）
        props.setProperty("offset.storage",  FileOffsetBackingStore.class.getCanonicalName());

        this.createFile(debeziumProperties.getOffsetFile());
        this.createFile(debeziumProperties.getHistoryFile());

        props.setProperty("offset.flush.interval.ms", "60000");

        // PostgreSQL 连接配置
        props.setProperty("database.hostname", debeziumProperties.getHost());
        props.setProperty("database.port", String.valueOf(debeziumProperties.getPort()));
        props.setProperty("database.user", debeziumProperties.getUser());
        props.setProperty("database.password", debeziumProperties.getPassword());
        props.setProperty("database.dbname", debeziumProperties.getDatabase());
        props.setProperty("database.server.name", debeziumProperties.getServerName());
        props.setProperty("database.history", "io.debezium.relational.history.FileDatabaseHistory");
        props.setProperty("database.history.file.filename", debeziumProperties.getHistoryFile());

        // 表过滤配置
        props.setProperty("table.include.list", "public.local_category,public.test");
        props.setProperty("plugin.name", "pgoutput");
        props.setProperty("slot.name", debeziumProperties.getSlotName());
        props.setProperty("publication.name", debeziumProperties.getPublicationName());

        // 快照配置
        props.setProperty("snapshot.mode", debeziumProperties.getSnapshotMode());


        // 创建Debezium引擎
        engine = DebeziumEngine.create(Json.class)
            .using(props)
            .notifying(changeEventHandler::handleEvent)
            .build();
        
        // 在单独线程中启动引擎
        Executor executor = Executors.newSingleThreadExecutor();
        executor.execute(engine);
        
        return engine;
    }

    @PreDestroy
    public void stop() throws IOException {
        if (engine != null) {
            engine.close();
        }
    }
}