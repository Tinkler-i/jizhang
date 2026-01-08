package com.billmanager.jizhang.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

/**
 * 分类分析数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryAnalysis {
    
    /**
     * 分类ID
     */
    private Long categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 占比百分比（0-100）
     */
    private BigDecimal percentage;
    
    /**
     * 交易数量
     */
    private Integer count;
}
