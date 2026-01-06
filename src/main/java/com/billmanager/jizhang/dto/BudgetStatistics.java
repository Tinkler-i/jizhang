package com.billmanager.jizhang.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetStatistics {
    private Long id;
    private Long categoryId;
    private String categoryName;
    private BigDecimal budgetAmount;
    private BigDecimal spentAmount;
    private BigDecimal remainingAmount;
    private BigDecimal percentageUsed;
    private String budgetMonth;
    private String remark;
}
