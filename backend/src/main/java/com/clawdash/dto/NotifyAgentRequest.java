package com.clawdash.dto;

public class NotifyAgentRequest {

    private String agentId;
    private String taskGroupId;
    private String action; // "decompose" | "execute"
    private String webhookUrl;

    public NotifyAgentRequest() {
    }

    public NotifyAgentRequest(String agentId, String taskGroupId, String action, String webhookUrl) {
        this.agentId = agentId;
        this.taskGroupId = taskGroupId;
        this.action = action;
        this.webhookUrl = webhookUrl;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(String taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}
