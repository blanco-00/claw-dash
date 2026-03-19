package com.clawdash.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateTaskRequest {

    private String type;

    private String payload;

    private Integer priority = 5;

    private String scheduledAt;

    private Integer maxRetries = 3;

    private List<String> dependsOn;
}
