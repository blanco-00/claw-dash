package com.clawdash.controller;

import com.clawdash.common.Result;
import com.clawdash.entity.FinanceRecord;
import com.clawdash.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {

    private final FinanceService financeService;

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
}
