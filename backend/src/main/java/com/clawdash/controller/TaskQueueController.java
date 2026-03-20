package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.dto.TaskPageResponse;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.service.TaskQueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/task-queue")
@RequiredArgsConstructor
public class TaskQueueController {

    private final TaskQueueService taskQueueService;

    @PostMapping("/tasks")
    public Result<TaskQueueTask> createTask(@RequestBody CreateTaskRequest request) {
        TaskQueueTask task = taskQueueService.createTask(request);
        return Result.success(task);
    }

    @GetMapping("/tasks")
    public Result<TaskPageResponse> listTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "false") boolean ascending) {
        
        TaskPageResponse response = taskQueueService.listTasks(page, size, status, sortBy, ascending);
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

    @DeleteMapping("/tasks/{taskId}")
    public Result<Void> deleteTask(@PathVariable String taskId) {
        boolean success = taskQueueService.deleteTask(taskId);
        if (!success) {
            return Result.error("Cannot delete task: task not found or is currently running");
        }
        return Result.success();
    }
}
