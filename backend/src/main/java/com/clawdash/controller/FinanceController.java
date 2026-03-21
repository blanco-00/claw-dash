package com.clawdash.controller;

import com.clawdash.common.PageRequest;
import com.clawdash.common.PageResponse;
import com.clawdash.common.Result;
import com.clawdash.entity.FinanceBudget;
import com.clawdash.entity.FinanceRecord;
import com.clawdash.service.FinanceBudgetService;
import com.clawdash.service.FinanceService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
public class FinanceController {

    private final FinanceService financeService;
    private final FinanceBudgetService financeBudgetService;

    public FinanceController(FinanceService financeService, FinanceBudgetService financeBudgetService) {
        this.financeService = financeService;
        this.financeBudgetService = financeBudgetService;
    }

    @PostMapping("/record")
    public Result<FinanceRecord> addRecord(@RequestBody Map<String, Object> request) {
        String type = (String) request.get("type");
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String category = (String) request.get("category");
        String description = (String) request.get("description");
        LocalDate recordDate = LocalDate.parse((String) request.get("recordDate"));

        FinanceRecord record = financeService.addRecord(type, amount, category, description, recordDate);
        return Result.success(record);
    }

    @GetMapping("/records")
    public Result<List<FinanceRecord>> listRecords(@RequestParam int year, @RequestParam int month) {
        List<FinanceRecord> records = financeService.listByMonth(year, month);
        return Result.success(records);
    }

    @GetMapping("/summary")
    public Result<Map<String, BigDecimal>> getSummary(@RequestParam int year, @RequestParam int month) {
        Map<String, BigDecimal> summary = financeService.getMonthlySummary(year, month);
        return Result.success(summary);
    }

    @GetMapping("/budgets")
    public Result<PageResponse<FinanceBudget>> listBudgets(PageRequest request) {
        return Result.success(financeBudgetService.listPage(request));
    }

    @PostMapping("/budgets")
    public Result<FinanceBudget> createBudget(@RequestBody FinanceBudget budget) {
        return financeBudgetService.create(budget);
    }

    @PutMapping("/budgets/{id}")
    public Result<FinanceBudget> updateBudget(@PathVariable Long id, @RequestBody FinanceBudget budget) {
        return financeBudgetService.update(id, budget);
    }

    @DeleteMapping("/budgets/{id}")
    public Result<Void> deleteBudget(@PathVariable Long id) {
        return financeBudgetService.delete(id);
    }
}
