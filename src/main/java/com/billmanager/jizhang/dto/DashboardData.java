package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 仪表盘数据DTO
 * 综合展示用户的财务状况
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardData {
    
    // 基本指标卡
    private BigDecimal thisMonthIncome;
    private BigDecimal thisMonthExpense;
    private BigDecimal thisMonthBalance;
    private BigDecimal thisMonthBudgetUtilization;  // 本月预算使用率
    
    // 最近30天数据
    private BigDecimal last30DaysIncome;
    private BigDecimal last30DaysExpense;
    private BigDecimal last30DaysBalance;
    
    // 当年累计
    private BigDecimal yearToDateIncome;
    private BigDecimal yearToDateExpense;
    private BigDecimal yearToDateBalance;
    
    // 目标与实际对比
    private BigDecimal targetIncome;
    private BigDecimal incomeAchievementRate;  // 实际收入 / 目标收入
    private BigDecimal targetExpense;
    private BigDecimal expenseControlRate;  // 目标支出 / 实际支出
    
    // 图表数据
    private ChartData chartData;
    
    // 预告和建议
    private String recommendation;
    private Integer financialHealthScore;  // 0-100
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChartData {
        // 收支趋势（最近30天）
        private List<String> dates;
        private List<BigDecimal> incomeData;
        private List<BigDecimal> expenseData;
        
        // 收入分类饼图
        private List<CategoryData> incomeCategory;
        
        // 支出分类饼图
        private List<CategoryData> expenseCategory;
        
        // 预算执行情况
        private List<BudgetComparisonData> budgetComparison;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryData {
        private String name;
        private BigDecimal value;
        private BigDecimal percentage;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BudgetComparisonData {
        private String category;
        private BigDecimal budget;
        private BigDecimal actual;
    }
}
