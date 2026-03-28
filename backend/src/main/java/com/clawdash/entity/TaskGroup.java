package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("task_groups")
public class TaskGroup extends BaseEntity {

    private String name;
    private String description;
    private String status;

    // Fields for autonomous task queue (added in V4)
    private String totalGoal;
    private String overallDesign;
    private String decomposerAgentId;
    private LocalDateTime completedAt;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTotalGoal() {
        return totalGoal;
    }

    public void setTotalGoal(String totalGoal) {
        this.totalGoal = totalGoal;
    }

    public String getOverallDesign() {
        return overallDesign;
    }

    public void setOverallDesign(String overallDesign) {
        this.overallDesign = overallDesign;
    }

    public String getDecomposerAgentId() {
        return decomposerAgentId;
    }

    public void setDecomposerAgentId(String decomposerAgentId) {
        this.decomposerAgentId = decomposerAgentId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TaskGroup taskGroup = (TaskGroup) o;
        return Objects.equals(getId(), taskGroup.getId()) &&
                Objects.equals(name, taskGroup.name) &&
                Objects.equals(description, taskGroup.description) &&
                Objects.equals(status, taskGroup.status) &&
                Objects.equals(totalGoal, taskGroup.totalGoal) &&
                Objects.equals(overallDesign, taskGroup.overallDesign) &&
                Objects.equals(decomposerAgentId, taskGroup.decomposerAgentId) &&
                Objects.equals(completedAt, taskGroup.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, description, status, 
                totalGoal, overallDesign, decomposerAgentId, completedAt);
    }
}
