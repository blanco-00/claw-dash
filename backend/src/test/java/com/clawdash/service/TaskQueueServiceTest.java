package com.clawdash.service;

import com.clawdash.common.PageResponse;
import com.clawdash.dto.CreateTaskRequest;
import com.clawdash.entity.TaskQueueTask;
import com.clawdash.entity.TaskStatus;
import com.clawdash.mapper.TaskQueueTaskMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskQueueServiceTest {

    @Mock
    private TaskQueueTaskMapper taskMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TaskQueueService taskQueueService;

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
        when(taskMapper.insert(any(TaskQueueTask.class))).thenReturn(1);
        when(redisTemplate.opsForList()).thenReturn(mock(ListOperations.class));

        CreateTaskRequest request = new CreateTaskRequest();
        request.setType("agent-execute");
        request.setPayload("{\"agent\": \"test\"}");
        request.setPriority(5);
        request.setMaxRetries(3);

        TaskQueueTask created = taskQueueService.createTask(request);

        assertNotNull(created);
        assertNotNull(created.getTaskId());
        assertTrue(created.getTaskId().startsWith("task-"));
        assertEquals("agent-execute", created.getType());
        assertEquals(TaskStatus.PENDING.getValue(), created.getStatus());
        assertEquals(0, created.getRetryCount());
    }

    @Test
    void testClaimTask_Success() {
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));
        when(taskMapper.updateById(any(TaskQueueTask.class))).thenReturn(1);
        when(redisTemplate.delete(anyString())).thenReturn(true);

        TaskQueueTask claimed = taskQueueService.claimTask("task-test-123", "worker-1");

        assertNotNull(claimed);
        assertEquals(TaskStatus.RUNNING.getValue(), claimed.getStatus());
        assertEquals("worker-1", claimed.getClaimedBy());
    }

    @Test
    void testClaimTask_AlreadyClaimed() {
        sampleTask.setStatus(TaskStatus.RUNNING.getValue());
        
        when(redisTemplate.opsForValue()).thenReturn(mock(ValueOperations.class));
        when(redisTemplate.opsForValue().setIfAbsent(anyString(), anyString(), anyLong(), any())).thenReturn(true);
        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));

        TaskQueueTask claimed = taskQueueService.claimTask("task-test-123", "worker-2");

        assertNull(claimed);
    }

    @Test
    void testCompleteTask_Success() {
        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));
        when(taskMapper.updateById(any(TaskQueueTask.class))).thenReturn(1);

        boolean result = taskQueueService.completeTask("task-test-123", "{\"success\": true}");

        assertTrue(result);
        verify(taskMapper).updateById(argThat(task -> 
            TaskStatus.COMPLETED.getValue().equals(task.getStatus())
        ));
    }

    @Test
    void testFailTask_Retry() {
        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));
        when(taskMapper.updateById(any(TaskQueueTask.class))).thenReturn(1);

        boolean result = taskQueueService.failTask("task-test-123", "Error message");

        assertTrue(result);
        verify(taskMapper).updateById(argThat(task -> 
            task.getRetryCount() == 1 && 
            TaskStatus.PENDING.getValue().equals(task.getStatus())
        ));
    }

    @Test
    void testFailTask_MaxRetriesReached() {
        sampleTask.setRetryCount(2);
        
        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));
        when(taskMapper.updateById(any(TaskQueueTask.class))).thenReturn(1);

        boolean result = taskQueueService.failTask("task-test-123", "Error message");

        assertTrue(result);
        verify(taskMapper).updateById(argThat(task -> 
            TaskStatus.DEAD.getValue().equals(task.getStatus())
        ));
    }

    @Test
    void testGetTaskByTaskId() {
        when(taskMapper.selectList(any())).thenReturn(List.of(sampleTask));

        TaskQueueTask found = taskQueueService.getTaskByTaskId("task-test-123");

        assertNotNull(found);
        assertEquals("task-test-123", found.getTaskId());
    }

    @Test
    void testGetTaskByTaskId_NotFound() {
        when(taskMapper.selectList(any())).thenReturn(List.of());

        TaskQueueTask found = taskQueueService.getTaskByTaskId("nonexistent");

        assertNull(found);
    }
}
