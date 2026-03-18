package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("api_tokens")
public class ApiToken extends BaseEntity {

    private String token;
    private String name;
    private String description;
    private String permissions;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private Boolean enabled;
}
