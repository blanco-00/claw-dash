package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("a2a_messages")
public class A2AMessage extends BaseEntity {

    private String taskId;
    private String sessionId;
    private String fromAgentId;
    private String toAgentId;
    private String messageType;
    private String content;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;
    private Integer latencyMs;
    private String status;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getFromAgentId() {
        return fromAgentId;
    }

    public void setFromAgentId(String fromAgentId) {
        this.fromAgentId = fromAgentId;
    }

    public String getToAgentId() {
        return toAgentId;
    }

    public void setToAgentId(String toAgentId) {
        this.toAgentId = toAgentId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }

    public Integer getLatencyMs() {
        return latencyMs;
    }

    public void setLatencyMs(Integer latencyMs) {
        this.latencyMs = latencyMs;
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
        A2AMessage that = (A2AMessage) o;
        return Objects.equals(taskId, that.taskId) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(fromAgentId, that.fromAgentId) &&
                Objects.equals(toAgentId, that.toAgentId) &&
                Objects.equals(messageType, that.messageType) &&
                Objects.equals(content, that.content) &&
                Objects.equals(sentAt, that.sentAt) &&
                Objects.equals(deliveredAt, that.deliveredAt) &&
                Objects.equals(latencyMs, that.latencyMs) &&
                Objects.equals(status, that.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), taskId, sessionId, fromAgentId, toAgentId, messageType, content, sentAt, deliveredAt, latencyMs, status);
    }
}
