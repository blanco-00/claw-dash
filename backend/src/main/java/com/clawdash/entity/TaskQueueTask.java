package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("task_queue_tasks")
public class TaskQueueTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskId;

    private String type;

    @TableField("`payload`")
    private String payload;

    private Integer priority;

    private String status;

    private Integer retryCount;

    private Integer maxRetries;

    private String claimedBy;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime scheduledAt;

    @TableField("`result`")
    private String result;

    private String error;

    private String dependsOn;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
