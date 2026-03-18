package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("privy_finance_budget")
public class FinanceBudget extends BaseEntity {

    private String category;
    private BigDecimal budgetAmount;
    private String month;
}
