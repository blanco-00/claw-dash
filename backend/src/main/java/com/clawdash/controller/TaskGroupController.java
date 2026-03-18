package com.clawdash.controller;

import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.TaskGroup;
import com.clawdash.service.TaskGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-groups")
public class TaskGroupController {

    @Autowired
    private TaskGroupService taskGroupService;

    @GetMapping
    public Result<PageResponse<TaskGroup>> list(PageRequest request) {
        return Result.success(taskGroupService.listPage(request));
    }

    @GetMapping("/enabled")
    public Result<List<TaskGroup>> getEnabled() {
        return Result.success(taskGroupService.getEnabledGroups());
    }

    @GetMapping("/{id}")
    public Result<TaskGroup> getById(@PathVariable Long id) {
        return taskGroupService.getById(id);
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
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        return taskGroupService.toggleEnabled(id);
    }
}
