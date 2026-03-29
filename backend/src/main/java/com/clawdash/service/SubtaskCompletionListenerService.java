package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.dto.SubtaskCompletionNotification;
import com.clawdash.entity.A2AMessage;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.mapper.A2AMessageMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class SubtaskCompletionListenerService extends ServiceImpl<A2AMessageMapper, A2AMessage> {

    private static final Logger log = LoggerFactory.getLogger(SubtaskCompletionListenerService.class);
    private static final String MENXIASHENG_AGENT_ID = "menxiasheng";
    private static final String MESSAGE_TYPE_SUBTASK_COMPLETED = "SUBTASK_COMPLETED";

    private final ObjectMapper objectMapper;
    private final TaskQueueService taskQueueService;

    @Autowired
    public SubtaskCompletionListenerService(ObjectMapper objectMapper, TaskQueueService taskQueueService) {
        this.objectMapper = objectMapper;
        this.taskQueueService = taskQueueService;
    }

    public List<SubtaskCompletionNotification> getPendingNotifications(LocalDateTime since) {
        List<A2AMessage> messages = lambdaQuery()
                .eq(A2AMessage::getToAgentId, MENXIASHENG_AGENT_ID)
                .eq(A2AMessage::getMessageType, MESSAGE_TYPE_SUBTASK_COMPLETED)
                .eq(A2AMessage::getStatus, "PENDING")
                .ge(since != null, A2AMessage::getSentAt, since)
                .orderByAsc(A2AMessage::getSentAt)
                .list();

        List<SubtaskCompletionNotification> notifications = new ArrayList<>();
        for (A2AMessage message : messages) {
            try {
                SubtaskCompletionNotification notification = parseNotification(message);
                if (notification != null) {
                    notifications.add(notification);
                }
            } catch (Exception e) {
                log.warn("Failed to parse notification message {}: {}", message.getId(), e.getMessage());
            }
        }

        return notifications;
    }

    public SubtaskCompletionNotification processNotification(Long messageId) {
        A2AMessage message = getById(messageId);
        if (message == null) {
            return null;
        }

        try {
            SubtaskCompletionNotification notification = parseNotification(message);
            if (notification != null) {
                SubtaskCompletionNotification enriched = enrichWithTaskDetails(notification);
                message.setStatus("PROCESSED");
                message.setDeliveredAt(LocalDateTime.now());
                updateById(message);
                return enriched;
            }
        } catch (Exception e) {
            log.warn("Failed to process notification {}: {}", messageId, e.getMessage());
            message.setStatus("FAILED");
            updateById(message);
        }

        return null;
    }

    public TaskQueueTask getFullSubtaskDetails(String taskId) {
        return taskQueueService.getTaskByTaskId(taskId);
    }

    public List<TaskQueueTask> getAllSubtasksInGroup(String taskGroupId) {
        return taskQueueService.lambdaQuery()
                .eq(TaskQueueTask::getTaskGroupId, taskGroupId)
                .orderByAsc(TaskQueueTask::getCreatedAt)
                .list();
    }

    public Map<String, Long> getGroupProgress(String taskGroupId) {
        List<TaskQueueTask> subtasks = getAllSubtasksInGroup(taskGroupId);
        
        long total = subtasks.size();
        long completed = subtasks.stream()
                .filter(t -> "COMPLETED".equals(t.getStatus()))
                .count();
        long failed = subtasks.stream()
                .filter(t -> "FAILED".equals(t.getStatus()) || "DEAD".equals(t.getStatus()))
                .count();
        long pending = subtasks.stream()
                .filter(t -> "PENDING".equals(t.getStatus()))
                .count();
        long running = subtasks.stream()
                .filter(t -> "RUNNING".equals(t.getStatus()))
                .count();

        return Map.of(
                "total", total,
                "completed", completed,
                "failed", failed,
                "pending", pending,
                "running", running
        );
    }

    public boolean isGroupComplete(String taskGroupId) {
        Map<String, Long> progress = getGroupProgress(taskGroupId);
        long total = progress.get("total");
        long completed = progress.get("completed");
        long failed = progress.get("failed");
        
        return total > 0 && (completed + failed) == total;
    }

    @SuppressWarnings("unchecked")
    private SubtaskCompletionNotification parseNotification(A2AMessage message) {
        try {
            Map<String, Object> content = objectMapper.readValue(message.getContent(), Map.class);
            
            SubtaskCompletionNotification notification = new SubtaskCompletionNotification();
            notification.setMessageId(message.getId());
            notification.setFromAgent(message.getFromAgentId());
            notification.setToAgent(message.getToAgentId());
            notification.setType((String) content.getOrDefault("type", MESSAGE_TYPE_SUBTASK_COMPLETED));
            notification.setTaskId((String) content.get("taskId"));
            notification.setTaskGroupId((String) content.get("taskGroupId"));
            notification.setSummary((String) content.get("summary"));
            notification.setAssignedAgent((String) content.get("assignedAgent"));
            notification.setTitle((String) content.get("title"));
            notification.setSentAt(message.getSentAt());
            
            return notification;
        } catch (Exception e) {
            log.warn("Failed to parse notification content: {}", e.getMessage());
            return null;
        }
    }

    private SubtaskCompletionNotification enrichWithTaskDetails(SubtaskCompletionNotification notification) {
        if (notification.getTaskId() == null) {
            return notification;
        }

        TaskQueueTask task = getFullSubtaskDetails(notification.getTaskId());
        if (task != null) {
            notification.setFullTaskDetails(task);
            notification.setResult(task.getResult());
            notification.setError(task.getError());
            notification.setStartedAt(task.getStartedAt());
            notification.setCompletedAt(task.getCompletedAt());
        }

        return notification;
    }
}
