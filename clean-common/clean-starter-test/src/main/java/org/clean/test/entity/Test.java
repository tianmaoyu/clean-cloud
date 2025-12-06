package org.clean.test.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("test")
public class Test {

    private Integer id;
    private String versionId;
    private String packagename;
    private String className;
    private String methodName;
    private String authorName;
    private String docUrl;
    private String executionTime;
    private String lineCoverage;
    private String branchCoverage;
    private String instructionCoverage;
    private String complexityCoverage;
    private String testResult;
    private String testDuration;
    private String testStatus;
}
