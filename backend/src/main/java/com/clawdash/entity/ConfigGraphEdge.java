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
    private String decisionMode;
    private String messageTemplate;
    private String sourceHandle;
    private String targetHandle;
    private String replyTarget;
    private String replyTemplate;
    private String errorTarget;
    private String errorTemplate;

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

    public String getDecisionMode() {
        return decisionMode;
    }

    public void setDecisionMode(String decisionMode) {
        this.decisionMode = decisionMode;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getSourceHandle() {
        return sourceHandle;
    }

    public void setSourceHandle(String sourceHandle) {
        this.sourceHandle = sourceHandle;
    }

    public String getTargetHandle() {
        return targetHandle;
    }

    public void setTargetHandle(String targetHandle) {
        this.targetHandle = targetHandle;
    }

    public String getReplyTarget() {
        return replyTarget;
    }

    public void setReplyTarget(String replyTarget) {
        this.replyTarget = replyTarget;
    }

    public String getReplyTemplate() {
        return replyTemplate;
    }

    public void setReplyTemplate(String replyTemplate) {
        this.replyTemplate = replyTemplate;
    }

    public String getErrorTarget() {
        return errorTarget;
    }

    public void setErrorTarget(String errorTarget) {
        this.errorTarget = errorTarget;
    }

    public String getErrorTemplate() {
        return errorTemplate;
    }

    public void setErrorTemplate(String errorTemplate) {
        this.errorTemplate = errorTemplate;
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
                Objects.equals(label, that.label) &&
                Objects.equals(decisionMode, that.decisionMode) &&
                Objects.equals(messageTemplate, that.messageTemplate) &&
                Objects.equals(sourceHandle, that.sourceHandle) &&
                Objects.equals(targetHandle, that.targetHandle) &&
                Objects.equals(replyTarget, that.replyTarget) &&
                Objects.equals(replyTemplate, that.replyTemplate) &&
                Objects.equals(errorTarget, that.errorTarget) &&
                Objects.equals(errorTemplate, that.errorTemplate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), graphId, sourceId, targetId, edgeType, enabled, label, decisionMode, messageTemplate, sourceHandle, targetHandle, replyTarget, replyTemplate, errorTarget, errorTemplate);
    }
}
