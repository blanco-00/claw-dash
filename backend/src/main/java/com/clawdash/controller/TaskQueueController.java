package com.clawdash.controller;

import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.dto.NotifyAgentRequest;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.service.OpenClawService;
import com.clawdash.service.TaskQueueService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/task-queue")
public class TaskQueueController {

    private final TaskQueueService taskQueueService;
    private final OpenClawService openClawService;
    private final RestTemplate restTemplate = new RestTemplate();

    public TaskQueueController(TaskQueueService taskQueueService, OpenClawService openClawService) {
        this.taskQueueService = taskQueueService;
        this.openClawService = openClawService;
    }

    @PostMapping("/tasks")
    public Result<TaskQueueTask> createTask(@RequestBody CreateTaskRequest request) {
        TaskQueueTask task = taskQueueService.createTask(request);
        return Result.success(task);
    }

    @GetMapping("/tasks")
    public Result<PageResponse<TaskQueueTask>> listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String assignedAgent,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {
        
        PageResponse<TaskQueueTask> response = taskQueueService.listTasks(page, size, status, assignedAgent, sortBy, ascending);
        return Result.success(response);
    }

    @GetMapping("/tasks/{id}")
    public Result<TaskQueueTask> getTask(@PathVariable Long id) {
        TaskQueueTask task = taskQueueService.getTaskById(id);
        if (task == null) {
            return Result.error("Task not found");
        }
        return Result.success(task);
    }

    @PostMapping("/tasks/{taskId}/claim")
    public Result<TaskQueueTask> claimTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        
        String workerId = request.getOrDefault("workerId", "worker-1");
        TaskQueueTask task = taskQueueService.claimTask(taskId, workerId);
        
        if (task == null) {
            return Result.error("Failed to claim task");
        }
        return Result.success(task);
    }

    @PostMapping("/tasks/{taskId}/complete")
    public Result<Void> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, String> request) {
        
        String result = request != null ? request.get("result") : null;
        boolean success = taskQueueService.completeTask(taskId, result);
        
        if (!success) {
            return Result.error("Failed to complete task");
        }
        return Result.success();
    }

    @PostMapping("/tasks/{taskId}/fail")
    public Result<Void> failTask(
            @PathVariable String taskId,
            @RequestBody Map<String, String> request) {
        
        String error = request.get("error");
        boolean success = taskQueueService.failTask(taskId, error);
        
        if (!success) {
            return Result.error("Failed to fail task");
        }
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("pending", taskQueueService.lambdaQuery().eq(TaskQueueTask::getStatus, "PENDING").count());
        stats.put("running", taskQueueService.lambdaQuery().eq(TaskQueueTask::getStatus, "RUNNING").count());
        stats.put("completed", taskQueueService.lambdaQuery().eq(TaskQueueTask::getStatus, "COMPLETED").count());
        stats.put("failed", taskQueueService.lambdaQuery().eq(TaskQueueTask::getStatus, "FAILED").count());
        stats.put("dead", taskQueueService.lambdaQuery().eq(TaskQueueTask::getStatus, "DEAD").count());
        return Result.success(stats);
    }

    @GetMapping("/agent-stats")
    public Result<Map<String, Object>> getAgentStats(@RequestParam String agentId) {
        Map<String, Object> stats = taskQueueService.getAgentStats(agentId);
        return Result.success(stats);
    }

    @PostMapping("/notify-agent")
    public Result<Void> notifyAgent(@RequestBody NotifyAgentRequest request) {
        try {
            String openClawUrl = openClawService.getSavedApiUrl();
            String webhookUrl = openClawUrl + "/hooks/agent";
            
            Map<String, Object> payload = new HashMap<>();
            payload.put("message", buildNotificationMessage(request));
            payload.put("agentId", request.getAgentId());
            payload.put("sessionKey", buildSessionKey(request));
            payload.put("wakeMode", "now");
            payload.put("deliver", true);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);
            
            restTemplate.postForObject(webhookUrl, entity, String.class);
            
            return Result.success();
        } catch (Exception e) {
            return Result.error("Failed to notify agent: " + e.getMessage());
        }
    }

    private String buildNotificationMessage(NotifyAgentRequest request) {
        if ("decompose".equals(request.getAction())) {
            return "New TaskGroup created. Call GET /api/task-groups/" + request.getTaskGroupId() + "/detail to decompose.";
        } else {
            return "New task assigned. TaskGroupId: " + request.getTaskGroupId();
        }
    }

    private String buildSessionKey(NotifyAgentRequest request) {
        if ("decompose".equals(request.getAction())) {
            return "clawdash:task-group:" + request.getTaskGroupId();
        } else {
            return "clawdash:task:" + request.getTaskGroupId();
        }
    }

    @DeleteMapping("/tasks/{taskId}")
    public Result<Void> deleteTask(@PathVariable String taskId) {
        boolean success = taskQueueService.deleteTask(taskId);
        if (!success) {
            return Result.error("Cannot delete task: task not found or is currently running");
        }
        return Result.success();
    }
}
