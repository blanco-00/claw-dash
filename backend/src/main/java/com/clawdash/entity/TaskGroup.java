package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("task_groups")
public class TaskGroup extends BaseEntity {

    private String groupId;
    private String name;
    private String description;
    private String taskIds;
    private String status;
}
