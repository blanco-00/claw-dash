package com.clawdash.dto;

import com.clawdash.entity.TaskQueueTask;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskPageResponse {

    private List<TaskQueueTask> content;

    private long totalElements;

    private int totalPages;

    private int size;

    private int number;

    private boolean first;

    private boolean last;
}
