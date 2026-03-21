package com.clawdash.dto;

import java.util.ArrayList;
import java.util.List;

public class SyncPreviewResult {
    private List<AgentPreview> agents;
    private int totalEdgesSynced;

    public SyncPreviewResult() {
        this.agents = new ArrayList<>();
        this.totalEdgesSynced = 0;
    }

    public List<AgentPreview> getAgents() {
        return agents;
    }

    public void setAgents(List<AgentPreview> agents) {
        this.agents = agents;
    }

    public int getTotalEdgesSynced() {
        return totalEdgesSynced;
    }

    public void setTotalEdgesSynced(int totalEdgesSynced) {
        this.totalEdgesSynced = totalEdgesSynced;
    }

    public static class AgentPreview {
        private String agentId;
        private List<String> blocksAdded;
        private List<String> blocksModified;
        private List<String> blocksRemoved;
        private String newContent;

        public AgentPreview() {
            this.blocksAdded = new ArrayList<>();
            this.blocksModified = new ArrayList<>();
            this.blocksRemoved = new ArrayList<>();
        }

        public String getAgentId() {
            return agentId;
        }

        public void setAgentId(String agentId) {
            this.agentId = agentId;
        }

        public List<String> getBlocksAdded() {
            return blocksAdded;
        }

        public void setBlocksAdded(List<String> blocksAdded) {
            this.blocksAdded = blocksAdded;
        }

        public List<String> getBlocksModified() {
            return blocksModified;
        }

        public void setBlocksModified(List<String> blocksModified) {
            this.blocksModified = blocksModified;
        }

        public List<String> getBlocksRemoved() {
            return blocksRemoved;
        }

        public void setBlocksRemoved(List<String> blocksRemoved) {
            this.blocksRemoved = blocksRemoved;
        }

        public String getNewContent() {
            return newContent;
        }

        public void setNewContent(String newContent) {
            this.newContent = newContent;
        }
    }
}
