package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.Task;
import com.clawdash.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @Test
    void testGetTasks() {
        when(taskService.list()).thenReturn(List.of());
        
        Result<List<Task>> response = taskController.listTasks(null);
        
        assertNotNull(response);
    }

    @Test
    void testCreateTask() {
        Task task = new Task();
        task.setTaskId("test-task-1");
        task.setType("TEST");
        
        when(taskService.createTask(any(), any(), any())).thenReturn(task);
        
        Result<?> response = taskController.createTask(java.util.Map.of(
            "type", "TEST",
            "payload", "{}",
            "priority", 0
        ));
        
        assertNotNull(response);
    }

    @Test
    void testClaimTask() {
        Task task = new Task();
        task.setTaskId("test-task-1");
        
        when(taskService.claimTask(any())).thenReturn(task);
        
        Result<?> response = taskController.claimTask(java.util.Map.of("workerId", "worker-1"));
        
        assertNotNull(response);
    }
}
