package org.clean.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.clean.system.enums.MessageStatus;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@TableName("transaction_message")
public class TransactionMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private String businessId;
    
    private String content;
    
    private String mqTopic;
    private String mqTag;
    
    private MessageStatus status;
    
    private Integer retryCount;
    
    private Date createdTime;
    
    private Date updatedTime;
    

}