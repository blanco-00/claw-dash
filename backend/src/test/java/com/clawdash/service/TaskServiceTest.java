package com.clawdash.service;

import com.clawdash.entity.Task;
import com.clawdash.mapper.TaskMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskMapper taskMapper;

    @InjectMocks
    private TaskService taskService;

    @Test
    void testCreateTask() {
        when(taskMapper.insert(any(Task.class))).thenReturn(1);
        
        Task task = taskService.createTask("TEST", "{}", 0);
        
        assertNotNull(task.getTaskId());
        assertEquals("TEST", task.getType());
        assertEquals("PENDING", task.getStatus());
    }

    @Test
    void testClaimTask() {
        Task pendingTask = new Task();
        pendingTask.setTaskId("task-1");
        pendingTask.setStatus("PENDING");
        
        when(taskMapper.selectList(any())).thenReturn(List.of(pendingTask));
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);
        
        Task claimed = taskService.claimTask("worker-1");
        
        assertNotNull(claimed);
        assertEquals("RUNNING", claimed.getStatus());
    }

    @Test
    void testCompleteTask() {
        Task task = new Task();
        task.setTaskId("task-1");
        
        when(taskMapper.selectList(any())).thenReturn(List.of(task));
        when(taskMapper.updateById(any(Task.class))).thenReturn(1);
        
        taskService.completeTask("task-1", "{\"result\": \"success\"}");
        
        verify(taskMapper).updateById(any(Task.class));
    }

    @Test
    void testCountByStatus() {
        when(taskMapper.selectList(any())).thenReturn(List.of());
        
        long count = taskService.countByStatus("PENDING");
        
        assertEquals(0, count);
    }
}
