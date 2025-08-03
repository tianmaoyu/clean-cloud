package org.clean.inventory.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;

/**
 * 物料批次/序列号表
 * @TableName batch
 */
@Data
public class Batch {

    @TableId(type = IdType.AUTO)
    private Long batchId;

    /**
     * 
     */
    private Long materialId;

    /**
     * 批次号或序列号，根据物料设置决定
     */
    private String batchCode;

    /**
     * 
     */
    private Date productionDate;

    /**
     * 
     */
    private Date expirationDate;

    /**
     * 
     */
    private Long supplierId;

    /**
     * 
     */
    private String supplierBatch;

    /**
     * 
     */
    private String qualityStatus;

    /**
     * 
     */
    private String notes;

    /**
     * 
     */
    private OffsetDateTime createdAt;

    /**
     * 皮属性扩展字段
     */
    private String attributeJson;
}