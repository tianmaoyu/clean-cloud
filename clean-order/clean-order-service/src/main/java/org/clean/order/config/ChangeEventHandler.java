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
        try {
//            JsonNode eventValue = objectMapper.readTree(event.value());
//            String prettyJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(event);
            logger.info(" event: {}", event.toString());
            
            // 获取唯一标识 (LSN for PostgreSQL)
//            String lsn = eventValue.path("source").path("lsn").asText();
//            String operation = eventValue.path("op").asText();
//            String table = eventValue.path("source").path("table").asText();
            
//            // 幂等性检查
//            if (processedLsn.contains(lsn)) {
//                logger.debug("Skipping duplicate event with LSN: {}", lsn);
//                return;
//            }
            
//            // 处理不同操作类型
//            switch (operation) {
//                case "c": // Create
//
//                    break;
//                case "u": // Update
//
//                    break;
//                case "d": // Delete
//
//                    break;
//                case "r": // Read (snapshot)
//
//                    break;
//                default:
////                    logger.warn("Unknown operation type: {}", operation);
//            }
            
//            // 记录已处理的LSN
//            processedLsn.add(lsn);
//
//            // 定期清理缓存 (实际项目中可使用定时任务)
//            if (processedLsn.size() > 10000) {
//                processedLsn.clear();
//            }
            
        } catch (Exception e) {
            logger.error("Error processing change event: {}", event.value(), e);
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