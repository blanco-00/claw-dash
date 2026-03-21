package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.time.LocalDateTime;
import java.util.Objects;

@TableName("cron_jobs")
public class CronJob extends BaseEntity {

    private String jobId;
    private String name;
    private String cronExpression;
    private String taskTemplate;
    private Boolean enabled;
    private LocalDateTime lastRunAt;
    private LocalDateTime nextRunAt;

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getTaskTemplate() {
        return taskTemplate;
    }

    public void setTaskTemplate(String taskTemplate) {
        this.taskTemplate = taskTemplate;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public LocalDateTime getLastRunAt() {
        return lastRunAt;
    }

    public void setLastRunAt(LocalDateTime lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public LocalDateTime getNextRunAt() {
        return nextRunAt;
    }

    public void setNextRunAt(LocalDateTime nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        CronJob cronJob = (CronJob) o;
        return Objects.equals(jobId, cronJob.jobId) &&
                Objects.equals(name, cronJob.name) &&
                Objects.equals(cronExpression, cronJob.cronExpression) &&
                Objects.equals(taskTemplate, cronJob.taskTemplate) &&
                Objects.equals(enabled, cronJob.enabled) &&
                Objects.equals(lastRunAt, cronJob.lastRunAt) &&
                Objects.equals(nextRunAt, cronJob.nextRunAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), jobId, name, cronExpression, taskTemplate, enabled, lastRunAt, nextRunAt);
    }
}
