package com.clawdash.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RuntimeGraphService {

    @Autowired
    private OpenClawService openClawService;

    public Map<String, Object> getCurrentRuntimeGraph() {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        List<Map<String, Object>> edges = new ArrayList<>();
        Set<String> seenAgents = new HashSet<>();
        Set<String> seenEdges = new HashSet<>();

        var bindingsResult = openClawService.getBindings();
        
        if (bindingsResult.getCode() != null && bindingsResult.getCode() == 200 && bindingsResult.getData() != null) {
            Map<String, Object> data = bindingsResult.getData();
            List<Map<String, Object>> bindings = (List<Map<String, Object>>) data.get("bindings");
            
            if (bindings != null) {
                for (Map<String, Object> binding : bindings) {
                    String agentName = (String) binding.get("agent");
                    String channels = (String) binding.get("channels");
                    String status = (String) binding.getOrDefault("status", "active");

                    if (agentName != null && !agentName.isEmpty() && !seenAgents.contains(agentName)) {
                        seenAgents.add(agentName);
                        Map<String, Object> node = new HashMap<>();
                        node.put("id", agentName);
                        node.put("name", agentName);
                        node.put("status", status);
                        node.put("type", "runtime");
                        nodes.add(node);
                    }

                    if (channels != null && !channels.isEmpty()) {
                        String[] channelArray = channels.split(",");
                        for (String channel : channelArray) {
                            channel = channel.trim();
                            if (channel.startsWith("channel:")) {
                                String targetAgent = channel.substring("channel:".length());
                                
                                String edgeId = agentName + "->" + targetAgent;
                                if (!seenEdges.contains(edgeId) && seenAgents.contains(agentName)) {
                                    seenEdges.add(edgeId);
                                    Map<String, Object> edge = new HashMap<>();
                                    edge.put("id", edgeId);
                                    edge.put("source", agentName);
                                    edge.put("target", targetAgent);
                                    edge.put("type", "runtime");
                                    edge.put("status", status);
                                    edges.add(edge);
                                }
                            }
                        }
                    }
                }
            }
        }

        result.put("nodes", nodes);
        result.put("edges", edges);
        result.put("timestamp", new Date().toString());
        
        return result;
    }

    public Map<String, Object> getRuntimeGraphHistory(String timeRange) {
        Map<String, Object> result = new HashMap<>();
        result.put("timeRange", timeRange);
        result.put("message", "History view not yet implemented - requires A2A Gateway Plugin");
        return result;
    }
}
