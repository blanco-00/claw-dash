package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.common.PageResponse;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.entity.TaskGroup;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.entity.TaskStatus;
import com.clawdash.mapper.TaskGroupMapper;
import com.clawdash.mapper.TaskQueueTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class TaskQueueService extends ServiceImpl<TaskQueueTaskMapper, TaskQueueTask> {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    @Autowired
    private TaskGroupMapper taskGroupMapper;

    private static final String TASK_LOCK_PREFIX = "task:lock:";
    private static final int LOCK_TIMEOUT_SECONDS = 30;
    private static final int BASE_RETRY_DELAY_SECONDS = 5;
    private static final int MAX_RETRY_DELAY_SECONDS = 300;

    public TaskQueueService(RedisTemplate<String, Object> redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

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
        
        if (request.getTaskGroupId() != null && !request.getTaskGroupId().isEmpty()) {
            task.setTaskGroupId(request.getTaskGroupId());
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

    public PageResponse<TaskQueueTask> listTasks(int page, int size, String status, String sortBy, boolean ascending) {
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
        
        long total = this.count(wrapper);
        Page<TaskQueueTask> result = page(pageParam, wrapper);
        
        return PageResponse.of(result.getRecords(), total, (int) pageParam.getCurrent(), (int) pageParam.getSize());
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
        
        String taskGroupId = task.getTaskGroupId();
        
        task.setStatus(TaskStatus.COMPLETED.getValue());
        task.setResult(result);
        task.setCompletedAt(LocalDateTime.now());
        task.setUpdatedAt(LocalDateTime.now());
        
        boolean success = updateById(task);
        
        if (success) {
            syncTaskGroupStatus(taskGroupId);
        }
        
        return success;
    }

    @Transactional
    public boolean failTask(String taskId, String error) {
        TaskQueueTask task = getTaskByTaskId(taskId);
        
        if (task == null || !TaskStatus.RUNNING.getValue().equals(task.getStatus())) {
            return false;
        }
        
        String taskGroupId = task.getTaskGroupId();
        
        task.setRetryCount(task.getRetryCount() + 1);
        task.setLastError(error);
        task.setError(error);
        
        if (task.getRetryCount() >= task.getMaxRetries()) {
            task.setStatus(TaskStatus.NEEDS_INTERVENTION.getValue());
        } else {
            task.setStatus(TaskStatus.PENDING.getValue());
            LocalDateTime retryTime = calculateExponentialBackoff(task.getRetryCount());
            task.setScheduledAt(retryTime);
        }
        
        task.setUpdatedAt(LocalDateTime.now());
        
        boolean success = updateById(task);
        
        if (success) {
            syncTaskGroupStatus(taskGroupId);
        }
        
        return success;
    }

    protected LocalDateTime calculateExponentialBackoff(int retryCount) {
        int delaySeconds = (int) Math.min(
                BASE_RETRY_DELAY_SECONDS * Math.pow(2, retryCount),
                MAX_RETRY_DELAY_SECONDS
        );
        return LocalDateTime.now().plusSeconds(delaySeconds);
    }

    private void pushToRedisQueue(TaskQueueTask task) {
        String queueKey = "task:queue:pending";
        redisTemplate.opsForList().rightPush(queueKey, task.getTaskId());
    }

    private void syncTaskGroupStatus(String taskGroupId) {
        if (taskGroupId == null || taskGroupId.isEmpty()) {
            return;
        }
        
        TaskGroup taskGroup = taskGroupMapper.selectById(Long.parseLong(taskGroupId));
        if (taskGroup == null) {
            return;
        }
        
        List<TaskQueueTask> tasks = lambdaQuery()
                .eq(TaskQueueTask::getTaskGroupId, taskGroupId)
                .list();
        
        if (tasks.isEmpty()) {
            return;
        }
        
        int total = tasks.size();
        int completed = 0;
        int failed = 0;
        int needsIntervention = 0;
        int running = 0;
        int pending = 0;
        
        for (TaskQueueTask task : tasks) {
            String status = task.getStatus();
            if (TaskStatus.COMPLETED.getValue().equals(status)) {
                completed++;
            } else if (TaskStatus.FAILED.getValue().equals(status) || TaskStatus.DEAD.getValue().equals(status)) {
                failed++;
            } else if (TaskStatus.NEEDS_INTERVENTION.getValue().equals(status)) {
                needsIntervention++;
            } else if (TaskStatus.RUNNING.getValue().equals(status)) {
                running++;
            } else if (TaskStatus.PENDING.getValue().equals(status)) {
                pending++;
            }
        }
        
        String newStatus = taskGroup.getStatus();
        
        if (failed > 0 || needsIntervention > 0) {
            newStatus = "NEEDS_INTERVENTION";
        } else if (completed == total) {
            newStatus = "COMPLETED";
        } else if (running > 0 || pending > 0) {
            newStatus = "IN_PROGRESS";
        }
        
        if (!newStatus.equals(taskGroup.getStatus())) {
            taskGroup.setStatus(newStatus);
            if ("COMPLETED".equals(newStatus) || "FAILED".equals(newStatus)) {
                taskGroup.setCompletedAt(LocalDateTime.now());
            }
            taskGroup.setUpdatedAt(LocalDateTime.now());
            taskGroupMapper.updateById(taskGroup);
        }
    }

    @Transactional
    public boolean deleteTask(String taskId) {
        TaskQueueTask task = getTaskByTaskId(taskId);
        
        if (task == null) {
            return false;
        }
        
        String status = task.getStatus();
        
        if (TaskStatus.PENDING.getValue().equals(status) ||
            TaskStatus.COMPLETED.getValue().equals(status) ||
            TaskStatus.FAILED.getValue().equals(status) ||
            TaskStatus.DEAD.getValue().equals(status) ||
            TaskStatus.NEEDS_INTERVENTION.getValue().equals(status)) {
            return removeById(task.getId());
        }
        
        return false;
    }

    public List<TaskQueueTask> getNeedsIntervention() {
        return lambdaQuery()
            .eq(TaskQueueTask::getStatus, TaskStatus.NEEDS_INTERVENTION.getValue())
            .orderByDesc(TaskQueueTask::getUpdatedAt)
            .list();
    }

    @Transactional
    public TaskQueueTask reassignTask(Long id, String newAssignedAgent, String reason) {
        TaskQueueTask task = getById(id);
        
        if (task == null) {
            return null;
        }
        
        String oldAgent = task.getAssignedAgent();
        task.setAssignedAgent(newAssignedAgent);
        task.setStatus(TaskStatus.PENDING.getValue());
        task.setRetryCount(0);
        task.setLastError("Reassigned from " + oldAgent + ": " + (reason != null ? reason : "No reason provided"));
        task.setUpdatedAt(LocalDateTime.now());
        
        updateById(task);
        
        return task;
    }
}
