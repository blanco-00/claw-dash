package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.Task;
import com.clawdash.service.TaskService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public Result<Task> createTask(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        String payload = (String) request.get("payload");
        Integer priority = (Integer) request.get("priority");

        Task task = taskService.createTask(type, payload, priority);
        return Result.success(task);
    }

    @GetMapping
    public Result<List<Task>> listTasks(@RequestParam(required = false) String status) {
        List<Task> tasks;
        if (status != null && !status.isEmpty()) {
            tasks = taskService.lambdaQuery()
                    .eq(Task::getStatus, status)
                    .list();
        } else {
            tasks = taskService.list();
        }
        return Result.success(tasks);
    }

    @GetMapping("/{taskId}")
    public Result<Task> getTask(@PathVariable String taskId) {
        Task task = taskService.lambdaQuery()
                .eq(Task::getTaskId, taskId)
                .one();
        return Result.success(task);
    }

    @PostMapping("/claim")
    public Result<Task> claimTask(@RequestBody Map<String, String> request) {
        String workerId = request.getOrDefault("workerId", "worker-1");
        Task task = taskService.claimTask(workerId);
        if (task == null) {
            return Result.error("No pending tasks available");
        }
        return Result.success(task);
    }

    @PostMapping("/{taskId}/complete")
    public Result<Void> completeTask(@PathVariable String taskId, @RequestBody Map<String, String> request) {
        String result = request.get("result");
        taskService.completeTask(taskId, result);
        return Result.success();
    }

    @PostMapping("/{taskId}/fail")
    public Result<Void> failTask(@PathVariable String taskId, @RequestBody Map<String, Object> request) {
        String error = (String) request.get("error");
        Boolean retryable = (Boolean) request.getOrDefault("retryable", true);
        taskService.failTask(taskId, error, retryable);
        return Result.success();
    }

    @GetMapping("/stats")
    public Result<Map<String, Long>> getStats() {
        long pending = taskService.lambdaQuery().eq(Task::getStatus, "PENDING").count();
        long running = taskService.lambdaQuery().eq(Task::getStatus, "RUNNING").count();
        long completed = taskService.lambdaQuery().eq(Task::getStatus, "COMPLETED").count();
        long failed = taskService.lambdaQuery().eq(Task::getStatus, "FAILED").count();
        long dead = taskService.lambdaQuery().eq(Task::getStatus, "DEAD").count();

        return Result.success(Map.of(
                "PENDING", pending,
                "RUNNING", running,
                "COMPLETED", completed,
                "FAILED", failed,
                "DEAD", dead
        ));
    }
}
