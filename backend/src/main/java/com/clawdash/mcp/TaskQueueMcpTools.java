package com.clawdash.mcp;

import com.clawdash.common.PageResponse;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.entity.A2AMessage;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.service.A2AMessageService;
import com.clawdash.service.TaskQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springaicommunity.mcp.annotation.McpTool;
import org.springaicommunity.mcp.annotation.McpToolParam;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TaskQueueMcpTools {

    private static final Logger log = LoggerFactory.getLogger(TaskQueueMcpTools.class);
    private static final String MENXIASHENG_AGENT_ID = "menxiasheng";
    private static final String MESSAGE_TYPE_SUBTASK_COMPLETED = "SUBTASK_COMPLETED";

    private final TaskQueueService taskQueueService;
    private final A2AMessageService a2aMessageService;
    private final ObjectMapper objectMapper;

    public TaskQueueMcpTools(TaskQueueService taskQueueService, 
                             A2AMessageService a2aMessageService,
                             ObjectMapper objectMapper) {
        this.taskQueueService = taskQueueService;
        this.a2aMessageService = a2aMessageService;
        this.objectMapper = objectMapper;
    }

    @McpTool(name = "task_create", description = "Create a new task in the task queue")
    public Map<String, Object> createTask(
            @McpToolParam(description = "Task type", required = true) String type,
            @McpToolParam(description = "Task payload as JSON string") String payload,
            @McpToolParam(description = "Priority (1-10, higher is more urgent)") Integer priority,
            @McpToolParam(description = "Maximum retry attempts") Integer maxRetries,
            @McpToolParam(description = "Comma-separated list of task IDs this depends on") String dependsOn,
            @McpToolParam(description = "Scheduled time in ISO format") String scheduledAt) {
        
        CreateTaskRequest request = new CreateTaskRequest();
        request.setType(type);
        
        if (payload != null && !payload.isEmpty()) {
            request.setPayload(payload);
        }
        
        request.setPriority(priority);
        request.setMaxRetries(maxRetries);
        
        if (dependsOn != null && !dependsOn.isEmpty()) {
            request.setDependsOn(List.of(dependsOn.split(",")));
        }
        
        request.setScheduledAt(scheduledAt);
        
        TaskQueueTask task = taskQueueService.createTask(request);
        
        Map<String, Object> result = new HashMap<>();
        result.put("taskId", task.getTaskId());
        result.put("id", task.getId());
        result.put("type", task.getType());
        result.put("status", task.getStatus());
        result.put("priority", task.getPriority());
        result.put("createdAt", task.getCreatedAt().toString());
        
        return result;
    }

    @McpTool(name = "task_list", description = "List tasks from the task queue with optional filters")
    public Map<String, Object> listTasks(
            @McpToolParam(description = "Page number (1-based)") Integer page,
            @McpToolParam(description = "Page size") Integer size,
            @McpToolParam(description = "Filter by status (PENDING, RUNNING, COMPLETED, FAILED, DEAD)") String status,
            @McpToolParam(description = "Sort field (priority, createdAt)") String sortBy,
            @McpToolParam(description = "Sort ascending") Boolean ascending) {
        
        int pageNum = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        
        PageResponse<TaskQueueTask> response = taskQueueService.listTasks(
            pageNum, pageSize, status, sortBy, ascending != null ? ascending : false);
        
        Map<String, Object> result = new HashMap<>();
        result.put("tasks", response.getContent().stream().map(this::toMap).toList());
        result.put("total", response.getTotalElements());
        result.put("page", response.getNumber() + 1);
        result.put("size", response.getSize());
        result.put("pages", response.getTotalPages());
        result.put("hasNext", !response.isLast());
        result.put("hasPrev", !response.isFirst());
        
        return result;
    }

    @McpTool(name = "task_status", description = "Get the status of a specific task")
    public Map<String, Object> getTaskStatus(
            @McpToolParam(description = "Task ID (database ID)", required = false) Long id,
            @McpToolParam(description = "Task ID (string taskId)", required = false) String taskId) {
        
        TaskQueueTask task = null;
        
        if (id != null) {
            task = taskQueueService.getTaskById(id);
        } else if (taskId != null && !taskId.isEmpty()) {
            task = taskQueueService.getTaskByTaskId(taskId);
        }
        
        if (task == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Task not found");
            error.put("providedId", id);
            error.put("providedTaskId", taskId);
            return error;
        }
        
        return toMap(task);
    }

    @McpTool(name = "task_complete", description = "Mark a task as completed with result")
    public Map<String, Object> completeTask(
            @McpToolParam(description = "Task ID (string taskId)", required = true) String taskId,
            @McpToolParam(description = "Result of the task execution") String result) {
        
        boolean success = taskQueueService.completeTask(taskId, result);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("taskId", taskId);
        
        if (success) {
            TaskQueueTask task = taskQueueService.getTaskByTaskId(taskId);
            if (task != null) {
                response.put("status", task.getStatus());
                response.put("completedAt", task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
                
                sendSubtaskCompletionNotification(task, result);
            }
        }
        
        return response;
    }

    private void sendSubtaskCompletionNotification(TaskQueueTask task, String result) {
        if (task.getTaskGroupId() == null || task.getTaskGroupId().isEmpty()) {
            return;
        }

        try {
            String targetAgent = task.getReportToAgent() != null && !task.getReportToAgent().isEmpty() 
                    ? task.getReportToAgent() 
                    : MENXIASHENG_AGENT_ID;

            Map<String, Object> content = new HashMap<>();
            content.put("type", MESSAGE_TYPE_SUBTASK_COMPLETED);
            content.put("taskId", task.getTaskId());
            content.put("taskGroupId", task.getTaskGroupId());
            content.put("summary", result != null ? result : "Task completed");
            content.put("assignedAgent", task.getAssignedAgent());
            content.put("title", task.getTitle());

            String contentJson = objectMapper.writeValueAsString(content);

            A2AMessage message = a2aMessageService.logMessage(
                    task.getAssignedAgent() != null ? task.getAssignedAgent() : "unknown",
                    targetAgent,
                    contentJson,
                    MESSAGE_TYPE_SUBTASK_COMPLETED
            );
            message.setTaskId(task.getTaskId());
            a2aMessageService.updateById(message);
        } catch (Exception e) {
            log.warn("Failed to send A2A notification for task {}: {}", task.getTaskId(), e.getMessage());
        }
    }

    @McpTool(name = "task_fail", description = "Mark a task as failed with error message")
    public Map<String, Object> failTask(
            @McpToolParam(description = "Task ID (string taskId)", required = true) String taskId,
            @McpToolParam(description = "Error message") String error) {
        
        boolean success = taskQueueService.failTask(taskId, error);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("taskId", taskId);
        
        if (success) {
            TaskQueueTask task = taskQueueService.getTaskByTaskId(taskId);
            if (task != null) {
                response.put("status", task.getStatus());
                response.put("retryCount", task.getRetryCount());
                response.put("maxRetries", task.getMaxRetries());
            }
        }
        
        return response;
    }

    @McpTool(name = "task_stats", description = "Get task queue statistics")
    public Map<String, Object> getTaskStats() {
        PageResponse<TaskQueueTask> response = taskQueueService.listTasks(1, 1, null, null, false);
        long total = response.getTotalElements();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTasks", total);
        stats.put("note", "Detailed stats require database query optimization");
        
        return stats;
    }

    private Map<String, Object> toMap(TaskQueueTask task) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", task.getId());
        map.put("taskId", task.getTaskId());
        map.put("type", task.getType());
        map.put("title", task.getTitle());
        map.put("payload", task.getPayload());
        map.put("priority", task.getPriority());
        map.put("status", task.getStatus());
        map.put("retryCount", task.getRetryCount());
        map.put("maxRetries", task.getMaxRetries());
        map.put("claimedBy", task.getClaimedBy());
        map.put("assignedAgent", task.getAssignedAgent());
        map.put("taskGroupId", task.getTaskGroupId());
        map.put("parentTaskId", task.getParentTaskId());
        map.put("reportToAgent", task.getReportToAgent());
        map.put("context", task.getContext());
        map.put("result", task.getResult());
        map.put("error", task.getError());
        map.put("lastError", task.getLastError());
        map.put("dependsOn", task.getDependsOn());
        map.put("createdAt", task.getCreatedAt() != null ? task.getCreatedAt().toString() : null);
        map.put("updatedAt", task.getUpdatedAt() != null ? task.getUpdatedAt().toString() : null);
        map.put("startedAt", task.getStartedAt() != null ? task.getStartedAt().toString() : null);
        map.put("completedAt", task.getCompletedAt() != null ? task.getCompletedAt().toString() : null);
        return map;
    }
}
