package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.DashboardData;

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
}
