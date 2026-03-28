package com.clawdash.dto;

import com.clawdash.entity.TaskGroup;

public class TaskGroupSummaryResponse {

    private Long id;
    private String name;
    private String description;
    private String status;
    private String totalGoal;
    private String overallDesign;
    private String decomposerAgentId;
    private java.time.LocalDateTime completedAt;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime updatedAt;
    
    private int totalTasks;
    private int completedTasks;
    private int failedTasks;
    private int runningTasks;
    private int pendingTasks;

    public TaskGroupSummaryResponse() {
    }

    public static TaskGroupSummaryResponse from(TaskGroup group, int total, int completed, int failed, int running, int pending) {
        TaskGroupSummaryResponse response = new TaskGroupSummaryResponse();
        response.setId(group.getId());
        response.setName(group.getName());
        response.setDescription(group.getDescription());
        response.setStatus(group.getStatus());
        response.setTotalGoal(group.getTotalGoal());
        response.setOverallDesign(group.getOverallDesign());
        response.setDecomposerAgentId(group.getDecomposerAgentId());
        response.setCompletedAt(group.getCompletedAt());
        response.setCreatedAt(group.getCreatedAt());
        response.setUpdatedAt(group.getUpdatedAt());
        response.setTotalTasks(total);
        response.setCompletedTasks(completed);
        response.setFailedTasks(failed);
        response.setRunningTasks(running);
        response.setPendingTasks(pending);
        return response;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTotalGoal() { return totalGoal; }
    public void setTotalGoal(String totalGoal) { this.totalGoal = totalGoal; }

    public String getOverallDesign() { return overallDesign; }
    public void setOverallDesign(String overallDesign) { this.overallDesign = overallDesign; }

    public String getDecomposerAgentId() { return decomposerAgentId; }
    public void setDecomposerAgentId(String decomposerAgentId) { this.decomposerAgentId = decomposerAgentId; }

    public java.time.LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(java.time.LocalDateTime completedAt) { this.completedAt = completedAt; }

    public java.time.LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(java.time.LocalDateTime createdAt) { this.createdAt = createdAt; }

    public java.time.LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(java.time.LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public int getTotalTasks() { return totalTasks; }
    public void setTotalTasks(int totalTasks) { this.totalTasks = totalTasks; }

    public int getCompletedTasks() { return completedTasks; }
    public void setCompletedTasks(int completedTasks) { this.completedTasks = completedTasks; }

    public int getFailedTasks() { return failedTasks; }
    public void setFailedTasks(int failedTasks) { this.failedTasks = failedTasks; }

    public int getRunningTasks() { return runningTasks; }
    public void setRunningTasks(int runningTasks) { this.runningTasks = runningTasks; }

    public int getPendingTasks() { return pendingTasks; }
    public void setPendingTasks(int pendingTasks) { this.pendingTasks = pendingTasks; }
}
