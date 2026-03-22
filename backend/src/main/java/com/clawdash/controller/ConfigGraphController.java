package com.clawdash.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawdash.common.Result;
import com.clawdash.dto.SyncPreviewResult;
import com.clawdash.dto.SyncResult;
import com.clawdash.entity.ConfigGraph;
import com.clawdash.entity.ConfigGraphEdge;
import com.clawdash.entity.ConfigGraphNode;
import com.clawdash.mapper.ConfigGraphEdgeMapper;
import com.clawdash.mapper.ConfigGraphMapper;
import com.clawdash.mapper.ConfigGraphNodeMapper;
import com.clawdash.service.AgentsMdSyncService;
import com.clawdash.service.ConfigGraphService;
import com.clawdash.service.OpenClawService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config-graphs")
public class ConfigGraphController {

    @Autowired
    private ConfigGraphService configGraphService;

    @Autowired
    private ConfigGraphMapper graphMapper;

    @Autowired
    private ConfigGraphNodeMapper nodeMapper;

    @Autowired
    private ConfigGraphEdgeMapper edgeMapper;

    @Autowired
    private OpenClawService openClawService;

    @Autowired
    private AgentsMdSyncService agentsMdSyncService;

    @GetMapping
    public Result<List<ConfigGraph>> list() {
        List<ConfigGraph> graphs = graphMapper.selectList(null);
        return Result.success(graphs);
    }

    @PostMapping
    public Result<ConfigGraph> create(@RequestBody Map<String, String> body) {
        ConfigGraph graph = new ConfigGraph();
        graph.setName(body.getOrDefault("name", "Default Graph"));
        graph.setDescription(body.getOrDefault("description", ""));
        graph.setVersion(1);
        graphMapper.insert(graph);
        return Result.success(graph);
    }

    @GetMapping("/{id}")
    public Result<Map<String, Object>> get(@PathVariable Long id) {
        Map<String, Object> data = configGraphService.getGraphWithDetails(id);
        if (data == null) {
            return Result.error(404, "Graph not found");
        }
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<ConfigGraph> update(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ConfigGraph graph = graphMapper.selectById(id);
        if (graph == null) {
            return Result.error(404, "Graph not found");
        }
        if (body.containsKey("name")) {
            graph.setName(body.get("name"));
        }
        if (body.containsKey("description")) {
            graph.setDescription(body.get("description"));
        }
        graphMapper.updateById(graph);
        return Result.success(graph);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        edgeMapper.delete(new LambdaQueryWrapper<ConfigGraphEdge>().eq(ConfigGraphEdge::getGraphId, id));
        nodeMapper.delete(new LambdaQueryWrapper<ConfigGraphNode>().eq(ConfigGraphNode::getGraphId, id));
        graphMapper.deleteById(id);
        return Result.success(null);
    }

    @PostMapping("/{id}/nodes")
    public Result<ConfigGraphNode> addNode(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String agentId = (String) body.get("agentId");
        Double x = body.get("x") != null ? ((Number) body.get("x")).doubleValue() : 0.0;
        Double y = body.get("y") != null ? ((Number) body.get("y")).doubleValue() : 0.0;
        ConfigGraphNode node = configGraphService.addNode(id, agentId, x, y);
        return Result.success(node);
    }

    @DeleteMapping("/{id}/nodes/{agentId}")
    public Result<Void> removeNode(@PathVariable Long id, @PathVariable String agentId) {
        configGraphService.removeNode(id, agentId);
        agentsMdSyncService.syncAll(id);
        openClawService.syncA2AConfigFromGraph(id);
        return Result.success(null);
    }

    @PutMapping("/{id}/nodes/{agentId}/position")
    public Result<Void> updateNodePosition(@PathVariable Long id, @PathVariable String agentId, @RequestBody Map<String, Double> body) {
        Double x = body.get("x");
        Double y = body.get("y");
        configGraphService.updateNodePosition(id, agentId, x, y);
        return Result.success(null);
    }

    @PutMapping("/{id}/nodes/positions")
    public Result<Void> updateAllNodePositions(@PathVariable Long id, @RequestBody List<Map<String, Object>> positions) {
        configGraphService.updateAllNodePositions(id, positions);
        return Result.success(null);
    }

    @PostMapping("/{id}/edges")
    public Result<ConfigGraphEdge> addEdge(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String sourceId = (String) body.get("sourceId");
        String targetId = (String) body.get("targetId");
        String edgeType = (String) body.get("edgeType");
        String label = (String) body.get("label");
        String decisionMode = (String) body.get("decisionMode");
        String messageTemplate = (String) body.get("messageTemplate");
        ConfigGraphEdge edge = configGraphService.addEdge(id, sourceId, targetId, edgeType, label, decisionMode, messageTemplate);
        return Result.success(edge);
    }

    @PutMapping("/{id}/edges/{edgeId}")
    public Result<Void> updateEdge(@PathVariable Long id, @PathVariable Long edgeId, @RequestBody Map<String, Object> body) {
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : null;
        String label = (String) body.get("label");
        String edgeType = (String) body.get("edgeRoutingType");
        String decisionMode = (String) body.get("decisionMode");
        String messageTemplate = (String) body.get("messageTemplate");
        configGraphService.updateEdge(edgeId, enabled, label, edgeType, decisionMode, messageTemplate);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/edges/{edgeId}")
    public Result<Void> removeEdge(@PathVariable Long id, @PathVariable Long edgeId) {
        configGraphService.removeEdge(edgeId);
        agentsMdSyncService.syncAll(id);
        openClawService.syncA2AConfigFromGraph(id);
        return Result.success(null);
    }

    @GetMapping("/{id}/sync-preview")
    public Result<SyncPreviewResult> syncPreview(@PathVariable Long id) {
        SyncPreviewResult result = agentsMdSyncService.syncPreview(id);
        return Result.success(result);
    }

    @PostMapping("/{id}/sync")
    public Result<SyncResult> sync(@PathVariable Long id) {
        SyncResult result = agentsMdSyncService.syncAll(id);
        return Result.success(result);
    }
}
