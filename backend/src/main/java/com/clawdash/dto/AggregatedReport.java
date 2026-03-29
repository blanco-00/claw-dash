package com.clawdash.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AggregatedReport implements Serializable {

    private String taskGroupId;
    private String status; // 'success', 'partial', 'failed'
    
    private String summary;
    private String totalGoal;
    private String overallDesign;
    
    private List<SubtaskResult> completedSubtasks = new ArrayList<>();
    private List<SubtaskResult> failedSubtasks = new ArrayList<>();
    
    private String recommendations;
    private LocalDateTime generatedAt;

    public String getTaskGroupId() {
        return taskGroupId;
    }

    public void setTaskGroupId(String taskGroupId) {
        this.taskGroupId = taskGroupId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
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

    public List<SubtaskResult> getCompletedSubtasks() {
        return completedSubtasks;
    }

    public void setCompletedSubtasks(List<SubtaskResult> completedSubtasks) {
        this.completedSubtasks = completedSubtasks;
    }

    public List<SubtaskResult> getFailedSubtasks() {
        return failedSubtasks;
    }

    public void setFailedSubtasks(List<SubtaskResult> failedSubtasks) {
        this.failedSubtasks = failedSubtasks;
    }

    public String getRecommendations() {
        return recommendations;
    }

    public void setRecommendations(String recommendations) {
        this.recommendations = recommendations;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }

    public static class SubtaskResult implements Serializable {
        private String taskId;
        private String title;
        private String assignedAgent;
        private String status;
        private String result;
        private String error;
        private LocalDateTime startedAt;
        private LocalDateTime completedAt;
        private Long durationSeconds;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getAssignedAgent() {
            return assignedAgent;
        }

        public void setAssignedAgent(String assignedAgent) {
            this.assignedAgent = assignedAgent;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

        public Long getDurationSeconds() {
            return durationSeconds;
        }

        public void setDurationSeconds(Long durationSeconds) {
            this.durationSeconds = durationSeconds;
        }
    }
}
