package com.clawdash.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.FinanceBudget;
import com.clawdash.mapper.FinanceBudgetMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

@Service
public class FinanceBudgetService extends ServiceImpl<FinanceBudgetMapper, FinanceBudget> {

    public PageResponse<FinanceBudget> listPage(PageRequest request) {
        Page<FinanceBudget> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<FinanceBudget> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(request.getKeyword())) {
            wrapper.like(FinanceBudget::getCategory, request.getKeyword());
        }
        
        wrapper.orderByDesc(FinanceBudget::getMonth);
        IPage<FinanceBudget> result = page(page, wrapper);
        
        return PageResponse.fromIPage(result, result.getRecords());
    }

    public Result<FinanceBudget> create(FinanceBudget budget) {
        budget.setCreatedAt(LocalDateTime.now());
        budget.setUpdatedAt(LocalDateTime.now());
        save(budget);
        return Result.success(budget);
    }

    public Result<FinanceBudget> update(Long id, FinanceBudget budget) {
        FinanceBudget existing = super.getById(id);
        if (existing == null) {
            return Result.error("Budget not found");
        }
        budget.setId(id);
        budget.setUpdatedAt(LocalDateTime.now());
        updateById(budget);
        FinanceBudget updated = super.getById(id);
        return Result.success(updated);
    }

    public Result<Void> delete(Long id) {
        removeById(id);
        return Result.success(null);
    }
}
