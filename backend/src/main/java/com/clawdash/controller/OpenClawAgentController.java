package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.service.OpenClawService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.Map;

/**
 * Controller for OpenClaw CLI-based agent management.
 * These are long-term agents managed via OpenClaw CLI (not database agents).
 */
@RestController
@RequestMapping("/api/openclaw/agents")
public class OpenClawAgentController {

    private final OpenClawService openClawService;

    public OpenClawAgentController(OpenClawService openClawService) {
        this.openClawService = openClawService;
    }

    /**
     * List all OpenClaw agents via CLI
     */
    @GetMapping
    public Result<List<String>> listOpenClawAgents() {
        List<String> agents = openClawService.listAgents();
        return Result.success(agents);
    }

    /**
     * Add a new OpenClaw agent via CLI
     * Optional: copy template files from an existing agent
     */
    @PostMapping
    public Result<Map<String, Object>> addOpenClawAgent(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String workspace = request.get("workspace");
        String copyFrom = request.get("copyFrom");
        
        if (name == null || name.isBlank()) {
            return Result.error("Agent name is required");
        }
        if (workspace == null || workspace.isBlank()) {
            return Result.error("Workspace path is required");
        }
        
        boolean success = openClawService.addAgent(name, workspace, copyFrom);
        if (success) {
            return Result.success(Map.of(
                "name", name, 
                "workspace", workspace, 
                "created", true,
                "copiedFrom", copyFrom != null ? copyFrom : ""
            ));
        } else {
            return Result.error("Failed to create agent");
        }
    }

    /**
     * Delete an OpenClaw agent via CLI
     */
    @DeleteMapping("/{name}")
    public Result<Void> deleteOpenClawAgent(@PathVariable String name) {
        boolean success = openClawService.deleteAgent(name);
        if (success) {
            return Result.success();
        } else {
            return Result.error("Failed to delete agent");
        }
    }

    /**
     * Bind an OpenClaw agent to a channel
     */
    @PostMapping("/{name}/bind")
    public Result<Void> bindOpenClawAgent(@PathVariable String name, @RequestBody Map<String, String> request) {
        String channel = request.get("channel");
        if (channel == null || channel.isBlank()) {
            return Result.error("Channel is required");
        }
        
        var bindResult = openClawService.bindAgent(name, channel);
        if (bindResult.getCode() != null && bindResult.getCode() == 200) {
            return Result.success();
        } else {
            return Result.error("Failed to bind agent");
        }
    }

    @GetMapping("/bindings")
    public Result<Map<String, Object>> getBindings() {
        return openClawService.getBindings();
    }

    @PostMapping("/{name}/unbind")
    public Result<Void> unbindOpenClawAgent(@PathVariable String name, @RequestBody Map<String, String> request) {
        String channel = request.get("channel");
        if (channel == null || channel.isBlank()) {
            return Result.error("Channel is required");
        }
        
        var unbindResult = openClawService.unbindAgent(name, channel);
        if (unbindResult.getCode() != null && unbindResult.getCode() == 200) {
            return Result.success();
        } else {
            return Result.error("Failed to unbind agent");
        }
    }

    /**
     * Get the main agent ID
     */
    @GetMapping("/main")
    public Result<Map<String, String>> getMainAgent() {
        String mainAgentId = openClawService.getMainAgentId();
        return Result.success(Map.of("mainAgentId", mainAgentId));
    }

    @GetMapping("/details")
    public Result<List<Map<String, String>>> listAgentsWithDetails() {
        List<Map<String, String>> agents = openClawService.listAgentsWithDetails();
        return Result.success(agents);
    }

    @GetMapping("/names")
    public Result<List<Map<String, String>>> listAgentNames() {
        List<Map<String, String>> agents = openClawService.listAgentNames();
        return Result.success(agents);
    }

    @PatchMapping("/{name}/identity")
    public Result<Map<String, Object>> updateAgentIdentity(
            @PathVariable String name,
            @RequestBody Map<String, String> request) {
        String identityName = request.get("name");
        String emoji = request.get("emoji");
        return openClawService.setIdentity(name, identityName, emoji);
    }

    @GetMapping("/{name}/files")
    public Result<List<Map<String, String>>> listAgentFiles(@PathVariable String name) {
        List<Map<String, String>> files = openClawService.listAgentFiles(name);
        return Result.success(files);
    }

    @GetMapping("/{name}/files/{filename}")
    public Result<Map<String, Object>> getAgentFileContent(
            @PathVariable String name,
            @PathVariable String filename) {
        return openClawService.getAgentFileContent(name, filename);
    }

    @PatchMapping("/{name}/files/{fileName:.+}")
    public Result<Void> saveAgentFile(
            @PathVariable String name,
            @PathVariable("fileName") String filename,
            @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null) {
            return Result.error("Content is required");
        }
        return openClawService.saveAgentFile(name, filename, content);
    }

    @GetMapping("/orphaned")
    public Result<List<String>> getOrphanedAgents() {
        List<String> orphaned = openClawService.getOrphanedAgents();
        return Result.success(orphaned);
    }

    @PostMapping("/cleanup")
    public Result<Map<String, Object>> cleanupOrphanedAgents() {
        return openClawService.cleanupOrphanedAgents();
    }

    @GetMapping("/task-bindings")
    public Result<List<Map<String, Object>>> getTaskBindings() {
        return openClawService.getTaskBindings();
    }

    @PostMapping("/task-bindings")
    public Result<Void> addTaskBinding(@RequestBody Map<String, String> request) {
        String agentName = request.get("agentName");
        String taskType = request.get("taskType");
        if (agentName == null || agentName.isBlank()) {
            return Result.error("Agent name is required");
        }
        if (taskType == null || taskType.isBlank()) {
            return Result.error("Task type is required");
        }
        return openClawService.addTaskBinding(agentName, taskType);
    }

    @DeleteMapping("/task-bindings")
    public Result<Void> removeTaskBinding(@RequestBody Map<String, String> request) {
        String agentName = request.get("agentName");
        String taskType = request.get("taskType");
        if (agentName == null || agentName.isBlank()) {
            return Result.error("Agent name is required");
        }
        if (taskType == null || taskType.isBlank()) {
            return Result.error("Task type is required");
        }
        return openClawService.removeTaskBinding(agentName, taskType);
    }

    @GetMapping("/distributor")
    public Result<Map<String, String>> getDistributor() {
        return openClawService.getDistributor();
    }

    @PostMapping("/distributor")
    public Result<Void> setDistributor(@RequestBody Map<String, String> request) {
        String agentName = request.get("agentName");
        if (agentName == null || agentName.isBlank()) {
            return Result.error("Agent name is required");
        }
        return openClawService.setDistributor(agentName);
    }
}
