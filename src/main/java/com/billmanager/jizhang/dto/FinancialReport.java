package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 财务报表DTO
 * 用于展示用户的收支报表信息
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FinancialReport {
    
    // 报表基本信息
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType;  // MONTHLY, QUARTERLY, YEARLY, CUSTOM
    
    // 收支总览
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netProfit;  // 收入 - 支出
    private BigDecimal savingRate;  // 储蓄率 = 净利润 / 收入
    
    // 收入明细
    private List<CategoryAmount> incomeByCategory;
    private Long incomeCount;
    private BigDecimal avgIncome;
    
    // 支出明细
    private List<CategoryAmount> expenseByCategory;
    private Long expenseCount;
    private BigDecimal avgExpense;
    
    // 分类信息
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryAmount {
        private Long categoryId;
        private String categoryName;
        private BigDecimal amount;
        private Long count;
        private BigDecimal percentage;  // 占总数的百分比
    }
}
