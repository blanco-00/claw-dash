package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.util.Objects;

@TableName("config_graph_nodes")
public class ConfigGraphNode extends BaseEntity {

    private Long graphId;
    private String agentId;
    private Double x;
    private Double y;
    private Boolean collapsed;

    public Long getGraphId() {
        return graphId;
    }

    public void setGraphId(Long graphId) {
        this.graphId = graphId;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public Double getX() {
        return x;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public Double getY() {
        return y;
    }

    public void setY(Double y) {
        this.y = y;
    }

    public Boolean getCollapsed() {
        return collapsed;
    }

    public void setCollapsed(Boolean collapsed) {
        this.collapsed = collapsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ConfigGraphNode that = (ConfigGraphNode) o;
        return Objects.equals(graphId, that.graphId) &&
                Objects.equals(agentId, that.agentId) &&
                Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                Objects.equals(collapsed, that.collapsed);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), graphId, agentId, x, y, collapsed);
    }
}
