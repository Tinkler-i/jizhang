package com.billmanager.jizhang.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class IncomeStatistics {
    private BigDecimal totalIncome;
    private Long recordCount;
    private BigDecimal avgIncome;
    private BigDecimal maxIncome;
    private LocalDate startDate;
    private LocalDate endDate;
}
