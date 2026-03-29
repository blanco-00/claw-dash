package com.clawdash.service;

import com.clawdash.dto.AggregatedReport;
import com.clawdash.dto.SubtaskCompletionNotification;
import com.clawdash.entity.Task;
import com.clawdash.entity.TaskGroup;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AggregationService {

    private static final Logger log = LoggerFactory.getLogger(AggregationService.class);

    @Autowired
    private TaskService taskService;

    @Autowired
    private TaskGroupService taskGroupService;

    @Autowired
    private A2AMessageService a2AMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String MENXIASHENG_AGENT_ID = "menxiasheng";

    public AggregatedReport generateReport(String taskGroupId) {
        TaskGroup group = taskGroupService.lambdaQuery()
                .eq(TaskGroup::getId, Long.parseLong(taskGroupId))
                .one();
        
        if (group == null) {
            log.warn("Task group not found: {}", taskGroupId);
            return null;
        }

        List<Task> subtasks = taskService.lambdaQuery()
                .eq(Task::getType, "subtask")
                .list();

        AggregatedReport report = new AggregatedReport();
        report.setTaskGroupId(taskGroupId);
        report.setTotalGoal(group.getName());
        report.setOverallDesign(group.getDescription());
        report.setGeneratedAt(LocalDateTime.now());

        List<AggregatedReport.SubtaskResult> completed = subtasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .map(this::toSubtaskResult)
                .collect(Collectors.toList());

        List<AggregatedReport.SubtaskResult> failed = subtasks.stream()
                .filter(t -> "FAILED".equals(t.getStatus()) || "DEAD".equals(t.getStatus()))
                .map(this::toSubtaskResult)
                .collect(Collectors.toList());

        report.setCompletedSubtasks(completed);
        report.setFailedSubtasks(failed);

        String status = determineStatus(subtasks);
        report.setStatus(status);

        String summary = generateSummary(group, completed, failed);
        report.setSummary(summary);

        if (!failed.isEmpty()) {
            report.setRecommendations(generateRecommendations(failed));
        }

        return report;
    }

    public AggregatedReport processCompletionNotification(SubtaskCompletionNotification notification) {
        if (notification.getTaskGroupId() == null) {
            log.debug("Notification without task group ID, skipping aggregation");
            return null;
        }

        boolean isComplete = isGroupComplete(notification.getTaskGroupId());
        
        if (isComplete) {
            log.info("Task group {} is complete, generating final report", notification.getTaskGroupId());
            AggregatedReport report = generateReport(notification.getTaskGroupId());
            
            if (report != null) {
                notifyUser(report);
                updateGroupStatus(notification.getTaskGroupId(), report.getStatus());
            }
            
            return report;
        }

        return null;
    }

    public Map<String, Long> getGroupProgress(String taskGroupId) {
        List<Task> subtasks = taskService.lambdaQuery()
                .eq(Task::getType, "subtask")
                .list();

        long total = subtasks.size();
        long completed = subtasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        long failed = subtasks.stream()
                .filter(t -> "FAILED".equals(t.getStatus()) || "DEAD".equals(t.getStatus()))
                .count();
        long pending = subtasks.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .count();
        long running = subtasks.stream()
                .filter(t -> "RUNNING".equals(t.getStatus()))
                .count();

        return Map.of(
                "total", total,
                "completed", completed,
                "failed", failed,
                "pending", pending,
                "running", running
        );
    }

    public boolean isGroupComplete(String taskGroupId) {
        Map<String, Long> progress = getGroupProgress(taskGroupId);
        long total = progress.get("total");
        long completed = progress.get("completed");
        long failed = progress.get("failed");

        return total > 0 && (completed + failed) == total;
    }

    public boolean hasFailures(String taskGroupId) {
        Map<String, Long> progress = getGroupProgress(taskGroupId);
        return progress.get("failed") > 0;
    }

    protected String determineStatus(List<Task> subtasks) {
        if (subtasks.isEmpty()) {
            return "pending";
        }

        long completed = subtasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        long failed = subtasks.stream()
                .filter(t -> "FAILED".equals(t.getStatus()) || "DEAD".equals(t.getStatus()))
                .count();
        long total = subtasks.size();

        if (completed == total) {
            return "success";
        } else if (failed == total) {
            return "failed";
        } else if (completed + failed == total) {
            return "partial";
        }

        return "in_progress";
    }

    protected String generateSummary(TaskGroup group, 
                                     List<AggregatedReport.SubtaskResult> completed,
                                     List<AggregatedReport.SubtaskResult> failed) {
        StringBuilder sb = new StringBuilder();
        
        if (failed.isEmpty()) {
            sb.append("✅ 任务完成：").append(group.getName()).append("\n\n");
            sb.append("执行步骤：\n");
            for (int i = 0; i < completed.size(); i++) {
                AggregatedReport.SubtaskResult result = completed.get(i);
                sb.append((i + 1)).append(". ✅ ")
                  .append(result.getTitle())
                  .append(" (").append(result.getAssignedAgent()).append(")");
                if (result.getResult() != null && !result.getResult().isEmpty()) {
                    sb.append(" - ").append(truncate(result.getResult(), 100));
                }
                sb.append("\n");
            }
        } else {
            sb.append("⚠️ 任务部分完成：").append(group.getName()).append("\n\n");
            
            if (!completed.isEmpty()) {
                sb.append("成功的步骤：\n");
                for (AggregatedReport.SubtaskResult result : completed) {
                    sb.append("✅ ").append(result.getTitle())
                      .append(" (").append(result.getAssignedAgent()).append(")\n");
                }
                sb.append("\n");
            }
            
            sb.append("失败的步骤：\n");
            for (AggregatedReport.SubtaskResult result : failed) {
                sb.append("❌ ").append(result.getTitle())
                  .append(" (").append(result.getAssignedAgent()).append(")");
                if (result.getError() != null && !result.getError().isEmpty()) {
                    sb.append(" - ").append(truncate(result.getError(), 100));
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    protected String generateRecommendations(List<AggregatedReport.SubtaskResult> failed) {
        StringBuilder sb = new StringBuilder();
        sb.append("建议：\n");
        
        for (AggregatedReport.SubtaskResult result : failed) {
            sb.append("1. 检查 ").append(result.getAssignedAgent())
              .append(" 的 ").append(result.getTitle()).append(" 任务\n");
            
            if (result.getError() != null) {
                sb.append("   错误信息：").append(truncate(result.getError(), 200)).append("\n");
            }
            
            sb.append("   可以尝试重新指派给其他agent\n");
        }

        return sb.toString();
    }

    protected AggregatedReport.SubtaskResult toSubtaskResult(Task task) {
        AggregatedReport.SubtaskResult result = new AggregatedReport.SubtaskResult();
        result.setTaskId(task.getTaskId());
        result.setStatus(task.getStatus());
        result.setResult(task.getResult());
        result.setError(task.getError());
        result.setStartedAt(task.getStartedAt());
        result.setCompletedAt(task.getCompletedAt());

        try {
            if (task.getPayload() != null) {
                Map<String, Object> payload = objectMapper.readValue(task.getPayload(), Map.class);
                result.setTitle((String) payload.getOrDefault("title", task.getTaskId()));
                result.setAssignedAgent((String) payload.get("assignedAgent"));
            }
        } catch (JsonProcessingException e) {
            result.setTitle(task.getTaskId());
        }

        if (task.getStartedAt() != null && task.getCompletedAt() != null) {
            result.setDurationSeconds(Duration.between(task.getStartedAt(), task.getCompletedAt()).getSeconds());
        }

        return result;
    }

    protected void notifyUser(AggregatedReport report) {
        try {
            String content = objectMapper.writeValueAsString(Map.of(
                    "type", "TASK_GROUP_COMPLETED",
                    "taskGroupId", report.getTaskGroupId(),
                    "status", report.getStatus(),
                    "summary", report.getSummary()
            ));
            
            a2AMessageService.logMessage(
                    MENXIASHENG_AGENT_ID,
                    "user",
                    content,
                    "TASK_GROUP_COMPLETED"
            );
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize report notification: {}", e.getMessage());
        }
    }

    protected void updateGroupStatus(String taskGroupId, String status) {
        TaskGroup group = taskGroupService.lambdaQuery()
                .eq(TaskGroup::getId, Long.parseLong(taskGroupId))
                .one();
        
        if (group != null) {
            group.setStatus(status.toUpperCase());
            group.setUpdatedAt(LocalDateTime.now());
            
            if ("SUCCESS".equalsIgnoreCase(status) || "FAILED".equalsIgnoreCase(status) || "PARTIAL".equalsIgnoreCase(status)) {
                group.setUpdatedAt(LocalDateTime.now());
            }
            
            taskGroupService.updateById(group);
        }
    }

    private String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + "...";
    }
}
