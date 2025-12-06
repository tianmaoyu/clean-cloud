package org.clean.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TestMethodInfo {
    private String methodName;
    private String author;
    private String authorDate;
    private String status;
    private Throwable failure;
    private LocalDateTime executionTime;
    
    public TestMethodInfo(String methodName, String status, Throwable failure, LocalDateTime executionTime) {
        this.methodName = methodName;
        this.status = status;
        this.failure = failure;
        this.executionTime = executionTime;
    }
}