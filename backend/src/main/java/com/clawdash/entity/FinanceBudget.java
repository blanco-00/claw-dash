package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;

import java.math.BigDecimal;
import java.util.Objects;

@TableName("privy_finance_budget")
public class FinanceBudget extends BaseEntity {

    private String category;
    private BigDecimal budgetAmount;
    private String month;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getBudgetAmount() {
        return budgetAmount;
    }

    public void setBudgetAmount(BigDecimal budgetAmount) {
        this.budgetAmount = budgetAmount;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        FinanceBudget that = (FinanceBudget) o;
        return Objects.equals(category, that.category) &&
                Objects.equals(budgetAmount, that.budgetAmount) &&
                Objects.equals(month, that.month);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), category, budgetAmount, month);
    }
}
