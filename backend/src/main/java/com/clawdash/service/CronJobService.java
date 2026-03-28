package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.CronJob;
import com.clawdash.mapper.CronJobMapper;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CronJobService extends ServiceImpl<CronJobMapper, CronJob> {

    public PageResponse<CronJob> listPage(PageRequest request) {
        Page<CronJob> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<CronJob> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(CronJob::getName, request.getKeyword());
        }
        
        wrapper.orderByDesc(CronJob::getCreatedAt);
        IPage<CronJob> result = page(page, wrapper);
        
        return PageResponse.fromIPage(result, result.getRecords());
    }

    public Result<CronJob> create(CronJob cronJob) {
        cronJob.setJobId(UUID.randomUUID().toString().replace("-", "").substring(0, 16));
        cronJob.setEnabled(true);
        
        if (StringUtils.hasText(cronJob.getCronExpression())) {
            try {
                CronExpression cron = CronExpression.parse(cronJob.getCronExpression());
                cronJob.setNextRunAt(cron.next(LocalDateTime.now()));
            } catch (Exception e) {
                return Result.error("Invalid cron expression");
            }
        }
        
        cronJob.setCreatedAt(LocalDateTime.now());
        cronJob.setUpdatedAt(LocalDateTime.now());
        save(cronJob);
        return Result.success(cronJob);
    }

    public Result<CronJob> update(Long id, CronJob cronJob) {
        CronJob existing = super.getById(id);
        if (existing == null) {
            return Result.error("Cron job not found");
        }
        
        if (StringUtils.hasText(cronJob.getCronExpression())) {
            try {
                CronExpression cron = CronExpression.parse(cronJob.getCronExpression());
                cronJob.setNextRunAt(cron.next(LocalDateTime.now()));
            } catch (Exception e) {
                return Result.error("Invalid cron expression");
            }
        }
        
        cronJob.setId(id);
        cronJob.setUpdatedAt(LocalDateTime.now());
        updateById(cronJob);
        CronJob updated = super.getById(id);
        return Result.success(updated);
    }

    public Result<Void> delete(Long id) {
        removeById(id);
        return Result.success(null);
    }

    public Result<CronJob> getById(Long id) {
        CronJob cronJob = super.getById(id);
        if (cronJob == null) {
            return Result.error("Cron job not found");
        }
        return Result.success(cronJob);
    }

    public Result<Void> toggleEnabled(Long id) {
        CronJob cronJob = super.getById(id);
        if (cronJob == null) {
            return Result.error("Cron job not found");
        }
        cronJob.setEnabled(!cronJob.getEnabled());
        cronJob.setUpdatedAt(LocalDateTime.now());
        updateById(cronJob);
        return Result.success(null);
    }

    public Result<Void> triggerNow(Long id) {
        CronJob cronJob = super.getById(id);
        if (cronJob == null) {
            return Result.error("Cron job not found");
        }
        cronJob.setLastRunAt(LocalDateTime.now());
        
        if (StringUtils.hasText(cronJob.getCronExpression())) {
            try {
                CronExpression cron = CronExpression.parse(cronJob.getCronExpression());
                cronJob.setNextRunAt(cron.next(LocalDateTime.now()));
            } catch (Exception e) {
                // ignore
            }
        }
        
        cronJob.setUpdatedAt(LocalDateTime.now());
        updateById(cronJob);
        return Result.success(null);
    }

    public List<CronJob> getEnabledJobs() {
        return list(new LambdaQueryWrapper<CronJob>()
                .eq(CronJob::getEnabled, true));
    }
}
