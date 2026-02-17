package com.billmanager.jizhang.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户目标实体
 * 存储用户的收入目标和支出预算
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTarget {
    
    /**
     * 目标ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 家庭组ID（0表示个人目标，>0表示家庭目标）
     */
    private Long familyGroupId = 0L;
    
    /**
     * 目标年月（格式：YYYY-MM）
     */
    private String targetMonth;
    
    /**
     * 收入目标
     */
    private BigDecimal incomeTarget;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
