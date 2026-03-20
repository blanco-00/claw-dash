package com.clawdash.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.clawdash.common.Result;
import com.clawdash.entity.ConfigGraph;
import com.clawdash.entity.ConfigGraphNode;
import com.clawdash.entity.ConfigGraphEdge;
import com.clawdash.mapper.ConfigGraphMapper;
import com.clawdash.mapper.ConfigGraphNodeMapper;
import com.clawdash.mapper.ConfigGraphEdgeMapper;
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
        return Result.success(null);
    }

    @PutMapping("/{id}/nodes/{agentId}/position")
    public Result<Void> updateNodePosition(@PathVariable Long id, @PathVariable String agentId, @RequestBody Map<String, Double> body) {
        Double x = body.get("x");
        Double y = body.get("y");
        configGraphService.updateNodePosition(id, agentId, x, y);
        return Result.success(null);
    }

    @PostMapping("/{id}/edges")
    public Result<ConfigGraphEdge> addEdge(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String sourceId = body.get("sourceId");
        String targetId = body.get("targetId");
        String edgeType = body.get("edgeType");
        String label = body.get("label");
        ConfigGraphEdge edge = configGraphService.addEdge(id, sourceId, targetId, edgeType, label);
        return Result.success(edge);
    }

    @PutMapping("/{id}/edges/{edgeId}")
    public Result<Void> updateEdge(@PathVariable Long id, @PathVariable Long edgeId, @RequestBody Map<String, Object> body) {
        Boolean enabled = body.get("enabled") != null ? (Boolean) body.get("enabled") : null;
        String label = (String) body.get("label");
        configGraphService.updateEdge(edgeId, enabled, label);
        return Result.success(null);
    }

    @DeleteMapping("/{id}/edges/{edgeId}")
    public Result<Void> removeEdge(@PathVariable Long id, @PathVariable Long edgeId) {
        configGraphService.removeEdge(edgeId);
        return Result.success(null);
    }

    @PostMapping("/{id}/sync")
    public Result<Map<String, Object>> sync(@PathVariable Long id) {
        Map<String, Object> result = new HashMap<>();
        
        List<ConfigGraphEdge> edges = edgeMapper.selectList(
                new LambdaQueryWrapper<ConfigGraphEdge>()
                        .eq(ConfigGraphEdge::getGraphId, id)
                        .eq(ConfigGraphEdge::getEnabled, true)
        );
        
        int synced = 0;
        int failed = 0;
        
        for (ConfigGraphEdge edge : edges) {
            String channel = "channel:" + edge.getTargetId();
            Result<Map<String, Object>> bindResult = openClawService.bindAgent(edge.getSourceId(), channel);
            if (bindResult.getCode() != null && bindResult.getCode() == 200) {
                synced++;
            } else {
                failed++;
            }
        }
        
        ConfigGraph graph = graphMapper.selectById(id);
        if (graph != null) {
            graph.setLastSyncedAt(LocalDateTime.now());
            graphMapper.updateById(graph);
        }
        
        result.put("synced", synced);
        result.put("failed", failed);
        result.put("total", edges.size());
        return Result.success(result);
    }
}
