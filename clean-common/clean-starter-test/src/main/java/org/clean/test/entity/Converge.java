package org.clean.test.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("coverage")
public class Converge {

    private Long id;
    private String versionId;
    private String projectId;
    private String className;
    private String methodName;

    private Integer lineCovered;
    private Integer lineMissed;
    private Double lineCoverageRatio;

    private Integer branchCovered;
    private Integer branchMissed;
    private Double branchCoverageRatio;

    private Integer instructionCovered;
    private Integer instructionMissed;
    private Double instructionCoverageRatio;

    private Integer complexity;
    private Integer complexityCovered;
    private Integer complexityMissed;
    private Double complexityCoverageRatio;

    private String authorName;
    private String docUrl;

    private Date createTime;
}
