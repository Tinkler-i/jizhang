package com.billmanager.jizhang.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class IncomeCategory {
    private Long id;
    private Long userId;
    private String name;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
