package org.clean.test.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName test_coverage
 */
@Data
@TableName("test_coverage")
public class TestCoverage {
    /**
     * 
     */
    private Integer id;

    /**
     * 
     */
    private String projectName;

    /**
     * 
     */
    private String testClass;

    /**
     * 
     */
    private String testMethod;

    /**
     * 
     */
    private Float lineCoverage;

    /**
     * 
     */
    private LocalDateTime executionTime;
}