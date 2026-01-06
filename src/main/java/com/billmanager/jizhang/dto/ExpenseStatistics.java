package com.billmanager.jizhang.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseStatistics {
    private BigDecimal totalExpense;
    private Long recordCount;
    private BigDecimal avgExpense;
    private BigDecimal maxExpense;
    private LocalDate startDate;
    private LocalDate endDate;
}
