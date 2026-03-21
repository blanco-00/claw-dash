package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.util.Objects;

@TableName("config_graph_edges")
public class ConfigGraphEdge extends BaseEntity {

    private Long graphId;
    private String sourceId;
    private String targetId;
    private String edgeType;
    private Boolean enabled;
    private String label;

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getEdgeType() {
        return edgeType;
    }

    public void setEdgeType(String edgeType) {
        this.edgeType = edgeType;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConfigGraphEdge that = (ConfigGraphEdge) o;
        return Objects.equals(graphId, that.graphId) &&
                Objects.equals(sourceId, that.sourceId) &&
                Objects.equals(targetId, that.targetId) &&
                Objects.equals(edgeType, that.edgeType) &&
                Objects.equals(enabled, that.enabled) &&
                Objects.equals(label, that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), graphId, sourceId, targetId, edgeType, enabled, label);
    }
}
