package org.clean.example.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SequenceGenerator {
    private Long id;
    
    private String bizType;
    
    private Long currentValue = 0L;
    
    private Integer stepSize = 100;
    
    private Integer minCacheSize = 50;
    
    private String description;
    
    private Long version = 0L;
    
    private LocalDateTime createdTime;
    
    private LocalDateTime updatedTime;

}