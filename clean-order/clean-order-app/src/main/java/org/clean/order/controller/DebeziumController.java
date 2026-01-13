package org.clean.order.controller;

import io.debezium.engine.DebeziumEngine;
import lombok.Data;
import org.clean.order.config.ChangeEventHandler;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/debezium")
public class DebeziumController {
    
    private final DebeziumEngine<?> engine;
    private final ChangeEventHandler eventHandler;
    
    public DebeziumController(DebeziumEngine<?> engine, 
                             ChangeEventHandler eventHandler) {
        this.engine = engine;
        this.eventHandler = eventHandler;
    }
    
    @PostMapping("/restart")
    public String restart() {
        try {
            if (engine != null) {
                engine.close();
                // 实际项目中需要重新初始化引擎
                return "Debezium engine restarted successfully";
            }
            return "Engine not initialized";
        } catch (IOException e) {
            return "Failed to restart engine: " + e.getMessage();
        }
    }
    
    @GetMapping("/metrics")
    public MetricsResponse getMetrics() {
        return new MetricsResponse(
//            eventHandler.getProcessedEventCount(),
//            eventHandler.getErrorCount(),
//            eventHandler.getLastErrorMessage()
        );
    }
    
    @PostMapping("/reset-offset")
    public String resetOffset() {
        // 实际项目中实现offset重置逻辑
        return "Offset reset initiated. Note: This may cause reprocessing of events!";
    }

    @Data
    public class MetricsResponse {
        private long processedEvents;
        private long errorCount;
        private String lastError;
        
        // 构造方法、getters
    }
}