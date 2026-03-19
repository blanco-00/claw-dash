package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.dto.TaskPageResponse;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.entity.TaskStatus;
import com.clawdash.mapper.TaskQueueTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TaskQueueService extends ServiceImpl<TaskQueueTaskMapper, TaskQueueTask> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TASK_LOCK_PREFIX = "task:lock:";
    private static final int LOCK_TIMEOUT_SECONDS = 30;

    @Transactional
    public TaskQueueTask createTask(CreateTaskRequest request) {
        TaskQueueTask task = new TaskQueueTask();
        task.setTaskId("task-" + UUID.randomUUID().toString().replace("-", ""));
        task.setType(request.getType());
        
        try {
            task.setPayload(objectMapper.writeValueAsString(request.getPayload()));
        } catch (Exception e) {
            task.setPayload("{}");
        }
        
        task.setPriority(request.getPriority() != null ? request.getPriority() : 5);
        task.setStatus(TaskStatus.PENDING.getValue());
        task.setRetryCount(0);
        task.setMaxRetries(request.getMaxRetries() != null ? request.getMaxRetries() : 3);
        
        if (request.getDependsOn() != null && !request.getDependsOn().isEmpty()) {
            task.setDependsOn(String.join(",", request.getDependsOn()));
        }
        
        if (request.getScheduledAt() != null) {
            task.setScheduledAt(LocalDateTime.parse(request.getScheduledAt()));
        }
        
        task.setCreatedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        save(task);
        
        pushToRedisQueue(task);
        
        return task;
    }

    public TaskPageResponse listTasks(int page, int size, String status, String sortBy, boolean ascending) {
        Page<TaskQueueTask> pageParam = new Page<>(page, size);
        
        LambdaQueryWrapper<TaskQueueTask> wrapper = new LambdaQueryWrapper<>();
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(TaskQueueTask::getStatus, status);
        }
        
        if ("priority".equals(sortBy)) {
            wrapper.orderBy(ascending, true, TaskQueueTask::getPriority);
        } else if ("createdAt".equals(sortBy)) {
            wrapper.orderBy(ascending, false, TaskQueueTask::getCreatedAt);
        } else {
            wrapper.orderByDesc(TaskQueueTask::getCreatedAt);
        }
        
        Page<TaskQueueTask> result = page(pageParam, wrapper);
        
        return new TaskPageResponse(
            result.getRecords(),
            result.getTotal(),
            (int) result.getPages(),
            (int) result.getSize(),
            (int) result.getCurrent(),
            result.getCurrent() == 1,
            result.getCurrent() >= result.getPages()
        );
    }

    public TaskQueueTask getTaskById(Long id) {
        return getById(id);
    }

    public TaskQueueTask getTaskByTaskId(String taskId) {
        return lambdaQuery()
            .eq(TaskQueueTask::getTaskId, taskId)
            .one();
    }

    @Transactional
    public TaskQueueTask claimTask(String taskId, String workerId) {
        String lockKey = TASK_LOCK_PREFIX + taskId;
        
        Boolean acquired = redisTemplate.opsForValue()
            .setIfAbsent(lockKey, workerId, LOCK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        
        if (Boolean.TRUE.equals(acquired)) {
            try {
                TaskQueueTask task = getTaskByTaskId(taskId);
                
                if (task == null) {
                    return null;
                }
                
                if (!TaskStatus.PENDING.getValue().equals(task.getStatus())) {
                    return null;
                }
                
                if (task.getScheduledAt() != null && 
                    task.getScheduledAt().isAfter(LocalDateTime.now())) {
                    return null;
                }
                
                task.setStatus(TaskStatus.RUNNING.getValue());
                task.setClaimedBy(workerId);
                task.setStartedAt(LocalDateTime.now());
                task.setUpdatedAt(LocalDateTime.now());
                
                updateById(task);
                
                return task;
            } finally {
                redisTemplate.delete(lockKey);
            }
        }
        
        return null;
    }

    @Transactional
    public boolean completeTask(String taskId, String result) {
        TaskQueueTask task = getTaskByTaskId(taskId);
        
        if (task == null || !TaskStatus.RUNNING.getValue().equals(task.getStatus())) {
            return false;
        }
        
        task.setStatus(TaskStatus.COMPLETED.getValue());
        task.setResult(result);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        return updateById(task);
    }

    @Transactional
    public boolean failTask(String taskId, String error) {
        TaskQueueTask task = getTaskByTaskId(taskId);
        
        if (task == null || !TaskStatus.RUNNING.getValue().equals(task.getStatus())) {
            return false;
        }
        
        task.setRetryCount(task.getRetryCount() + 1);
        task.setError(error);
        
        if (task.getRetryCount() >= task.getMaxRetries()) {
            task.setStatus(TaskStatus.DEAD.getValue());
        } else {
            task.setStatus(TaskStatus.PENDING.getValue());
        }
        
        task.setUpdatedAt(LocalDateTime.now());
        
        return updateById(task);
    }

    private void pushToRedisQueue(TaskQueueTask task) {
        String queueKey = "task:queue:pending";
        redisTemplate.opsForList().rightPush(queueKey, task.getTaskId());
    }
}
