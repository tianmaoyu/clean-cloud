package org.clean.inventory.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@TableName("product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String code;
    private String name;
    private Long categoryId;
    private Long brandId;
    private Double price;
    private Double cost;
    private Integer stockQuantity;
    private Double weight;
    private Boolean activeFlag;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private String description;
    private String imageUrl;
    private String tags;
    private String attributes;
}