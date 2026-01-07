package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 现金流分析DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CashFlowAnalysis {
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    // 现金流统计
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netCashFlow;  // 净现金流 = 收入 - 支出
    
    // 按日期分组的现金流
    private List<DailyCashFlow> dailyCashFlows;
    
    // 现金流趋势分析
    private BigDecimal incomeAverage;
    private BigDecimal expenseAverage;
    private BigDecimal maxDailyIncome;
    private BigDecimal minDailyIncome;
    private BigDecimal maxDailyExpense;
    private BigDecimal minDailyExpense;
    private BigDecimal volatility;  // 现金流波动度
    
    // 现金流健康度评分 (0-100)
    private Integer healthScore;
    private String healthStatus;  // EXCELLENT, GOOD, FAIR, POOR
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyCashFlow {
        private LocalDate date;
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal netFlow;
        private BigDecimal cumulativeBalance;  // 累计余额
    }
}
