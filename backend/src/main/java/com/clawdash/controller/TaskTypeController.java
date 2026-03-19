package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.TaskType;
import com.clawdash.service.TaskTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/task-types")
public class TaskTypeController {

    @Autowired
    private TaskTypeService taskTypeService;

    @GetMapping
    public Result<List<TaskType>> getAll() {
        return Result.success(taskTypeService.getAll());
    }

    @GetMapping("/{id}")
    public Result<TaskType> getById(@PathVariable Long id) {
        TaskType taskType = taskTypeService.getById(id);
        if (taskType == null) {
            return Result.error("Task type not found");
        }
        return Result.success(taskType);
    }

    @PostMapping
    public Result<TaskType> create(@RequestBody TaskType taskType) {
        TaskType created = taskTypeService.create(taskType);
        return Result.success(created);
    }

    @PutMapping("/{id}")
    public Result<TaskType> update(@PathVariable Long id, @RequestBody TaskType taskType) {
        TaskType updated = taskTypeService.update(id, taskType);
        return Result.success(updated);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        boolean removed = taskTypeService.delete(id);
        if (removed) {
            return Result.success(null);
        }
        return Result.error("Failed to delete task type");
    }
}
