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
import java.util.stream.Collectors;

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

    @Autowired
    private OpenClawService openClawService;

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

    public String generateTaskBlock(ConfigGraphEdge edge) {
        StringBuilder sb = new StringBuilder();
        String messageTemplate = edge.getMessageTemplate();
        String targetAgent = edge.getTargetId();
        
        sb.append("<!-- CLAWDASH:BLOCK id=\"edge-").append(edge.getId())
          .append("\" edge_type=\"task\" decision=\"llm\" -->\n");
        
        sb.append("## [CLAWDASH] Task → ").append(targetAgent);
        sb.append(" (LLM Judged)");
        sb.append("\n\n");
        
        sb.append("Type: 任务 (委托任务)\n");
        sb.append("使用 sessions_send 发送消息。\n");
        
        if (messageTemplate != null && !messageTemplate.isEmpty()) {
            sb.append("Message: ").append(messageTemplate).append("\n");
        }
        
        sb.append("\n").append(BLOCK_END);
        return sb.toString();
    }

    public String generateReplyBlock(ConfigGraphEdge edge) {
        if (edge.getReplyTarget() == null || edge.getReplyTarget().isBlank()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        String replyTemplate = edge.getReplyTemplate();
        String replyTarget = edge.getReplyTarget();
        
        sb.append("<!-- CLAWDASH:BLOCK id=\"edge-").append(edge.getId())
          .append("-reply\" edge_type=\"reply\" decision=\"llm\" -->\n");
        
        sb.append("## [CLAWDASH] Reply → ").append(replyTarget);
        sb.append(" (LLM Judged)");
        sb.append("\n\n");
        
        sb.append("Type: 回复 (完成任务后回复)\n");
        sb.append("使用 sessions_send 发送消息。\n");
        
        if (replyTemplate != null && !replyTemplate.isEmpty()) {
            sb.append("Message: ").append(replyTemplate).append("\n");
        } else {
            sb.append("Message: 任务完成。\n");
        }
        
        sb.append("\n").append(BLOCK_END);
        return sb.toString();
    }

    public String generateErrorBlock(ConfigGraphEdge edge) {
        if (edge.getErrorTarget() == null || edge.getErrorTarget().isBlank()) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        String errorTemplate = edge.getErrorTemplate();
        String errorTarget = edge.getErrorTarget();
        
        sb.append("<!-- CLAWDASH:BLOCK id=\"edge-").append(edge.getId())
          .append("-error\" edge_type=\"error\" decision=\"llm\" -->\n");
        
        sb.append("## [CLAWDASH] Error → ").append(errorTarget);
        sb.append(" (LLM Judged)");
        sb.append("\n\n");
        
        sb.append("Type: 错误 (发生错误时通知)\n");
        sb.append("使用 sessions_send 发送消息。\n");
        
        if (errorTemplate != null && !errorTemplate.isEmpty()) {
            sb.append("Message: ").append(errorTemplate).append("\n");
        } else {
            sb.append("Message: 执行出错：{error_message}\n");
        }
        
        sb.append("\n").append(BLOCK_END);
        return sb.toString();
    }

    public boolean syncToAgent(String agentId, List<GeneratedBlock> blocks) {
        Path agentsMdPath = getAgentsMdPath(agentId);
        try {
            String existingContent = Files.exists(agentsMdPath) 
                ? Files.readString(agentsMdPath, StandardCharsets.UTF_8) 
                : "";
            Map<String, String> existingBlocks = parseExistingBlocks(existingContent);
            Set<String> existingBlockIds = existingBlocks.keySet();
            Set<String> newBlockIds = new HashSet<>();
            StringBuilder newContent = new StringBuilder(existingContent);
            
            for (GeneratedBlock block : blocks) {
                newBlockIds.add(block.blockId);
                if (existingBlocks.containsKey(block.blockId)) {
                    newContent = replaceBlockInContent(newContent, existingBlocks.get(block.blockId), block.content);
                } else {
                    newContent.append("\n").append(block.content);
                }
            }
            
            Set<String> blocksToRemove = new HashSet<>(existingBlockIds);
            blocksToRemove.removeAll(newBlockIds);
            for (String blockId : blocksToRemove) {
                if (blockId.startsWith("edge-")) {
                    newContent = removeBlockFromContent(newContent, existingBlocks.get(blockId));
                }
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
        
        Map<String, List<GeneratedBlock>> blocksByAgent = new HashMap<>();
        for (ConfigGraphEdge edge : edges) {
            String sourceId = edge.getSourceId();
            String targetId = edge.getTargetId();
            
            blocksByAgent.computeIfAbsent(sourceId, k -> new ArrayList<>())
                .add(new GeneratedBlock("edge-" + edge.getId(), generateTaskBlock(edge)));
            
            String replyBlock = generateReplyBlock(edge);
            if (replyBlock != null) {
                blocksByAgent.computeIfAbsent(targetId, k -> new ArrayList<>())
                    .add(new GeneratedBlock("edge-" + edge.getId() + "-reply", replyBlock));
            }
            
            String errorBlock = generateErrorBlock(edge);
            if (errorBlock != null) {
                blocksByAgent.computeIfAbsent(targetId, k -> new ArrayList<>())
                    .add(new GeneratedBlock("edge-" + edge.getId() + "-error", errorBlock));
            }
        }
        
        SyncPreviewResult result = new SyncPreviewResult();
        result.setTotalEdgesSynced(edges.size());
        
        for (Map.Entry<String, List<GeneratedBlock>> entry : blocksByAgent.entrySet()) {
            String agentId = entry.getKey();
            List<GeneratedBlock> agentBlocks = entry.getValue();
            SyncPreviewResult.AgentPreview preview = createAgentPreview(agentId, agentBlocks);
            result.getAgents().add(preview);
        }
        
        return result;
    }

    public SyncResult syncAll(Long graphId) {
        SyncResult result = new SyncResult();
        List<ConfigGraphEdge> edges = getEnabledEdgesForGraph(graphId);
        
        Map<String, List<GeneratedBlock>> blocksByAgent = new HashMap<>();
        Set<String> allAgentIds = new HashSet<>();
        
        for (ConfigGraphEdge edge : edges) {
            String sourceId = edge.getSourceId();
            String targetId = edge.getTargetId();
            allAgentIds.add(sourceId);
            allAgentIds.add(targetId);
            
            blocksByAgent.computeIfAbsent(sourceId, k -> new ArrayList<>())
                .add(new GeneratedBlock("edge-" + edge.getId(), generateTaskBlock(edge)));
            
            String replyBlock = generateReplyBlock(edge);
            if (replyBlock != null) {
                blocksByAgent.computeIfAbsent(targetId, k -> new ArrayList<>())
                    .add(new GeneratedBlock("edge-" + edge.getId() + "-reply", replyBlock));
            }
            
            String errorBlock = generateErrorBlock(edge);
            if (errorBlock != null) {
                blocksByAgent.computeIfAbsent(targetId, k -> new ArrayList<>())
                    .add(new GeneratedBlock("edge-" + edge.getId() + "-error", errorBlock));
            }
            
            result.setEdgesSynced(edges.size());
        }
        
        List<ConfigGraphNode> nodes = nodeMapper.selectList(
            new LambdaQueryWrapper<ConfigGraphNode>()
                .eq(ConfigGraphNode::getGraphId, graphId)
                .eq(ConfigGraphNode::getDeleted, 0)
        );
        nodes.forEach(node -> allAgentIds.add(node.getAgentId()));
        
        for (String agentId : allAgentIds) {
            List<GeneratedBlock> agentBlocks = blocksByAgent.getOrDefault(agentId, new ArrayList<>());
            try {
                boolean success = syncToAgent(agentId, agentBlocks);
                if (success) {
                    result.getAgentsUpdated().add(agentId);
                    updateSyncStats(result, agentId, agentBlocks);
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

    private Path getAgentsMdPath(String agentId) {
        String workspacePath = openClawService.getWorkspacePath(agentId);
        return Path.of(workspacePath, "AGENTS.md");
    }

    private SyncPreviewResult.AgentPreview createAgentPreview(String agentId, List<GeneratedBlock> agentBlocks) {
        SyncPreviewResult.AgentPreview preview = new SyncPreviewResult.AgentPreview();
        preview.setAgentId(agentId);
        Path agentsMdPath = getAgentsMdPath(agentId);
        String existingContent = readExistingContent(agentsMdPath);
        Map<String, String> existingBlocks = parseExistingBlocks(existingContent);
        
        StringBuilder diff = new StringBuilder();
        
        for (GeneratedBlock block : agentBlocks) {
            if (existingBlocks.containsKey(block.blockId)) {
                String oldContent = existingBlocks.get(block.blockId);
                if (!oldContent.equals(block.content)) {
                    preview.getBlocksModified().add(block.blockId);
                    diff.append("--- Old: ").append(block.blockId).append("\n");
                    diff.append(oldContent).append("\n");
                    diff.append("+++ New: ").append(block.blockId).append("\n");
                    diff.append(block.content).append("\n");
                }
            } else {
                preview.getBlocksAdded().add(block.blockId);
                diff.append("+ ").append(block.blockId).append(" (new)\n");
            }
        }
        
        Set<String> newBlockIds = agentBlocks.stream()
            .map(b -> b.blockId)
            .collect(Collectors.toSet());
        for (String existingBlockId : existingBlocks.keySet()) {
            if (!newBlockIds.contains(existingBlockId) && existingBlockId.startsWith("edge-")) {
                preview.getBlocksRemoved().add(existingBlockId);
                diff.append("- ").append(existingBlockId).append(" (removed)\n");
            }
        }
        
        preview.setNewContent(diff.toString());
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

    private void updateSyncStats(SyncResult result, String agentId, List<GeneratedBlock> agentBlocks) {
        Path agentsMdPath = getAgentsMdPath(agentId);
        String existingContent = readExistingContent(agentsMdPath);
        Map<String, String> existingBlocks = parseExistingBlocks(existingContent);
        
        for (GeneratedBlock block : agentBlocks) {
            if (!existingBlocks.containsKey(block.blockId)) {
                result.setBlocksAdded(result.getBlocksAdded() + 1);
            } else if (!existingBlocks.get(block.blockId).equals(block.content)) {
                result.setBlocksModified(result.getBlocksModified() + 1);
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
        if (blockToRemove == null) return content;
        String contentStr = content.toString().replace(blockToRemove, "");
        return new StringBuilder(contentStr.replaceAll("\n\n\n+", "\n\n"));
    }

    private StringBuilder cleanUpNewlines(StringBuilder content) {
        return new StringBuilder(content.toString().replaceAll("\n\n\n+", "\n\n"));
    }

    public static class GeneratedBlock {
        public String blockId;
        public String content;
        
        public GeneratedBlock(String blockId, String content) {
            this.blockId = blockId;
            this.content = content;
        }
    }
}
