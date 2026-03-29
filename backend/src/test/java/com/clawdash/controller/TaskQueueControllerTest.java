package com.clawdash.controller;

import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.dto.NotifyAgentRequest;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.entity.TaskStatus;
import com.clawdash.service.OpenClawService;
import com.clawdash.service.TaskQueueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskQueueControllerTest {

    @Mock
    private TaskQueueService taskQueueService;

    @Mock
    private OpenClawService openClawService;

    @InjectMocks
    private TaskQueueController taskQueueController;

    private TaskQueueTask sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new TaskQueueTask();
        sampleTask.setId(1L);
        sampleTask.setTaskId("task-test-123");
        sampleTask.setType("test-type");
        sampleTask.setPayload("{\"key\": \"value\"}");
        sampleTask.setPriority(5);
        sampleTask.setStatus(TaskStatus.PENDING.getValue());
        sampleTask.setRetryCount(0);
        sampleTask.setMaxRetries(3);
        sampleTask.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testCreateTask() {
        when(taskQueueService.createTask(any(CreateTaskRequest.class))).thenReturn(sampleTask);

        CreateTaskRequest request = new CreateTaskRequest();
        request.setType("agent-execute");

        Result<TaskQueueTask> result = taskQueueController.createTask(request);

        assertNotNull(result);
        assertEquals("task-test-123", result.getData().getTaskId());
    }

    @Test
    void testListTasks() {
        // Match the 6-parameter overload with null for assignedAgent
        when(taskQueueService.listTasks(eq(0), eq(20), isNull(), isNull(), eq("createdAt"), eq(false)))
            .thenReturn(new PageResponse<>(List.of(sampleTask), 1, 1, 20));
        
        Result<PageResponse<TaskQueueTask>> result = taskQueueController.listTasks(0, 20, null, null, "createdAt", false);
        
        assertNotNull(result);
        assertEquals(1, result.getData().getContent().size());
    }

    @Test
    void testGetTask_Found() {
        when(taskQueueService.getTaskById(1L)).thenReturn(sampleTask);

        Result<TaskQueueTask> result = taskQueueController.getTask(1L);

        assertNotNull(result);
        assertEquals("task-test-123", result.getData().getTaskId());
    }

    @Test
    void testGetTask_NotFound() {
        when(taskQueueService.getTaskById(999L)).thenReturn(null);

        Result<TaskQueueTask> result = taskQueueController.getTask(999L);

        assertNotNull(result);
    }

    @Test
    void testClaimTask_Success() {
        when(taskQueueService.claimTask(eq("task-test-123"), anyString()))
            .thenReturn(sampleTask);

        Result<TaskQueueTask> result = taskQueueController.claimTask(
            "task-test-123", 
            Map.of("workerId", "worker-1")
        );

        assertNotNull(result);
    }

    @Test
    void testCompleteTask_Success() {
        when(taskQueueService.completeTask(eq("task-test-123"), any())).thenReturn(true);

        Result<Void> result = taskQueueController.completeTask(
            "task-test-123",
            Map.of("result", "success")
        );

        assertNotNull(result);
    }

    @Test
    void testFailTask_Success() {
        when(taskQueueService.failTask(eq("task-test-123"), any())).thenReturn(true);

        Result<Void> result = taskQueueController.failTask(
            "task-test-123",
            Map.of("error", "test error")
        );

        assertNotNull(result);
    }

    @Test
    void testNotifyAgent() {
        when(openClawService.getSavedApiUrl()).thenReturn("http://localhost:3000");

        NotifyAgentRequest request = new NotifyAgentRequest();
        request.setAgentId("test-agent");
        request.setTaskGroupId("tg-123");
        request.setAction("decompose");

        Result<Void> result = taskQueueController.notifyAgent(request);

        assertNotNull(result);
        assertEquals(0, result.getCode());
    }

    @Test
    void testListTasks_WithAssignedAgent() {
        TaskQueueTask assignedTask = new TaskQueueTask();
        assignedTask.setId(2L);
        assignedTask.setTaskId("task-assigned-456");
        assignedTask.setAssignedAgent("specific-agent");
        assignedTask.setStatus(TaskStatus.PENDING.getValue());

        PageResponse<TaskQueueTask> pageResponse = new PageResponse<>(
            List.of(assignedTask), 1, 1, 20
        );

        when(taskQueueService.listTasks(eq(0), eq(20), isNull(), eq("specific-agent"), eq("createdAt"), eq(false)))
            .thenReturn(pageResponse);

        Result<PageResponse<TaskQueueTask>> result = taskQueueController.listTasks(0, 20, null, "specific-agent", "createdAt", false);

        assertNotNull(result);
        assertEquals(1, result.getData().getContent().size());
        assertEquals("specific-agent", result.getData().getContent().get(0).getAssignedAgent());
    }

    @Test
    void testGetAgentStats() {
        Map<String, Object> stats = Map.of(
            "pending", 5L,
            "running", 2L,
            "completedToday", 10L,
            "completedThisWeek", 45L,
            "failed", 1L
        );

        when(taskQueueService.getAgentStats("test-agent")).thenReturn(stats);

        Result<Map<String, Object>> result = taskQueueController.getAgentStats("test-agent");

        assertNotNull(result);
        assertEquals(5L, result.getData().get("pending"));
        assertEquals(2L, result.getData().get("running"));
    }
}
