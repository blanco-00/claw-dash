package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("agents")
public class Agent extends BaseEntity {

    private String agentId;
    private String name;
    private String role;
    private String description;
    private String config;
    private String status;
    private LocalDateTime lastActiveAt;

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getLastActiveAt() {
        return lastActiveAt;
    }

    public void setLastActiveAt(LocalDateTime lastActiveAt) {
        this.lastActiveAt = lastActiveAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Agent agent = (Agent) o;
        return Objects.equals(agentId, agent.agentId) &&
                Objects.equals(name, agent.name) &&
                Objects.equals(role, agent.role) &&
                Objects.equals(description, agent.description) &&
                Objects.equals(config, agent.config) &&
                Objects.equals(status, agent.status) &&
                Objects.equals(lastActiveAt, agent.lastActiveAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), agentId, name, role, description, config, status, lastActiveAt);
    }
}
