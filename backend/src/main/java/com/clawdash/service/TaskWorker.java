package com.clawdash.service;

import com.clawdash.entity.TaskQueueTask;
import com.clawdash.entity.TaskStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class TaskWorker {

    private static final Logger log = LoggerFactory.getLogger(TaskWorker.class);

    private final TaskQueueService taskQueueService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${task-queue.workers:2}")
    private int workerCount;

    @Value("${task-queue.poll-interval:5000}")
    private long pollInterval;

    private final ExecutorService executor = Executors.newFixedThreadPool(2);

    public TaskWorker(TaskQueueService taskQueueService, RedisTemplate<String, Object> redisTemplate) {
        this.taskQueueService = taskQueueService;
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelayString = "${task-queue.poll-interval:5000}")
    public void pollAndExecute() {
        List<TaskQueueTask> pendingTasks = taskQueueService.lambdaQuery()
                .eq(TaskQueueTask::getStatus, TaskStatus.PENDING.getValue())
                .orderByAsc(TaskQueueTask::getPriority)
                .orderByAsc(TaskQueueTask::getCreatedAt)
                .list();

        for (TaskQueueTask task : pendingTasks) {
            if (shouldProcess(task)) {
                executor.submit(() -> processTask(task));
            }
        }
    }

    private boolean shouldProcess(TaskQueueTask task) {
        if (task.getScheduledAt() != null && task.getScheduledAt().isAfter(LocalDateTime.now())) {
            return false;
        }

        if (task.getDependsOn() != null && !task.getDependsOn().isEmpty()) {
            String[] deps = task.getDependsOn().split(",");
            for (String depId : deps) {
                TaskQueueTask depTask = taskQueueService.getTaskByTaskId(depId.trim());
                if (depTask == null || !TaskStatus.COMPLETED.getValue().equals(depTask.getStatus())) {
                    return false;
                }
            }
        }

        return true;
    }

    public void processTask(TaskQueueTask task) {
        String workerId = "worker-" + UUID.randomUUID().toString().substring(0, 8);

        log.info("Worker {} claiming task {}", workerId, task.getTaskId());

        TaskQueueTask claimed = taskQueueService.claimTask(task.getTaskId(), workerId);

        if (claimed == null) {
            log.debug("Task {} already claimed by another worker", task.getTaskId());
            return;
        }

        try {
            log.info("Executing task {} of type {}", task.getTaskId(), task.getType());

            executeTask(task);

            taskQueueService.completeTask(task.getTaskId(), "{\"success\": true}");
            log.info("Task {} completed successfully", task.getTaskId());

        } catch (Exception e) {
            log.error("Task {} failed: {}", task.getTaskId(), e.getMessage());
            taskQueueService.failTask(task.getTaskId(), e.getMessage());
        }
    }

    private void executeTask(TaskQueueTask task) {
        switch (task.getType()) {
            case "agent-execute":
                executeAgentTask(task);
                break;
            case "data-sync":
                executeDataSyncTask(task);
                break;
            case "notification":
                executeNotificationTask(task);
                break;
            case "cleanup":
                executeCleanupTask(task);
                break;
            default:
                log.warn("Unknown task type: {}", task.getType());
        }
    }

    private void executeAgentTask(TaskQueueTask task) {
        log.info("Executing agent task: {}", task.getTaskId());
    }

    private void executeDataSyncTask(TaskQueueTask task) {
        log.info("Executing data sync task: {}", task.getTaskId());
    }

    private void executeNotificationTask(TaskQueueTask task) {
        log.info("Executing notification task: {}", task.getTaskId());
    }

    private void executeCleanupTask(TaskQueueTask task) {
        log.info("Executing cleanup task: {}", task.getTaskId());
    }
}
