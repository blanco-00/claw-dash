package com.clawdash.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawdash.entity.TaskType;
import com.clawdash.mapper.TaskTypeMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class TaskTypeInitializer implements CommandLineRunner {

    private final TaskTypeMapper taskTypeMapper;

    public TaskTypeInitializer(TaskTypeMapper taskTypeMapper) {
        this.taskTypeMapper = taskTypeMapper;
    }

    @Override
    public void run(String... args) {
        // Check if task types already exist
        Long count = taskTypeMapper.selectCount(new LambdaQueryWrapper<>());
        
        if (count == 0) {
            // Seed default task types
            List<TaskType> defaultTypes = Arrays.asList(
                    createTaskType("agent-execute", "Agent Execute", "Execute an agent task"),
                    createTaskType("data-sync", "Data Sync", "Synchronize data between systems"),
                    createTaskType("notification", "Notification", "Send notification to users"),
                    createTaskType("cleanup", "Cleanup", "Clean up old data or resources")
            );
            
            defaultTypes.forEach(taskTypeMapper::insert);
        }
    }

    private TaskType createTaskType(String name, String displayName, String description) {
        TaskType taskType = new TaskType();
        taskType.setName(name);
        taskType.setDisplayName(displayName);
        taskType.setDescription(description);
        taskType.setEnabled(true);
        taskType.setCreatedAt(LocalDateTime.now());
        taskType.setUpdatedAt(LocalDateTime.now());
        return taskType;
    }
}
