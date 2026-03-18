package com.clawdash.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.clawdash.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("privy_finance")
public class FinanceRecord extends BaseEntity {

    private String recordId;
    private String type;
    private BigDecimal amount;
    private String category;
    private String description;
    private LocalDate recordDate;
}
