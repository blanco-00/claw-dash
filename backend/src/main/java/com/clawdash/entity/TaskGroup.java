package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.util.Objects;

@TableName("task_groups")
public class TaskGroup extends BaseEntity {

    private String groupId;
    private String name;
    private String description;
    private String taskIds;
    private String status;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
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

    public String getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(String taskIds) {
        this.taskIds = taskIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        TaskGroup taskGroup = (TaskGroup) o;
        return Objects.equals(groupId, taskGroup.groupId) &&
                Objects.equals(name, taskGroup.name) &&
                Objects.equals(description, taskGroup.description) &&
                Objects.equals(taskIds, taskGroup.taskIds) &&
                Objects.equals(status, taskGroup.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), groupId, name, description, taskIds, status);
    }
}
