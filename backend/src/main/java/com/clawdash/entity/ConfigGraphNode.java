package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("config_graph_nodes")
public class ConfigGraphNode extends BaseEntity {

    private Long graphId;
    private String agentId;
    private Double x;
    private Double y;
    private Boolean collapsed;
}
