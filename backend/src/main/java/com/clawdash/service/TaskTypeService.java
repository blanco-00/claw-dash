package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.TaskType;
import com.clawdash.mapper.TaskTypeMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskTypeService extends ServiceImpl<TaskTypeMapper, TaskType> {

    public List<TaskType> getAllEnabled() {
        return list(new LambdaQueryWrapper<TaskType>()
                .eq(TaskType::getEnabled, true)
                .orderByAsc(TaskType::getDisplayName));
    }

    public List<TaskType> getAll() {
        return list(new LambdaQueryWrapper<TaskType>()
                .orderByAsc(TaskType::getDisplayName));
    }

    public TaskType create(TaskType taskType) {
        taskType.setCreatedAt(LocalDateTime.now());
        taskType.setUpdatedAt(LocalDateTime.now());
        if (taskType.getEnabled() == null) {
            taskType.setEnabled(true);
        }
        save(taskType);
        return taskType;
    }

    public TaskType update(Long id, TaskType taskType) {
        taskType.setId(id);
        taskType.setUpdatedAt(LocalDateTime.now());
        updateById(taskType);
        return getById(id);
    }

    public boolean delete(Long id) {
        return removeById(id);
    }
}
