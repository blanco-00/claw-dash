package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.ConfigGraph;
import com.clawdash.entity.ConfigGraphNode;
import com.clawdash.entity.ConfigGraphEdge;
import com.clawdash.mapper.ConfigGraphMapper;
import com.clawdash.mapper.ConfigGraphNodeMapper;
import com.clawdash.mapper.ConfigGraphEdgeMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfigGraphService extends ServiceImpl<ConfigGraphMapper, ConfigGraph> {

    @Autowired
    private ConfigGraphNodeMapper nodeMapper;

    @Autowired
    private ConfigGraphEdgeMapper edgeMapper;

    public ConfigGraph createDefaultGraph() {
        ConfigGraph graph = new ConfigGraph();
        graph.setName("Default Graph");
        graph.setVersion(1);
        save(graph);
        return graph;
    }

    public Map<String, Object> getGraphWithDetails(Long graphId) {
        Map<String, Object> result = new HashMap<>();
        
        ConfigGraph graph = getById(graphId);
        if (graph == null) {
            return null;
        }
        
        List<ConfigGraphNode> nodes = nodeMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigGraphNode>()
                .eq(ConfigGraphNode::getGraphId, graphId)
        );
        
        List<ConfigGraphEdge> edges = edgeMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigGraphEdge>()
                .eq(ConfigGraphEdge::getGraphId, graphId)
        );
        
        result.put("graph", graph);
        result.put("nodes", nodes);
        result.put("edges", edges);
        return result;
    }

    public ConfigGraphNode addNode(Long graphId, String agentId, Double x, Double y) {
        ConfigGraphNode node = new ConfigGraphNode();
        node.setGraphId(graphId);
        node.setAgentId(agentId);
        node.setX(x);
        node.setY(y);
        node.setCollapsed(false);
        nodeMapper.insert(node);
        return node;
    }

    public void removeNode(Long graphId, String agentId) {
        // First remove all edges connected to this node
        edgeMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigGraphEdge>()
                .eq(ConfigGraphEdge::getGraphId, graphId)
                .and(wrapper -> wrapper
                    .eq(ConfigGraphEdge::getSourceId, agentId)
                    .or()
                    .eq(ConfigGraphEdge::getTargetId, agentId)
                )
        );
        
        // Then remove the node
        nodeMapper.delete(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigGraphNode>()
                .eq(ConfigGraphNode::getGraphId, graphId)
                .eq(ConfigGraphNode::getAgentId, agentId)
        );
    }

    public void updateNodePosition(Long graphId, String agentId, Double x, Double y) {
        ConfigGraphNode node = nodeMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigGraphNode>()
                .eq(ConfigGraphNode::getGraphId, graphId)
                .eq(ConfigGraphNode::getAgentId, agentId)
        );
        if (node != null) {
            node.setX(x);
            node.setY(y);
            nodeMapper.updateById(node);
        }
    }

    public void updateAllNodePositions(Long graphId, List<Map<String, Object>> positions) {
        for (Map<String, Object> pos : positions) {
            String agentId = (String) pos.get("id");
            Number xNum = (Number) pos.get("x");
            Number yNum = (Number) pos.get("y");
            Double x = xNum != null ? xNum.doubleValue() : null;
            Double y = yNum != null ? yNum.doubleValue() : null;
            
            if (agentId == null || x == null || y == null) {
                continue;
            }
            
            ConfigGraphNode node = nodeMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<ConfigGraphNode>()
                    .eq(ConfigGraphNode::getGraphId, graphId)
                    .eq(ConfigGraphNode::getAgentId, agentId)
            );
            
            if (node != null) {
                node.setX(x);
                node.setY(y);
                nodeMapper.updateById(node);
            } else {
                ConfigGraphNode newNode = new ConfigGraphNode();
                newNode.setGraphId(graphId);
                newNode.setAgentId(agentId);
                newNode.setX(x);
                newNode.setY(y);
                newNode.setCollapsed(false);
                nodeMapper.insert(newNode);
            }
        }
    }

    public ConfigGraphEdge addEdge(Long graphId, String sourceId, String targetId, String edgeType, String label, String decisionMode, String messageTemplate, String sourceHandle, String targetHandle) {
        ConfigGraphEdge edge = new ConfigGraphEdge();
        edge.setGraphId(graphId);
        edge.setSourceId(sourceId);
        edge.setTargetId(targetId);
        edge.setEdgeType(edgeType);
        edge.setLabel(label);
        edge.setDecisionMode(decisionMode);
        edge.setMessageTemplate(messageTemplate);
        edge.setSourceHandle(sourceHandle);
        edge.setTargetHandle(targetHandle);
        edge.setEnabled(true);
        edgeMapper.insert(edge);
        return edge;
    }

    public ConfigGraphEdge getEdgeById(Long edgeId) {
        return edgeMapper.selectById(edgeId);
    }

    public void updateEdge(Long edgeId, Boolean enabled, String label, String edgeType, String decisionMode, String messageTemplate) {
        ConfigGraphEdge edge = edgeMapper.selectById(edgeId);
        if (edge != null) {
            if (enabled != null) {
                edge.setEnabled(enabled);
            }
            if (label != null) {
                edge.setLabel(label);
            }
            if (edgeType != null) {
                edge.setEdgeType(edgeType);
            }
            if (decisionMode != null) {
                edge.setDecisionMode(decisionMode);
            }
            if (messageTemplate != null) {
                edge.setMessageTemplate(messageTemplate);
            }
            edgeMapper.updateById(edge);
        }
    }

    public void removeEdge(Long edgeId) {
        edgeMapper.deleteById(edgeId);
    }
}
