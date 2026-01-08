package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.DashboardData;
import com.billmanager.jizhang.dto.CategoryAnalysis;
import com.billmanager.jizhang.dto.BudgetVsActual;
import java.util.List;

/**
 * 分析服务接口
 */
public interface AnalysisService {
    
    /**
     * 获取仪表盘数据
     * @param userId 用户ID
     * @return 仪表盘数据
     */
    DashboardData getDashboardData(Long userId);
    
    /**
     * 计算财务健康分数
     * @param userId 用户ID
     * @return 健康分数(0-100)
     */
    Integer calculateFinancialHealthScore(Long userId);
    
    /**
     * 获取财务建议
     * @param userId 用户ID
     * @return 建议文本
     */
    String getFinancialRecommendation(Long userId);
    
    /**
     * 获取分类分析数据
     * @param userId 用户ID
     * @param yearMonth 年月（格式：2026-01）
     * @return 分类分析列表
     */
    List<CategoryAnalysis> getCategoryAnalysis(Long userId, String yearMonth);
    
    /**
     * 获取预算与实际对比数据
     * @param userId 用户ID
     * @param yearMonth 年月（格式：2026-01）
     * @return 预算与实际对比列表
     */
    List<BudgetVsActual> getBudgetVsActual(Long userId, String yearMonth);
}
