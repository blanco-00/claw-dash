package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("a2a_messages")
public class A2AMessage extends BaseEntity {

    private String taskId;
    private String sessionId;
    private String fromAgentId;
    private String toAgentId;
    private String messageType;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private Integer latencyMs;
    private String status;
}
