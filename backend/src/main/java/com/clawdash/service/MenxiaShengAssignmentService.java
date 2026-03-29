package com.clawdash.service;

import com.clawdash.dto.DecomposedTask;
import com.clawdash.dto.ParsedTask;
import com.clawdash.entity.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MenxiaShengAssignmentService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private MenxiaShengDecompositionService decompositionService;

    @Autowired
    private AgentSelector agentSelector;

    @Autowired
    private A2AMessageService a2AMessageService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TaskGroupService taskGroupService;

    private static final String MENXIASHENG_AGENT_ID = "menxiasheng";

    @Transactional
    public AssignmentResult assignTask(ParsedTask parsedTask, String fromAgentId) {
        boolean isComplex = decompositionService.isComplexTask(parsedTask);

        if (isComplex) {
            return assignComplexTask(parsedTask, fromAgentId);
        } else {
            return assignSimpleTask(parsedTask, fromAgentId);
        }
    }

    protected AssignmentResult assignSimpleTask(ParsedTask parsedTask, String fromAgentId) {
        String assignedAgent = agentSelector.selectAgent(parsedTask.getTitle());
        
        Task task = createTask(parsedTask, assignedAgent, null, null);
        
        notifyAssignment(task, assignedAgent, fromAgentId);
        
        AssignmentResult result = new AssignmentResult();
        result.setTaskId(task.getTaskId());
        result.setAssignedAgent(assignedAgent);
        result.setComplex(false);
        result.setMessage("任务已分配给 " + assignedAgent);
        
        return result;
    }

    protected AssignmentResult assignComplexTask(ParsedTask parsedTask, String fromAgentId) {
        DecomposedTask decomposed = decompositionService.decompose(parsedTask);
        
        com.clawdash.entity.TaskGroup taskGroup = new com.clawdash.entity.TaskGroup();
        taskGroup.setName(parsedTask.getTitle());
        taskGroup.setDescription(parsedTask.getDescription());
        taskGroup.setStatus("IN_PROGRESS");
        taskGroup.setCreatedAt(LocalDateTime.now());
        taskGroup.setUpdatedAt(LocalDateTime.now());
        taskGroupService.save(taskGroup);
        
        Long groupId = taskGroup.getId();
        String groupIdStr = String.valueOf(groupId);
        
        List<String> subtaskIds = new ArrayList<>();
        Map<String, String> subtaskIdMap = new HashMap<>();
        
        for (int i = 0; i < decomposed.getSubtasks().size(); i++) {
            DecomposedTask.SubtaskDef subtaskDef = decomposed.getSubtasks().get(i);
            
            String subtaskId = "subtask-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
            subtaskIdMap.put("subtask-" + i, subtaskId);
            
            List<String> resolvedDependsOn = new ArrayList<>();
            for (String dep : subtaskDef.getDependsOn()) {
                String resolvedId = subtaskIdMap.get(dep);
                if (resolvedId != null) {
                    resolvedDependsOn.add(resolvedId);
                }
            }
            
            Task subtask = createSubtask(
                subtaskDef,
                subtaskId,
                groupIdStr,
                null,
                resolvedDependsOn,
                parsedTask.getPriority()
            );
            
            subtaskIds.add(subtask.getTaskId());
            
            notifyAssignment(subtask, subtaskDef.getAssignedAgent(), MENXIASHENG_AGENT_ID);
        }
        
        AssignmentResult result = new AssignmentResult();
        result.setTaskGroupId(groupIdStr);
        result.setSubtaskIds(subtaskIds);
        result.setComplex(true);
        result.setMessage("复杂任务已拆解为 " + subtaskIds.size() + " 个子任务");
        
        return result;
    }

    protected Task createTask(ParsedTask parsedTask, String assignedAgent, String groupId, String parentId) {
        Task task = new Task();
        task.setTaskId("task-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        task.setType("user_task");
        task.setPayload(buildPayload(parsedTask));
        task.setPriority(priorityToInt(parsedTask.getPriority()));
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setMaxRetries(3);
        task.setCreatedAt(LocalDateTime.now());
        
        taskService.save(task);
        
        return task;
    }

    protected Task createSubtask(DecomposedTask.SubtaskDef subtaskDef, String taskId, 
                                  String groupId, String parentId, List<String> dependsOn, String priority) {
        Task task = new Task();
        task.setTaskId(taskId);
        task.setType("subtask");
        
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", subtaskDef.getTitle());
            payload.put("description", subtaskDef.getDescription());
            payload.put("context", subtaskDef.getContext());
            task.setPayload(objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            task.setPayload(subtaskDef.getDescription());
        }
        
        task.setPriority(priorityToInt(priority));
        task.setStatus("PENDING");
        task.setRetryCount(0);
        task.setMaxRetries(3);
        task.setCreatedAt(LocalDateTime.now());
        
        if (dependsOn != null && !dependsOn.isEmpty()) {
            try {
                task.setDependsOn(objectMapper.writeValueAsString(dependsOn));
            } catch (JsonProcessingException e) {
                task.setDependsOn(String.join(",", dependsOn));
            }
        }
        
        taskService.save(task);
        
        return task;
    }

    protected void notifyAssignment(Task task, String assignedAgent, String fromAgentId) {
        a2AMessageService.logMessage(
                MENXIASHENG_AGENT_ID,
                assignedAgent,
                "新任务分配: " + task.getTaskId(),
                "TASK_ASSIGNED"
        );
    }

    protected String buildPayload(ParsedTask parsedTask) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", parsedTask.getTitle());
            payload.put("description", parsedTask.getDescription());
            payload.put("rawInput", parsedTask.getRawInput());
            payload.put("priority", parsedTask.getPriority());
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            return parsedTask.getDescription();
        }
    }

    protected int priorityToInt(String priority) {
        if (priority == null) {
            return 5;
        }
        return switch (priority.toLowerCase()) {
            case "urgent" -> 10;
            case "high" -> 8;
            case "low" -> 2;
            default -> 5;
        };
    }

    public static class AssignmentResult {
        private String taskId;
        private String taskGroupId;
        private List<String> subtaskIds;
        private String assignedAgent;
        private boolean complex;
        private String message;

        public String getTaskId() {
            return taskId;
        }

        public void setTaskId(String taskId) {
            this.taskId = taskId;
        }

        public String getTaskGroupId() {
            return taskGroupId;
        }

        public void setTaskGroupId(String taskGroupId) {
            this.taskGroupId = taskGroupId;
        }

        public List<String> getSubtaskIds() {
            return subtaskIds;
        }

        public void setSubtaskIds(List<String> subtaskIds) {
            this.subtaskIds = subtaskIds;
        }

        public String getAssignedAgent() {
            return assignedAgent;
        }

        public void setAssignedAgent(String assignedAgent) {
            this.assignedAgent = assignedAgent;
        }

        public boolean isComplex() {
            return complex;
        }

        public void setComplex(boolean complex) {
            this.complex = complex;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
