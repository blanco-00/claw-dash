package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.Task;
import com.clawdash.mapper.TaskMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TaskService extends ServiceImpl<TaskMapper, Task> {

    public Task createTask(String type, String payload, Integer priority) {
        Task task = new Task();
        task.setTaskId("task-" + UUID.randomUUID().toString().replace("-", ""));
        task.setType(type);
        task.setPayload(payload);
        task.setPriority(priority != null ? priority : 0);
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setMaxRetries(3);
        task.setCreatedAt(LocalDateTime.now());
        save(task);
        return task;
    }

    public Task claimTask(String workerId) {
        Task task = lambdaQuery()
                .eq(Task::getStatus, "PENDING")
                .orderByAsc(Task::getPriority)
                .orderByAsc(Task::getCreatedAt)
                .last("LIMIT 1")
                .one();

        if (task != null) {
            task.setStatus("RUNNING");
            task.setClaimedBy(workerId);
            task.setClaimedAt(LocalDateTime.now());
            task.setStartedAt(LocalDateTime.now());
            updateById(task);
        }
        return task;
    }

    public void completeTask(String taskId, String result) {
        Task task = lambdaQuery().eq(Task::getTaskId, taskId).one();
        if (task != null) {
            task.setStatus("COMPLETED");
            task.setResult(result);
            task.setCompletedAt(LocalDateTime.now());
            updateById(task);
        }
    }

    public void failTask(String taskId, String error, boolean retryable) {
        Task task = lambdaQuery().eq(Task::getTaskId, taskId).one();
        if (task != null) {
            if (retryable && task.getRetryCount() < task.getMaxRetries()) {
                task.setRetryCount(task.getRetryCount() + 1);
                task.setStatus("PENDING");
                task.setError(error);
            } else {
                task.setStatus("DEAD");
                task.setError(error);
            }
            updateById(task);
        }
    }
}
