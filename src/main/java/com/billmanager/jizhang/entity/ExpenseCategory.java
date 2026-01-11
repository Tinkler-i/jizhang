package com.billmanager.jizhang.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ExpenseCategory {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private Integer isBuiltIn;  // 0-用户自定义，1-系统内置
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

