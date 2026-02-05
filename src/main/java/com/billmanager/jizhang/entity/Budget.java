package com.billmanager.jizhang.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class Budget {
    private Long id;
    private Long userId;
    private Long familyGroupId;   // 家庭组ID
    private Long categoryId;
    private BigDecimal amount;
    private String budgetMonth;
    private BigDecimal spent;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
