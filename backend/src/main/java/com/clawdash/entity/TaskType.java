package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("task_type")
public class TaskType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String displayName;

    private String description;

    private Boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskType taskType = (TaskType) o;
        return Objects.equals(id, taskType.id) &&
                Objects.equals(name, taskType.name) &&
                Objects.equals(displayName, taskType.displayName) &&
                Objects.equals(description, taskType.description) &&
                Objects.equals(enabled, taskType.enabled) &&
                Objects.equals(createdAt, taskType.createdAt) &&
                Objects.equals(updatedAt, taskType.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, displayName, description, enabled, createdAt, updatedAt);
    }
}
