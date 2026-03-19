package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.TaskGroup;
import com.clawdash.mapper.TaskGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TaskGroupService extends ServiceImpl<TaskGroupMapper, TaskGroup> {

    public PageResponse<TaskGroup> listPage(PageRequest request) {
        Page<TaskGroup> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<TaskGroup> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(TaskGroup::getName, request.getKeyword())
                   .or()
                   .like(TaskGroup::getDescription, request.getKeyword());
        }
        
        wrapper.orderByDesc(TaskGroup::getCreatedAt);
        IPage<TaskGroup> result = page(page, wrapper);
        
        return new PageResponse<>(result.getRecords(), result.getTotal(), request.getPage(), request.getPageSize());
    }

    public Result<TaskGroup> create(TaskGroup taskGroup) {
        taskGroup.setGroupId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        taskGroup.setStatus("PENDING");
        taskGroup.setCreatedAt(LocalDateTime.now());
        taskGroup.setUpdatedAt(LocalDateTime.now());
        save(taskGroup);
        return Result.success(taskGroup);
    }

    public Result<TaskGroup> update(Long id, TaskGroup taskGroup) {
        TaskGroup existing = super.getById(id);
        if (existing == null) {
            return Result.error("Task group not found");
        }
        taskGroup.setId(id);
        taskGroup.setUpdatedAt(LocalDateTime.now());
        updateById(taskGroup);
        TaskGroup updated = super.getById(id);
        return Result.success(updated);
    }

    public Result<Void> delete(Long id) {
        removeById(id);
        return Result.success(null);
    }

    public Result<TaskGroup> getById(Long id) {
        TaskGroup taskGroup = super.getById(id);
        if (taskGroup == null) {
            return Result.error("Task group not found");
        }
        return Result.success(taskGroup);
    }

    public List<TaskGroup> getEnabledGroups() {
        return list(new LambdaQueryWrapper<TaskGroup>()
            .eq(TaskGroup::getStatus, "ENABLED")
            .orderByDesc(TaskGroup::getCreatedAt));
    }

    public TaskGroup toggleEnabled(Long id) {
        TaskGroup taskGroup = super.getById(id);
        if (taskGroup != null) {
            if ("ENABLED".equals(taskGroup.getStatus())) {
                taskGroup.setStatus("DISABLED");
            } else {
                taskGroup.setStatus("ENABLED");
            }
            taskGroup.setUpdatedAt(LocalDateTime.now());
            updateById(taskGroup);
        }
        return taskGroup;
    }
}
