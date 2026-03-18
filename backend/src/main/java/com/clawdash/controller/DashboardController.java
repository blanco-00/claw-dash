package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private AgentService agentService;

    @Autowired
    private CronJobService cronJobService;

    @Autowired
    private TaskGroupService taskGroupService;

    @GetMapping("/overview")
    public Result<Map<String, Object>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        overview.put("totalTasks", taskService.countTotal());
        overview.put("pendingTasks", taskService.countByStatus("PENDING"));
        overview.put("processingTasks", taskService.countByStatus("PROCESSING"));
        overview.put("completedTasks", taskService.countByStatus("COMPLETED"));
        overview.put("failedTasks", taskService.countByStatus("FAILED"));
        
        overview.put("totalAgents", agentService.count());
        overview.put("activeAgents", agentService.countByStatus("ACTIVE"));
        
        overview.put("activeCronJobs", cronJobService.getEnabledJobs().size());
        overview.put("taskGroups", taskGroupService.count());
        
        overview.put("timestamp", LocalDateTime.now().toString());
        
        return Result.success(overview);
    }

    @GetMapping("/stats")
    public Result<Map<String, Object>> getStats() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("tasksToday", taskService.countToday());
        stats.put("tasksThisWeek", taskService.countThisWeek());
        stats.put("tasksThisMonth", taskService.countThisMonth());
        
        Map<String, Object> successRate = new HashMap<>();
        long total = taskService.count();
        long completed = taskService.countByStatus("COMPLETED");
        long failed = taskService.countByStatus("FAILED");
        successRate.put("total", total);
        successRate.put("completed", completed);
        successRate.put("failed", failed);
        successRate.put("rate", total > 0 ? (double) completed / total * 100 : 0);
        stats.put("successRate", successRate);
        
        return Result.success(stats);
    }
}
