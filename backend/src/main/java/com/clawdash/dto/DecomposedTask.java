package com.clawdash.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DecomposedTask implements Serializable {

    private String totalGoal;
    private String overallDesign;
    private List<SubtaskDef> subtasks = new ArrayList<>();

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

    public List<SubtaskDef> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(List<SubtaskDef> subtasks) {
        this.subtasks = subtasks;
    }

    public static class SubtaskDef implements Serializable {
        private String title;
        private String description;
        private String assignedAgent;
        private List<String> dependsOn = new ArrayList<>();
        private SubtaskContext context;

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getAssignedAgent() {
            return assignedAgent;
        }

        public void setAssignedAgent(String assignedAgent) {
            this.assignedAgent = assignedAgent;
        }

        public List<String> getDependsOn() {
                return dependsOn;
        }

        public void setDependsOn(List<String> dependsOn) {
            this.dependsOn = dependsOn;
        }

        public SubtaskContext getContext() {
            return context;
        }

        public void setContext(SubtaskContext context) {
            this.context = context;
        }
    }

    public static class SubtaskContext implements Serializable {
        private String totalGoal;
        private String overallDesign;
        private String subtaskDescription;

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

        public String getSubtaskDescription() {
            return subtaskDescription;
        }

        public void setSubtaskDescription(String subtaskDescription) {
            this.subtaskDescription = subtaskDescription;
        }
    }
}
