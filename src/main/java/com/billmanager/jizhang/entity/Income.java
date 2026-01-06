package com.billmanager.jizhang.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Income {
    private Long id;
    private Long userId;
    private Long categoryId;
    private BigDecimal amount;
    private LocalDate transactionDate;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
