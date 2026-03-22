package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("edge_types")
public class EdgeType {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String value;

    private String name;

    private String description;

    private String defaultMessageTemplate;

    private LocalDateTime createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
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

    public String getDefaultMessageTemplate() {
        return defaultMessageTemplate;
    }

    public void setDefaultMessageTemplate(String defaultMessageTemplate) {
        this.defaultMessageTemplate = defaultMessageTemplate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeType edgeType = (EdgeType) o;
        return Objects.equals(id, edgeType.id) &&
                Objects.equals(value, edgeType.value) &&
                Objects.equals(name, edgeType.name) &&
                Objects.equals(description, edgeType.description) &&
                Objects.equals(defaultMessageTemplate, edgeType.defaultMessageTemplate) &&
                Objects.equals(createdAt, edgeType.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, value, name, description, createdAt);
    }
}
