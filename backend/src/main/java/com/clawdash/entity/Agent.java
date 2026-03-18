package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("agents")
public class Agent extends BaseEntity {

    private String agentId;
    private String name;
    private String role;
    private String description;
    private String config;
    private String status;
    private LocalDateTime lastActiveAt;
}
