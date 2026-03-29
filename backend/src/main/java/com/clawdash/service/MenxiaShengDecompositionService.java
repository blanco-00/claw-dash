package com.clawdash.service;

import com.clawdash.dto.DecomposedTask;
import com.clawdash.dto.ParsedTask;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MenxiaShengDecompositionService {

    @Autowired
    private AgentSelector agentSelector;

    @Autowired
    private ObjectMapper objectMapper;

    public boolean isComplexTask(ParsedTask task) {
        if (task.getExplicitDecomposeRequested()) {
            return true;
        }

        String title = task.getTitle();
        String description = task.getDescription();
        String combined = (title + " " + description).toLowerCase();

        int goalIndicators = 0;
        String[] multiGoalKeywords = {"并且", "同时", "还要", "以及", "另外", "还要做", "还需要"};
        for (String keyword : multiGoalKeywords) {
            if (combined.contains(keyword)) {
                goalIndicators++;
            }
        }

        if (combined.contains("流程") || combined.contains("系统") || combined.contains("完整")) {
            goalIndicators++;
        }

        int commaCount = countOccurrences(combined, "，");
        int periodCount = countOccurrences(combined, "。");
        if (commaCount >= 2 || periodCount >= 2) {
            goalIndicators++;
        }

        return goalIndicators >= 2;
    }

    public DecomposedTask decompose(ParsedTask task) {
        DecomposedTask result = new DecomposedTask();
        result.setTotalGoal(task.getTitle());
        
        List<DecomposedTask.SubtaskDef> subtasks = generateSubtasks(task);
        result.setSubtasks(subtasks);
        
        String design = buildOverallDesign(subtasks);
        result.setOverallDesign(design);
        
        for (DecomposedTask.SubtaskDef subtask : subtasks) {
            DecomposedTask.SubtaskContext ctx = new DecomposedTask.SubtaskContext();
            ctx.setTotalGoal(result.getTotalGoal());
            ctx.setOverallDesign(design);
            ctx.setSubtaskDescription(subtask.getDescription());
            subtask.setContext(ctx);
        }

        return result;
    }

    protected List<DecomposedTask.SubtaskDef> generateSubtasks(ParsedTask task) {
        List<DecomposedTask.SubtaskDef> subtasks = new ArrayList<>();
        
        String title = task.getTitle();
        String description = task.getDescription();
        
        List<String> steps = analyzeSteps(title, description);
        
        for (int i = 0; i < steps.size(); i++) {
            DecomposedTask.SubtaskDef subtask = new DecomposedTask.SubtaskDef();
            subtask.setTitle(extractSubtaskTitle(steps.get(i), i));
            subtask.setDescription(steps.get(i));
            
            String agent = agentSelector.selectAgent(steps.get(i));
            subtask.setAssignedAgent(agent);
            
            List<String> dependsOn = new ArrayList<>();
            if (i > 0) {
                dependsOn.add("subtask-" + (i - 1));
            }
            subtask.setDependsOn(dependsOn);
            
            subtasks.add(subtask);
        }

        return subtasks;
    }

    protected List<String> analyzeSteps(String title, String description) {
        List<String> steps = new ArrayList<>();
        
        String combined = title + " " + (description != null ? description : "");
        
        String[] sentences = combined.split("[,，。]");
        for (String sentence : sentences) {
            String trimmed = sentence.trim();
            if (!trimmed.isEmpty()) {
                steps.add(trimmed);
            }
        }

        if (steps.isEmpty()) {
            steps.add(title);
        }

        return steps;
    }

    protected String extractSubtaskTitle(String step, int index) {
        int maxLen = 50;
        if (step.length() <= maxLen) {
            return step;
        }
        return step.substring(0, maxLen) + "...";
    }

    protected String buildOverallDesign(List<DecomposedTask.SubtaskDef> subtasks) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < subtasks.size(); i++) {
            DecomposedTask.SubtaskDef subtask = subtasks.get(i);
            sb.append((i + 1)).append(". [")
              .append(subtask.getAssignedAgent())
              .append("] ")
              .append(subtask.getTitle());
            if (i < subtasks.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private int countOccurrences(String str, String sub) {
        int count = 0;
        int idx = 0;
        while ((idx = str.indexOf(sub, idx)) != -1) {
            count++;
            idx += sub.length();
        }
        return count;
    }
}
