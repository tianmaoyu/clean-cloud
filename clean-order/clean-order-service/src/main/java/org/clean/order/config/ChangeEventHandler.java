package org.clean.order.config;

import cn.hutool.json.JSON;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.debezium.engine.ChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ChangeEventHandler {

    private static final Logger logger = LoggerFactory.getLogger(ChangeEventHandler.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    // 用于幂等处理的LSN缓存
    private final Set<String> processedLsn = ConcurrentHashMap.newKeySet();

    public void handleEvent(ChangeEvent<String, String> event) {

        logger.info(" event: {}", event.toString());

        String key = event.key();
        String value = event.value();
        String destination = event.destination();

        // 添加JSON解析
        try {
            JsonNode eventJson = objectMapper.readTree(value);
            logger.info("解析后的JSON: {}", eventJson);
            // 提取核心字段
            JsonNode payload = eventJson.path("payload");
            String operation = payload.path("op").asText();
            String lsn = payload.path("source").path("lsn").asText();
            String table = payload.path("source").path("table").asText();
            
            // 提取新旧数据
            JsonNode before = payload.path("before");
            JsonNode after = payload.path("after");
            
            logger.info("操作类型: {}, LSN: {}, 表名: {}", operation, lsn, table);
            logger.info("更新前数据: {}", before);
            logger.info("更新后数据: {}", after);

        } catch (Exception e) {
            logger.error("JSON解析失败: {}", value, e);
        }
    }

    private void handleCreate(JsonNode data, String table) {
        logger.info("Create event for table {}: {}", table, data);
    }

    private void handleUpdate(JsonNode before, JsonNode after, String table) {
        // 实际项目中实现具体更新逻辑
        logger.info("Update event for table {}: \nBefore: {}\nAfter: {}",
                table, before, after);
    }

    private void handleDelete(JsonNode data, String table) {
        logger.info("Delete event for table {}: {}", table, data);
    }

    private void handleSnapshot(JsonNode data, String table) {
        logger.info("Snapshot event for table {}: {}", table, data);
    }


}