package com.clawdash.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.clawdash.entity.FinanceRecord;
import com.clawdash.mapper.FinanceRecordMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class FinanceService extends ServiceImpl<FinanceRecordMapper, FinanceRecord> {

    public FinanceRecord addRecord(String type, BigDecimal amount, String category, 
                                   String description, LocalDate recordDate) {
        FinanceRecord record = new FinanceRecord();
        record.setRecordId("finance-" + UUID.randomUUID().toString().substring(0, 8));
        record.setType(type);
        record.setAmount(amount);
        record.setCategory(category);
        record.setDescription(description);
        record.setRecordDate(recordDate);
        save(record);
        return record;
    }

    public List<FinanceRecord> listByMonth(int year, int month) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        return lambdaQuery()
                .between(FinanceRecord::getRecordDate, startDate, endDate)
                .list();
    }

    public Map<String, BigDecimal> getMonthlySummary(int year, int month) {
        List<FinanceRecord> records = listByMonth(year, month);
        
        BigDecimal totalIncome = records.stream()
                .filter(r -> "INCOME".equals(r.getType()))
                .map(FinanceRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalExpense = records.stream()
                .filter(r -> "EXPENSE".equals(r.getType()))
                .map(FinanceRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return Map.of(
                "income", totalIncome,
                "expense", totalExpense,
                "balance", totalIncome.subtract(totalExpense)
        );
    }
}
