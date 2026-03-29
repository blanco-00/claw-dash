package com.clawdash.integration;

import com.clawdash.common.PageResponse;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.entity.TaskStatus;
import com.clawdash.mapper.TaskGroupMapper;
import com.clawdash.mapper.TaskQueueTaskMapper;
import com.clawdash.service.OpenClawService;
import com.clawdash.service.TaskQueueService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskDistributionIntegrationTest {

    @Mock
    private TaskQueueTaskMapper taskMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private OpenClawService openClawService;

    @Mock
    private TaskGroupMapper taskGroupMapper;

    @Mock
    private RestTemplate restTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    private TaskQueueService taskQueueService;

    private TaskQueueTask sampleTask;

    @BeforeEach
    void setUp() {
        taskQueueService = new TaskQueueService(redisTemplate, objectMapper, openClawService);
        ReflectionTestUtils.setField(taskQueueService, "taskGroupMapper", taskGroupMapper);
        ReflectionTestUtils.setField(taskQueueService, "restTemplate", restTemplate);

        sampleTask = new TaskQueueTask();
        sampleTask.setId(1L);
        sampleTask.setTaskId("task-test-123");
        sampleTask.setType("agent-execute");
        sampleTask.setPayload("{\"description\": \"Test task\"}");
        sampleTask.setPriority(5);
        sampleTask.setStatus(TaskStatus.PENDING.getValue());
        sampleTask.setRetryCount(0);
        sampleTask.setMaxRetries(3);
        sampleTask.setCreatedAt(LocalDateTime.now());
        sampleTask.setTaskGroupId("tg-123");
    }

    @Test
    void testFullTaskGroupDecompositionFlow() {
        when(taskMapper.insert(any(TaskQueueTask.class))).thenReturn(1);
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(openClawService.getSavedApiUrl()).thenReturn("http://localhost:3000");

        when(restTemplate.postForObject(anyString(), any(HttpEntity.class), eq(String.class)))
            .thenReturn("{\"code\":0}");

        CreateTaskRequest request = new CreateTaskRequest();
        request.setType("agent-execute");
        request.setTitle("Subtask 1");
        request.setPayload("{\"description\": \"Decomposed work\"}");
        request.setAssignedAgent("worker-agent");
        request.setTaskGroupId("tg-123");
        request.setPriority(5);

        TaskQueueTask created = taskQueueService.createTask(request);

        assertNotNull(created);
        assertEquals("tg-123", created.getTaskGroupId());
        assertEquals("worker-agent", created.getAssignedAgent());
        assertEquals(TaskStatus.PENDING.getValue(), created.getStatus());

        verify(restTemplate, atLeastOnce()).postForObject(
            eq("http://localhost:3000/hooks/agent"),
            any(HttpEntity.class),
            eq(String.class)
        );
    }

    @Test
    void testExponentialBackoffOnSubtaskFailure() {
        sampleTask.setMaxRetries(3);
        sampleTask.setRetryCount(0);

        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));
        when(taskMapper.updateById(any(TaskQueueTask.class))).thenReturn(1);

        boolean result = taskQueueService.failTask("task-test-123", "Transient error");

        assertTrue(result);

        verify(taskMapper).updateById(argThat(task -> {
            assertEquals(1, task.getRetryCount());
            assertEquals(TaskStatus.PENDING.getValue(), task.getStatus());
            assertNotNull(task.getScheduledAt());
            assertTrue(task.getScheduledAt().isAfter(LocalDateTime.now()));
            return true;
        }));
    }

    @Test
    void testExponentialBackoffMaxRetriesReached() {
        sampleTask.setMaxRetries(3);
        sampleTask.setRetryCount(2);

        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));
        when(taskMapper.updateById(any(TaskQueueTask.class))).thenReturn(1);

        boolean result = taskQueueService.failTask("task-test-123", "Final error");

        assertTrue(result);

        verify(taskMapper).updateById(argThat(task ->
            TaskStatus.NEEDS_INTERVENTION.getValue().equals(task.getStatus())
        ));
    }

    @Test
    void testNotificationRetryOnWebhookFailure() {
        when(taskMapper.insert(any(TaskQueueTask.class))).thenReturn(1);
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));
        when(openClawService.getSavedApiUrl()).thenReturn("http://localhost:3000");

        doThrow(new RuntimeException("Webhook unavailable"))
            .when(restTemplate).postForObject(anyString(), any(HttpEntity.class), eq(String.class));

        CreateTaskRequest request = new CreateTaskRequest();
        request.setType("agent-execute");
        request.setAssignedAgent("test-agent");
        request.setPriority(5);

        TaskQueueTask created = taskQueueService.createTask(request);

        assertNotNull(created);
        assertEquals("test-agent", created.getAssignedAgent());
        assertEquals(TaskStatus.PENDING.getValue(), created.getStatus());
    }

    @Test
    void testGetAgentStats() {
        TaskQueueTask pendingTask = new TaskQueueTask();
        pendingTask.setStatus(TaskStatus.PENDING.getValue());
        pendingTask.setAssignedAgent("test-agent");

        TaskQueueTask runningTask = new TaskQueueTask();
        runningTask.setStatus(TaskStatus.RUNNING.getValue());
        runningTask.setAssignedAgent("test-agent");

        TaskQueueTask completedTask = new TaskQueueTask();
        completedTask.setStatus(TaskStatus.COMPLETED.getValue());
        completedTask.setAssignedAgent("test-agent");
        completedTask.setCompletedAt(LocalDateTime.now());

        when(taskMapper.selectList(any())).thenReturn(
            List.of(pendingTask, runningTask, completedTask)
        );

        var stats = taskQueueService.getAgentStats("test-agent");

        assertNotNull(stats);
        assertEquals(1L, stats.get("pending"));
        assertEquals(1L, stats.get("running"));
        assertEquals(1L, stats.get("completedToday"));
    }
}
