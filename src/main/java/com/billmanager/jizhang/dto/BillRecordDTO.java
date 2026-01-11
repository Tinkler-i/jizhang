package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 账单记录数据传输对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillRecordDTO {
    
    /**
     * 交易类型: INCOME | EXPENSE
     */
    private String type;
    
    /**
     * 金额
     */
    private BigDecimal amount;
    
    /**
     * 交易日期
     */
    private LocalDate transactionDate;
    
    /**
     * AI识别的分类名称
     */
    private String categoryName;
    
    /**
     * AI匹配的分类ID，如果未找到匹配分类则为null
     */
    private Long categoryId;
    
    /**
     * 交易说明/备注
     */
    private String description;
}
