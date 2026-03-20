package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.service.RuntimeGraphService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/runtime-graphs")
public class RuntimeGraphController {

    @Autowired
    private RuntimeGraphService runtimeGraphService;

    @GetMapping("/current")
    public Result<Map<String, Object>> getCurrentRuntimeGraph() {
        Map<String, Object> graph = runtimeGraphService.getCurrentRuntimeGraph();
        return Result.success(graph);
    }

    @GetMapping("/history")
    public Result<Map<String, Object>> getRuntimeGraphHistory(
            @RequestParam(required = false, defaultValue = "1h") String timeRange) {
        Map<String, Object> history = runtimeGraphService.getRuntimeGraphHistory(timeRange);
        return Result.success(history);
    }
}
