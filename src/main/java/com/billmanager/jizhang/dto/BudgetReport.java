package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 预算执行情况报表DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetReport {
    
    private String budgetMonth;
    private List<BudgetItem> budgetItems;
    private BigDecimal totalBudget;
    private BigDecimal totalSpent;
    private BigDecimal totalRemaining;
    private BigDecimal budgetUtilizationRate;  // 已用预算占总预算的百分比
    private BigDecimal overBudgetAmount;  // 超支总额
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetItem {
        private Long categoryId;
        private String categoryName;
        private BigDecimal budgetAmount;
        private BigDecimal spentAmount;
        private BigDecimal remainingAmount;
        private BigDecimal utilizationRate;  // 占该类别预算的百分比
        private Boolean isOverBudget;  // 是否超预算
    }
}
