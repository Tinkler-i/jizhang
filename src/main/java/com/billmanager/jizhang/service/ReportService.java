package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.BudgetReport;
import com.billmanager.jizhang.dto.CashFlowAnalysis;
import com.billmanager.jizhang.dto.FinancialReport;
import com.billmanager.jizhang.dto.TrendAnalysis;

import java.time.LocalDate;

/**
 * 报表服务接口
 */
public interface ReportService {
    
    /**
     * 生成财务报表
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 财务报表
     */
    FinancialReport generateFinancialReport(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 生成预算执行情况报表
     * @param userId 用户ID
     * @param budgetMonth 预算月份 (YYYY-MM)
     * @return 预算报表
     */
    BudgetReport generateBudgetReport(Long userId, String budgetMonth);
    
    /**
     * 生成现金流分析报告
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 现金流分析
     */
    CashFlowAnalysis generateCashFlowAnalysis(Long userId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 生成收支趋势预测
     * @param userId 用户ID
     * @param monthCount 需要预测的历史月数（用于计算趋势）
     * @return 趋势分析结果
     */
    TrendAnalysis generateTrendAnalysis(Long userId, Integer monthCount);
}
