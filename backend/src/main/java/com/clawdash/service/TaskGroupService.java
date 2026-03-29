package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.dto.TaskGroupDetailResponse;
import com.clawdash.dto.TaskGroupSummaryResponse;
import com.clawdash.entity.TaskGroup;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.mapper.TaskGroupMapper;
import com.clawdash.mapper.TaskQueueTaskMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TaskGroupService extends ServiceImpl<TaskGroupMapper, TaskGroup> {

    @Autowired
    private TaskQueueTaskMapper taskQueueTaskMapper;

    public PageResponse<TaskGroupSummaryResponse> listPage(PageRequest request) {
        Page<TaskGroup> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<TaskGroup> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(TaskGroup::getName, request.getKeyword())
                   .or()
                   .like(TaskGroup::getDescription, request.getKeyword());
        }
        
        if (StringUtils.hasText(request.getStatus())) {
            wrapper.eq(TaskGroup::getStatus, request.getStatus());
        }
        
        wrapper.orderByDesc(TaskGroup::getCreatedAt);
        
        long total = this.count(wrapper);
        IPage<TaskGroup> result = page(page, wrapper);
        
        // Convert to summary response with task counts
        List<TaskGroupSummaryResponse> summaries = result.getRecords().stream()
            .map(group -> {
                int[] counts = getTaskCounts(String.valueOf(group.getId()));
                return TaskGroupSummaryResponse.from(group, counts[0], counts[1], counts[2], counts[3], counts[4]);
            })
            .collect(java.util.stream.Collectors.toList());
        
        return PageResponse.of(summaries, total, (int) page.getCurrent(), (int) page.getSize());
    }
    
    private int[] getTaskCounts(String taskGroupId) {
        LambdaQueryWrapper<TaskQueueTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskQueueTask::getTaskGroupId, taskGroupId);
        List<TaskQueueTask> tasks = taskQueueTaskMapper.selectList(wrapper);
        
        int total = tasks.size();
        int completed = 0;
        int failed = 0;
        int running = 0;
        int pending = 0;
        
        for (TaskQueueTask task : tasks) {
            String status = task.getStatus();
            if ("COMPLETED".equals(status)) {
                completed++;
            } else if ("FAILED".equals(status) || "DEAD".equals(status)) {
                failed++;
            } else if ("RUNNING".equals(status)) {
                running++;
            } else if ("PENDING".equals(status)) {
                pending++;
            }
        }
        
        return new int[]{total, completed, failed, running, pending};
    }

    public Result<TaskGroup> create(TaskGroup taskGroup) {
        if (taskGroup.getStatus() == null || taskGroup.getStatus().isEmpty()) {
            taskGroup.setStatus("pending");
        }
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

    @Transactional
    public Result<Void> delete(Long id) {
        // 先删除关联的子任务
        LambdaQueryWrapper<TaskQueueTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskQueueTask::getTaskGroupId, String.valueOf(id));
        taskQueueTaskMapper.delete(wrapper);
        
        // 再删除任务组本身
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

    public Result<TaskGroupDetailResponse> getDetail(Long id) {
        TaskGroup taskGroup = super.getById(id);
        if (taskGroup == null) {
            return Result.error("Task group not found");
        }
        
        LambdaQueryWrapper<TaskQueueTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskQueueTask::getTaskGroupId, String.valueOf(id));
        List<TaskQueueTask> tasks = taskQueueTaskMapper.selectList(wrapper);
        
        TaskGroupDetailResponse detail = TaskGroupDetailResponse.from(taskGroup, tasks);
        return Result.success(detail);
    }

    public Result<TaskGroup> updateStatus(Long id, String status) {
        TaskGroup taskGroup = super.getById(id);
        if (taskGroup == null) {
            return Result.error("Task group not found");
        }
        
        taskGroup.setStatus(status);
        taskGroup.setUpdatedAt(LocalDateTime.now());
        
        if ("completed".equals(status) || "failed".equals(status)) {
            taskGroup.setCompletedAt(LocalDateTime.now());
        }
        
        updateById(taskGroup);
        return Result.success(taskGroup);
    }

    public Result<Map<String, Object>> getProgress(Long id) {
        TaskGroup taskGroup = super.getById(id);
        if (taskGroup == null) {
            return Result.error("Task group not found");
        }
        
        LambdaQueryWrapper<TaskQueueTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskQueueTask::getTaskGroupId, String.valueOf(taskGroup.getId()));
        
        List<TaskQueueTask> tasks = taskQueueTaskMapper.selectList(wrapper);
        
        int total = tasks.size();
        int completed = 0;
        int failed = 0;
        int running = 0;
        int pending = 0;
        
        for (TaskQueueTask task : tasks) {
            String status = task.getStatus();
            if ("COMPLETED".equals(status)) {
                completed++;
            } else if ("FAILED".equals(status) || "DEAD".equals(status)) {
                failed++;
            } else if ("RUNNING".equals(status)) {
                running++;
            } else if ("PENDING".equals(status)) {
                pending++;
            }
        }
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("taskGroupId", taskGroup.getId());
        progress.put("title", taskGroup.getName());
        progress.put("status", taskGroup.getStatus());
        progress.put("total", total);
        progress.put("completed", completed);
        progress.put("failed", failed);
        progress.put("running", running);
        progress.put("pending", pending);
        progress.put("progressPercentage", total > 0 ? (completed * 100.0 / total) : 0);
        
        return Result.success(progress);
    }

    @Transactional
    public Result<TaskGroup> abandonTaskGroup(Long id, String reason) {
        TaskGroup taskGroup = super.getById(id);
        if (taskGroup == null) {
            return Result.error("Task group not found");
        }
        
        taskGroup.setStatus("failed");
        taskGroup.setCompletedAt(LocalDateTime.now());
        taskGroup.setUpdatedAt(LocalDateTime.now());
        updateById(taskGroup);
        
        LambdaQueryWrapper<TaskQueueTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TaskQueueTask::getTaskGroupId, String.valueOf(taskGroup.getId()))
               .in(TaskQueueTask::getStatus, "PENDING", "RUNNING");
        
        TaskQueueTask updateTask = new TaskQueueTask();
        updateTask.setStatus("DEAD");
        updateTask.setLastError("Task group abandoned: " + (reason != null ? reason : "No reason provided"));
        updateTask.setUpdatedAt(LocalDateTime.now());
        
        taskQueueTaskMapper.update(updateTask, wrapper);
        
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

    public TaskGroup getByGroupId(String groupId) {
        return lambdaQuery()
            .eq(TaskGroup::getId, Long.parseLong(groupId))
            .one();
    }
}
