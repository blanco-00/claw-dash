package com.clawdash.controller;

import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.CronJob;
import com.clawdash.service.CronJobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cron-jobs")
public class CronJobController {

    @Autowired
    private CronJobService cronJobService;

    @GetMapping
    public Result<PageResponse<CronJob>> list(PageRequest request) {
        return Result.success(cronJobService.listPage(request));
    }

    @GetMapping("/enabled")
    public Result<List<CronJob>> getEnabled() {
        return Result.success(cronJobService.getEnabledJobs());
    }

    @GetMapping("/{id}")
    public Result<CronJob> getById(@PathVariable Long id) {
        return cronJobService.getById(id);
    }

    @PostMapping
    public Result<CronJob> create(@RequestBody CronJob cronJob) {
        return cronJobService.create(cronJob);
    }

    @PutMapping("/{id}")
    public Result<CronJob> update(@PathVariable Long id, @RequestBody CronJob cronJob) {
        return cronJobService.update(id, cronJob);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        return cronJobService.delete(id);
    }

    @PatchMapping("/{id}/toggle")
    public Result<Void> toggleEnabled(@PathVariable Long id) {
        return cronJobService.toggleEnabled(id);
    }

    @PostMapping("/{id}/trigger")
    public Result<Void> triggerNow(@PathVariable Long id) {
        return cronJobService.triggerNow(id);
    }
}
