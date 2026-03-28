package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("task_queue_tasks")
public class TaskQueueTask {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String taskId;

    private String type;

    @TableField("`payload`")
    private String payload;

    private Integer priority;

    private String status;

    private Integer retryCount;

    private Integer maxRetries;

    private String claimedBy;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private LocalDateTime scheduledAt;

    @TableField("`result`")
    private String result;

    private String error;

    private String dependsOn;

    private String taskGroupId;

    private String parentTaskId;

    private String assignedAgent;

    private String reportToAgent;

    private String context;

    private String title;

    private String lastError;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public String getClaimedBy() {
        return claimedBy;
    }

    public void setClaimedBy(String claimedBy) {
        this.claimedBy = claimedBy;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getScheduledAt() {
        return scheduledAt;
    }

    public void setScheduledAt(LocalDateTime scheduledAt) {
        this.scheduledAt = scheduledAt;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getDependsOn() {
        return dependsOn;
    }

    public void setDependsOn(String dependsOn) {
        this.dependsOn = dependsOn;
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

    public String getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(String taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getAssignedAgent() {
        return assignedAgent;
    }

    public void setAssignedAgent(String assignedAgent) {
        this.assignedAgent = assignedAgent;
    }

    public String getReportToAgent() {
        return reportToAgent;
    }

    public void setReportToAgent(String reportToAgent) {
        this.reportToAgent = reportToAgent;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLastError() {
        return lastError;
    }

    public void setLastError(String lastError) {
        this.lastError = lastError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskQueueTask that = (TaskQueueTask) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(taskId, that.taskId) &&
                Objects.equals(type, that.type) &&
                Objects.equals(payload, that.payload) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(status, that.status) &&
                Objects.equals(retryCount, that.retryCount) &&
                Objects.equals(maxRetries, that.maxRetries) &&
                Objects.equals(claimedBy, that.claimedBy) &&
                Objects.equals(startedAt, that.startedAt) &&
                Objects.equals(completedAt, that.completedAt) &&
                Objects.equals(scheduledAt, that.scheduledAt) &&
                Objects.equals(result, that.result) &&
                Objects.equals(error, that.error) &&
                Objects.equals(dependsOn, that.dependsOn) &&
                Objects.equals(taskGroupId, that.taskGroupId) &&
                Objects.equals(parentTaskId, that.parentTaskId) &&
                Objects.equals(assignedAgent, that.assignedAgent) &&
                Objects.equals(reportToAgent, that.reportToAgent) &&
                Objects.equals(context, that.context) &&
                Objects.equals(title, that.title) &&
                Objects.equals(lastError, that.lastError) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(updatedAt, that.updatedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, taskId, type, payload, priority, status, retryCount, maxRetries, 
                claimedBy, startedAt, completedAt, scheduledAt, result, error, dependsOn, 
                taskGroupId, parentTaskId, assignedAgent, reportToAgent, context, title, lastError, 
                createdAt, updatedAt);
    }
}
