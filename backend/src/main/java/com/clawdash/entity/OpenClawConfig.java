package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("openclaw_config")
public class OpenClawConfig extends BaseEntity {

    private String configKey;
    private String configValue;
    private String description;
}
