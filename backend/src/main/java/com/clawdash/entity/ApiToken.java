package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("api_tokens")
public class ApiToken extends BaseEntity {

    private String token;
    private String name;
    private String description;
    private String permissions;
    private LocalDateTime expiresAt;
    private LocalDateTime lastUsedAt;
    private Boolean enabled;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPermissions() {
        return permissions;
    }

    public void setPermissions(String permissions) {
        this.permissions = permissions;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ApiToken apiToken = (ApiToken) o;
        return Objects.equals(token, apiToken.token) &&
                Objects.equals(name, apiToken.name) &&
                Objects.equals(description, apiToken.description) &&
                Objects.equals(permissions, apiToken.permissions) &&
                Objects.equals(expiresAt, apiToken.expiresAt) &&
                Objects.equals(lastUsedAt, apiToken.lastUsedAt) &&
                Objects.equals(enabled, apiToken.enabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), token, name, description, permissions, expiresAt, lastUsedAt, enabled);
    }
}
