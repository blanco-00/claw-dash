package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("config_graph_edges")
public class ConfigGraphEdge extends BaseEntity {

    private Long graphId;
    private String sourceId;
    private String targetId;
    private String edgeType;
    private Boolean enabled;
    private String label;
}
