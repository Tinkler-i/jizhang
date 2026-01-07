package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 收支趋势分析与预测DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendAnalysis {
    
    private LocalDate analysisDate;
    
    // 历史数据
    private List<MonthlySummary> historicalData;
    
    // 趋势分析
    private BigDecimal incomeGrowthRate;  // 收入增长率
    private BigDecimal expenseGrowthRate;  // 支出增长率
    private String incomeTrend;  // UP, DOWN, STABLE
    private String expenseTrend;
    
    // 预测数据（未来3个月）
    private List<Forecast> forecastData;
    
    // 预测准确度信息
    private Integer confidenceLevel;  // 0-100
    private String confidenceNote;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlySummary {
        private String month;  // YYYY-MM
        private BigDecimal income;
        private BigDecimal expense;
        private BigDecimal netProfit;
        private Integer transactionCount;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Forecast {
        private String month;  // YYYY-MM
        private BigDecimal predictedIncome;
        private BigDecimal predictedExpense;
        private BigDecimal predictedNetProfit;
        private String trend;  // UP, DOWN, STABLE
        private BigDecimal confidence;  // 信心指数 0-1
    }
}
