package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("cron_jobs")
public class CronJob extends BaseEntity {

    private String jobId;
    private String name;
    private String cronExpression;
    private String taskTemplate;
    private Boolean enabled;
    private LocalDateTime lastRunAt;
    private LocalDateTime nextRunAt;
}
