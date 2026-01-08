package com.billmanager.jizhang.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 预算与实际对比数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetVsActual {
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 预算金额
     */
    private BigDecimal budgetAmount;
    
    /**
     * 实际金额
     */
    private BigDecimal actualAmount;
    
    /**
     * 执行率百分比（0-100）
     */
    private BigDecimal percentage;
    
    /**
     * 剩余预算
     */
    private BigDecimal remaining;
}
