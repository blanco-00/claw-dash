package com.clawdash.dto;

import java.util.ArrayList;
import java.util.List;

public class SyncResult {
    private List<String> agentsUpdated;
    private int edgesSynced;
    private int blocksAdded;
    private int blocksModified;
    private int blocksRemoved;
    private List<String> errors;

    public SyncResult() {
        this.agentsUpdated = new ArrayList<>();
        this.edgesSynced = 0;
        this.blocksAdded = 0;
        this.blocksModified = 0;
        this.blocksRemoved = 0;
        this.errors = new ArrayList<>();
    }

    public List<String> getAgentsUpdated() {
        return agentsUpdated;
    }

    public void setAgentsUpdated(List<String> agentsUpdated) {
        this.agentsUpdated = agentsUpdated;
    }

    public int getEdgesSynced() {
        return edgesSynced;
    }

    public void setEdgesSynced(int edgesSynced) {
        this.edgesSynced = edgesSynced;
    }

    public int getBlocksAdded() {
        return blocksAdded;
    }

    public void setBlocksAdded(int blocksAdded) {
        this.blocksAdded = blocksAdded;
    }

    public int getBlocksModified() {
        return blocksModified;
    }

    public void setBlocksModified(int blocksModified) {
        this.blocksModified = blocksModified;
    }

    public int getBlocksRemoved() {
        return blocksRemoved;
    }

    public void setBlocksRemoved(int blocksRemoved) {
        this.blocksRemoved = blocksRemoved;
    }

    public List<String> getErrors() {
        return errors;
    }

    public void setErrors(List<String> errors) {
        this.errors = errors;
    }
}
