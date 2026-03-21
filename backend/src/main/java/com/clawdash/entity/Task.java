package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("tasks")
public class Task extends BaseEntity {

    private String taskId;
    private String type;
    private String payload;
    private Integer priority;
    private String status;
    private Integer retryCount;
    private Integer maxRetries;
    private LocalDateTime claimedAt;
    private String claimedBy;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private LocalDateTime scheduledAt;
    private String result;
    private String error;
    private String dependsOn;
    private Integer orderIndex;
    private String sourceChannel;
    private String sourceConversation;

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

    public LocalDateTime getClaimedAt() {
        return claimedAt;
    }

    public void setClaimedAt(LocalDateTime claimedAt) {
        this.claimedAt = claimedAt;
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

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public String getSourceChannel() {
        return sourceChannel;
    }

    public void setSourceChannel(String sourceChannel) {
        this.sourceChannel = sourceChannel;
    }

    public String getSourceConversation() {
        return sourceConversation;
    }

    public void setSourceConversation(String sourceConversation) {
        this.sourceConversation = sourceConversation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Task task = (Task) o;
        return Objects.equals(taskId, task.taskId) &&
                Objects.equals(type, task.type) &&
                Objects.equals(payload, task.payload) &&
                Objects.equals(priority, task.priority) &&
                Objects.equals(status, task.status) &&
                Objects.equals(retryCount, task.retryCount) &&
                Objects.equals(maxRetries, task.maxRetries) &&
                Objects.equals(claimedAt, task.claimedAt) &&
                Objects.equals(claimedBy, task.claimedBy) &&
                Objects.equals(startedAt, task.startedAt) &&
                Objects.equals(completedAt, task.completedAt) &&
                Objects.equals(scheduledAt, task.scheduledAt) &&
                Objects.equals(result, task.result) &&
                Objects.equals(error, task.error) &&
                Objects.equals(dependsOn, task.dependsOn) &&
                Objects.equals(orderIndex, task.orderIndex) &&
                Objects.equals(sourceChannel, task.sourceChannel) &&
                Objects.equals(sourceConversation, task.sourceConversation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), taskId, type, payload, priority, status, retryCount, maxRetries, claimedAt, claimedBy, startedAt, completedAt, scheduledAt, result, error, dependsOn, orderIndex, sourceChannel, sourceConversation);
    }
}
