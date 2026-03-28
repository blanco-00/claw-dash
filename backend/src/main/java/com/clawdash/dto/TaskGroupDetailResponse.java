package com.clawdash.dto;

import com.clawdash.entity.TaskGroup;
import com.clawdash.entity.TaskQueueTask;

import java.time.LocalDateTime;
import java.util.List;

public class TaskGroupDetailResponse {

    private Long id;
    private String name;
    private String description;
    private String status;
    private String totalGoal;
    private String overallDesign;
    private String decomposerAgentId;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    private List<TaskQueueTask> tasks;
    private int totalTasks;
    private int completedTasks;
    private int failedTasks;
    private int runningTasks;
    private int pendingTasks;

    public TaskGroupDetailResponse() {
    }

    public static TaskGroupDetailResponse from(TaskGroup group, List<TaskQueueTask> tasks) {
        TaskGroupDetailResponse response = new TaskGroupDetailResponse();
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
        response.setTasks(tasks);
        
        int total = tasks.size();
        int completed = 0;
        int failed = 0;
        int running = 0;
        int pending = 0;
        
        for (TaskQueueTask task : tasks) {
            String status = task.getStatus();
            if ("COMPLETED".equals(status)) {
                completed++;
            } else if ("FAILED".equals(status) || "DEAD".equals(status)) {
                failed++;
            } else if ("RUNNING".equals(status)) {
                running++;
            } else if ("PENDING".equals(status)) {
                pending++;
            }
        }
        
        response.setTotalTasks(total);
        response.setCompletedTasks(completed);
        response.setFailedTasks(failed);
        response.setRunningTasks(running);
        response.setPendingTasks(pending);
        
        return response;
    }

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

    public List<TaskQueueTask> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskQueueTask> tasks) {
        this.tasks = tasks;
    }

    public int getTotalTasks() {
        return totalTasks;
    }

    public void setTotalTasks(int totalTasks) {
        this.totalTasks = totalTasks;
    }

    public int getCompletedTasks() {
        return completedTasks;
    }

    public void setCompletedTasks(int completedTasks) {
        this.completedTasks = completedTasks;
    }

    public int getFailedTasks() {
        return failedTasks;
    }

    public void setFailedTasks(int failedTasks) {
        this.failedTasks = failedTasks;
    }

    public int getRunningTasks() {
        return runningTasks;
    }

    public void setRunningTasks(int runningTasks) {
        this.runningTasks = runningTasks;
    }

    public int getPendingTasks() {
        return pendingTasks;
    }

    public void setPendingTasks(int pendingTasks) {
        this.pendingTasks = pendingTasks;
    }
}
