package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.service.OpenClawService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller for OpenClaw CLI-based agent management.
 * These are long-term agents managed via OpenClaw CLI (not database agents).
 */
@RestController
@RequestMapping("/api/openclaw/agents")
@RequiredArgsConstructor
public class OpenClawAgentController {

    private final OpenClawService openClawService;

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
     */
    @PostMapping
    public Result<Map<String, Object>> addOpenClawAgent(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String workspace = request.get("workspace");
        
        if (name == null || name.isBlank()) {
            return Result.error("Agent name is required");
        }
        if (workspace == null || workspace.isBlank()) {
            return Result.error("Workspace path is required");
        }
        
        boolean success = openClawService.addAgent(name, workspace);
        if (success) {
            return Result.success(Map.of("name", name, "workspace", workspace, "created", true));
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
        
        boolean success = openClawService.bindAgent(name, channel);
        if (success) {
            return Result.success();
        } else {
            return Result.error("Failed to bind agent");
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
}
