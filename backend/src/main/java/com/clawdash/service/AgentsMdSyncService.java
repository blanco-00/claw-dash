package com.clawdash.service;

import com.clawdash.dto.SyncPreviewResult;
import com.clawdash.dto.SyncResult;
import com.clawdash.entity.ConfigGraphEdge;
import com.clawdash.entity.ConfigGraphNode;
import com.clawdash.mapper.ConfigGraphEdgeMapper;
import com.clawdash.mapper.ConfigGraphNodeMapper;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AgentsMdSyncService {
    private static final Logger log = LoggerFactory.getLogger(AgentsMdSyncService.class);
    private static final String BLOCK_END = "<!-- CLAWDASH:BLOCK END -->";
    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "<!--\\s*CLAWDASH:BLOCK[^>]*id=\"([^\"]+)\"[^>]*-->\\s*([\\s\\S]*?)<!--\\s*CLAWDASH:BLOCK END -->",
            Pattern.DOTALL
    );

    @Autowired
    private ConfigGraphEdgeMapper edgeMapper;

    @Autowired
    private ConfigGraphNodeMapper nodeMapper;

    public Map<String, String> parseExistingBlocks(String content) {
        Map<String, String> blocks = new HashMap<>();
        if (content == null || content.isEmpty()) {
            return blocks;
        }
        Matcher matcher = BLOCK_PATTERN.matcher(content);
        while (matcher.find()) {
            String blockId = matcher.group(1);
            String fullBlock = matcher.group(0);
            blocks.put(blockId, fullBlock);
        }
        return blocks;
    }

    public String generateBlock(ConfigGraphEdge edge) {
        StringBuilder sb = new StringBuilder();
        String edgeType = edge.getEdgeType() != null ? edge.getEdgeType() : "always";
        String decisionMode = edge.getDecisionMode() != null ? edge.getDecisionMode() : "always";
        String messageTemplate = edge.getMessageTemplate();
        String targetAgent = edge.getTargetId();
        
        sb.append("<!-- CLAWDASH:BLOCK id=\"edge-").append(edge.getId())
          .append("\" edge_type=\"").append(edgeType)
          .append("\" decision=\"").append(decisionMode).append("\" -->\n");
        
        String typeName = getEdgeTypeName(edgeType);
        sb.append("## [CLAWDASH] ").append(typeName).append(" → ").append(targetAgent);
        if ("llm".equals(decisionMode)) {
            sb.append(" (LLM Judged)");
        }
        sb.append("\n\n");
        
        sb.append("Type: ").append(getEdgeTypeFullDescription(edgeType)).append("\n");
        sb.append("Decision: ").append(getDecisionDescription(edgeType, decisionMode)).append("\n");
        
        if ("llm".equals(decisionMode)) {
            sb.append(getLlmInstruction(edgeType, targetAgent)).append("\n");
        }
        
        if (messageTemplate != null && !messageTemplate.isEmpty()) {
            sb.append("Message: ").append(messageTemplate).append("\n");
        }
        
        sb.append("\n").append(BLOCK_END);
        return sb.toString();
    }

    private String getEdgeTypeName(String edgeType) {
        switch (edgeType) {
            case "task": return "Task";
            case "reply": return "Reply";
            case "error": return "Error";
            case "always": return "Always";
            default: return edgeType;
        }
    }

    private String getEdgeTypeFullDescription(String edgeType) {
        switch (edgeType) {
            case "task": return "任务 (委托任务)";
            case "reply": return "回复 (完成任务后回复)";
            case "error": return "错误 (发生错误时通知)";
            case "always": return "始终 (无条件发送)";
            default: return edgeType;
        }
    }

    private String getDecisionDescription(String edgeType, String decisionMode) {
        if ("llm".equals(decisionMode)) {
            return "由 AI 判断是否发送此路由，以及发送什么内容";
        }
        return "直接发送，无条件";
    }

    private String getLlmInstruction(String edgeType, String targetAgent) {
        StringBuilder sb = new StringBuilder();
        if ("task".equals(edgeType)) {
            sb.append("如果判断需要发送给 ").append(targetAgent);
            sb.append("，使用 sessions_send 发送任务消息。");
        } else if ("reply".equals(edgeType)) {
            sb.append("如果判断需要回复给 ").append(targetAgent);
            sb.append("，使用 sessions_send 发送回复消息。");
        } else if ("error".equals(edgeType)) {
            sb.append("如果判断需要通知 ").append(targetAgent);
            sb.append("，使用 sessions_send 发送错误通知。");
        } else {
            sb.append("如果判断需要发送消息给 ").append(targetAgent);
            sb.append("，使用 sessions_send 发送。");
        }
        return sb.toString();
    }

    public boolean syncToAgent(String agentId, List<ConfigGraphEdge> edges) {
        Path agentsMdPath = getAgentsMdPath(agentId);
        try {
            String existingContent = Files.exists(agentsMdPath) 
                ? Files.readString(agentsMdPath, StandardCharsets.UTF_8) 
                : "";
            Map<String, String> existingBlocks = parseExistingBlocks(existingContent);
            Set<String> existingBlockIds = existingBlocks.keySet();
            Set<String> newBlockIds = new HashSet<>();
            StringBuilder newContent = new StringBuilder(existingContent);
            for (ConfigGraphEdge edge : edges) {
                String blockId = String.valueOf(edge.getId());
                String newBlock = generateBlock(edge);
                newBlockIds.add(blockId);
                if (existingBlockIds.contains(blockId)) {
                    newContent = replaceBlockInContent(newContent, existingBlocks.get(blockId), newBlock);
                } else {
                    newContent.append("\n").append(newBlock);
                }
            }
            Set<String> blocksToRemove = new HashSet<>(existingBlockIds);
            blocksToRemove.removeAll(newBlockIds);
            for (String blockId : blocksToRemove) {
                newContent = removeBlockFromContent(newContent, existingBlocks.get(blockId));
            }
            newContent = cleanUpNewlines(newContent);
            Path tempPath = Path.of(agentsMdPath.toString() + ".tmp");
            Files.writeString(tempPath, newContent.toString(), StandardCharsets.UTF_8);
            Files.move(tempPath, agentsMdPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            log.error("Failed to sync to agent {}: {}", agentId, e);
            return false;
        }
    }

    public SyncPreviewResult syncPreview(Long graphId) {
        List<ConfigGraphEdge> edges = getEnabledEdgesForGraph(graphId);
        if (edges.isEmpty()) {
            return new SyncPreviewResult();
        }
        Map<String, List<ConfigGraphEdge>> edgesBySource = groupEdgesBySourceAgent(edges);
        SyncPreviewResult result = new SyncPreviewResult();
        result.setTotalEdgesSynced(edges.size());
        for (Map.Entry<String, List<ConfigGraphEdge>> entry : edgesBySource.entrySet()) {
            String agentId = entry.getKey();
            List<ConfigGraphEdge> agentEdges = entry.getValue();
            SyncPreviewResult.AgentPreview preview = createAgentPreview(agentId, agentEdges);
            result.getAgents().add(preview);
        }
        return result;
    }

    public SyncResult syncAll(Long graphId) {
        SyncResult result = new SyncResult();
        List<ConfigGraphEdge> edges = getEnabledEdgesForGraph(graphId);
        if (edges.isEmpty()) {
            return result;
        }
        Map<String, List<ConfigGraphEdge>> edgesBySource = groupEdgesBySourceAgent(edges);
        result.setEdgesSynced(edges.size());
        for (Map.Entry<String, List<ConfigGraphEdge>> entry : edgesBySource.entrySet()) {
            String agentId = entry.getKey();
            List<ConfigGraphEdge> agentEdges = entry.getValue();
            try {
                boolean success = syncToAgent(agentId, agentEdges);
                if (success) {
                    result.getAgentsUpdated().add(agentId);
                    updateSyncStats(result, agentId, agentEdges);
                }
            } catch (Exception e) {
                log.error("Failed to sync to agent {}: {}", agentId, e);
                result.getErrors().add("Agent " + agentId + ": " + e.getMessage());
            }
        }
        return result;
    }

    private List<ConfigGraphEdge> getEnabledEdgesForGraph(Long graphId) {
        return edgeMapper.selectList(
            new LambdaQueryWrapper<ConfigGraphEdge>()
                .eq(ConfigGraphEdge::getGraphId, graphId)
                .eq(ConfigGraphEdge::getEnabled, true)
        );
    }

    private Map<String, List<ConfigGraphEdge>> groupEdgesBySourceAgent(List<ConfigGraphEdge> edges) {
        Map<String, List<ConfigGraphEdge>> edgesBySource = new HashMap<>();
        for (ConfigGraphEdge edge : edges) {
            edgesBySource.computeIfAbsent(edge.getSourceId(), k -> new ArrayList<>()).add(edge);
            if ("error".equals(edge.getEdgeType())) {
                edgesBySource.computeIfAbsent(edge.getSourceId(), k -> new ArrayList<>()).add(edge);
            }
        }
        return edgesBySource;
    }

    private Path getAgentsMdPath(String agentId) {
        return Path.of(System.getProperty("user.home"), ".openclaw", "agents", agentId, "workspace", "AGENTS.md");
    }

    private SyncPreviewResult.AgentPreview createAgentPreview(String agentId, List<ConfigGraphEdge> agentEdges) {
        SyncPreviewResult.AgentPreview preview = new SyncPreviewResult.AgentPreview();
        preview.setAgentId(agentId);
        Path agentsMdPath = getAgentsMdPath(agentId);
        String existingContent = readExistingContent(agentsMdPath);
        Map<String, String> existingBlocks = parseExistingBlocks(existingContent);
        for (ConfigGraphEdge edge : agentEdges) {
            String blockId = String.valueOf(edge.getId());
            if (existingBlocks.containsKey(blockId)) {
                preview.getBlocksModified().add(blockId);
            } else {
                preview.getBlocksAdded().add(blockId);
            }
        }
        StringBuilder newContent = new StringBuilder(existingContent);
        for (ConfigGraphEdge edge : agentEdges) {
            String blockId = String.valueOf(edge.getId());
            String newBlock = generateBlock(edge);
            if (existingBlocks.containsKey(blockId)) {
                newContent = replaceBlockInContent(newContent, existingBlocks.get(blockId), newBlock);
            } else {
                newContent.append("\n").append(newBlock);
            }
        }
        preview.setNewContent(newContent.toString());
        return preview;
    }

    private String readExistingContent(Path agentsMdPath) {
        if (Files.exists(agentsMdPath)) {
            try {
                return Files.readString(agentsMdPath, StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Could not read existing AGENTS.md at {}: {}", agentsMdPath, e);
            }
        }
        return "";
    }

    private void updateSyncStats(SyncResult result, String agentId, List<ConfigGraphEdge> agentEdges) {
        Path agentsMdPath = getAgentsMdPath(agentId);
        String existingContent = readExistingContent(agentsMdPath);
        Map<String, String> existingBlocks = parseExistingBlocks(existingContent);
        Set<String> existingBlockIds = existingBlocks.keySet();
        Set<String> newBlockIds = agentEdges.stream()
            .map(e -> String.valueOf(e.getId()))
            .collect(java.util.stream.Collectors.toSet());
        for (String blockId : newBlockIds) {
            if (!existingBlockIds.contains(blockId)) {
                result.setBlocksAdded(result.getBlocksAdded() + 1);
            }
        }
        for (String blockId : newBlockIds) {
            if (existingBlockIds.contains(blockId)) {
                result.setBlocksModified(result.getBlocksModified() + 1);
            }
        }
        for (String blockId : existingBlockIds) {
            if (!newBlockIds.contains(blockId)) {
                result.setBlocksRemoved(result.getBlocksRemoved() + 1);
            }
        }
    }

    private StringBuilder replaceBlockInContent(StringBuilder content, String oldBlock, String newBlock) {
        String contentStr = content.toString();
        int startIndex = contentStr.indexOf(oldBlock);
        if (startIndex == -1) {
            return content;
        }
        int endIndex = contentStr.indexOf(BLOCK_END, startIndex) + BLOCK_END.length();
        if (endIndex == -1) {
            return content;
        }
        StringBuilder result = new StringBuilder();
        result.append(contentStr.substring(0, startIndex));
        result.append(newBlock);
        result.append(contentStr.substring(endIndex));
        return result;
    }

    private StringBuilder removeBlockFromContent(StringBuilder content, String blockToRemove) {
        String contentStr = content.toString().replace(blockToRemove, "");
        return new StringBuilder(contentStr.replaceAll("\n\n\n+", "\n\n"));
    }

    private StringBuilder cleanUpNewlines(StringBuilder content) {
        return new StringBuilder(content.toString().replaceAll("\n\n\n+", "\n\n"));
    }
}
