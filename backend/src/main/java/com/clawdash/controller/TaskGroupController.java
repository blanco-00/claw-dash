package com.clawdash.controller;

import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.dto.TaskGroupDetailResponse;
import com.clawdash.entity.TaskGroup;
import com.clawdash.service.TaskGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/task-groups")
public class TaskGroupController {

    @Autowired
    private TaskGroupService taskGroupService;

    @GetMapping
    public Result<PageResponse<TaskGroup>> list(PageRequest request) {
        return Result.success(taskGroupService.listPage(request));
    }

    @GetMapping("/stats")
    public Result<?> stats() {
        List<TaskGroup> groups = taskGroupService.list();
        Map<String, Object> counts = new java.util.HashMap<>();
        counts.put("total", (long) groups.size());
        counts.put("inProgress", groups.stream().filter(g -> "IN_PROGRESS".equals(g.getStatus())).count());
        counts.put("needsIntervention", groups.stream().filter(g -> "NEEDS_INTERVENTION".equals(g.getStatus())).count());
        counts.put("completed", groups.stream().filter(g -> "COMPLETED".equals(g.getStatus())).count());
        counts.put("failed", groups.stream().filter(g -> "FAILED".equals(g.getStatus())).count());
        return Result.success(counts);
    }

    @GetMapping("/enabled")
    public Result<List<TaskGroup>> getEnabled() {
        return Result.success(taskGroupService.getEnabledGroups());
    }

    @GetMapping("/{id}")
    public Result<TaskGroup> getById(@PathVariable Long id) {
        return taskGroupService.getById(id);
    }

    @GetMapping("/{id}/detail")
    public Result<TaskGroupDetailResponse> getDetail(@PathVariable Long id) {
        return taskGroupService.getDetail(id);
    }

    @PostMapping
    public Result<TaskGroup> create(@RequestBody TaskGroup taskGroup) {
        return taskGroupService.create(taskGroup);
    }

    @PutMapping("/{id}")
    public Result<TaskGroup> update(@PathVariable Long id, @RequestBody TaskGroup taskGroup) {
        return taskGroupService.update(id, taskGroup);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return taskGroupService.delete(id);
    }

    @PatchMapping("/{id}/toggle")
    public Result<TaskGroup> toggleEnabled(@PathVariable Long id) {
        TaskGroup result = taskGroupService.toggleEnabled(id);
        return result != null ? Result.success(result) : Result.error("Task group not found");
    }

    @PatchMapping("/{id}/status")
    public Result<TaskGroup> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        if (status == null || status.isEmpty()) {
            return Result.error("Status is required");
        }
        
        TaskGroup taskGroup = taskGroupService.getById(id).getData();
        if (taskGroup == null) {
            return Result.error("Task group not found");
        }
        
        taskGroup.setStatus(status);
        taskGroup.setUpdatedAt(java.time.LocalDateTime.now());
        
        if ("completed".equals(status) || "failed".equals(status)) {
            taskGroup.setCompletedAt(java.time.LocalDateTime.now());
        }
        
        taskGroupService.updateById(taskGroup);
        return Result.success(taskGroup);
    }

    @GetMapping("/{id}/progress")
    public Result<Map<String, Object>> getProgress(@PathVariable Long id) {
        return taskGroupService.getProgress(id);
    }

    @PostMapping("/{id}/abandon")
    public Result<TaskGroup> abandonTaskGroup(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> request) {
        String reason = request != null ? request.get("reason") : null;
        return taskGroupService.abandonTaskGroup(id, reason);
    }
}
