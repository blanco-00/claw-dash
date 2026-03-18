package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tasks")
public class Task extends BaseEntity {

    private String taskId;
    private String type;
    private String payload;
    private Integer priority;
    private String status;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime claimedAt;
    private String claimedBy;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime scheduledAt;
    private String result;
    private String error;
    private String dependsOn;
    private Integer orderIndex;
    private String sourceChannel;
    private String sourceConversation;
}
