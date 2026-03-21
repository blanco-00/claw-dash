package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.Agent;
import com.clawdash.service.AgentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agents")
public class AgentController {

    private final AgentService agentService;

    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @GetMapping
    public Result<List<Agent>> listAgents() {
        List<Agent> agents = agentService.list();
        return Result.success(agents);
    }

    @GetMapping("/active")
    public Result<List<Agent>> listActiveAgents() {
        List<Agent> agents = agentService.listActive();
        return Result.success(agents);
    }

    @GetMapping("/{agentId}")
    public Result<Agent> getAgent(@PathVariable String agentId) {
        Agent agent = agentService.lambdaQuery()
                .eq(Agent::getAgentId, agentId)
                .one();
        return Result.success(agent);
    }

    @PostMapping
    public Result<Agent> createAgent(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String role = request.get("role");
        String description = request.get("description");
        String config = request.get("config");

        Agent agent = agentService.createAgent(name, role, description, config);
        return Result.success(agent);
    }

    @PatchMapping("/{agentId}/status")
    public Result<Void> updateStatus(@PathVariable String agentId, @RequestBody Map<String, String> request) {
        String status = request.get("status");
        agentService.updateStatus(agentId, status);
        return Result.success();
    }
}
