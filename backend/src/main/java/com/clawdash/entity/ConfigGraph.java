package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("config_graphs")
public class ConfigGraph extends BaseEntity {

    private String name;
    private String description;
    private Integer version;
    private LocalDateTime lastSyncedAt;
}
