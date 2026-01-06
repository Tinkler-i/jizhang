package com.billmanager.jizhang.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    @NotNull(message = "预算金额不能为空")
    @Positive(message = "预算金额必须大于0")
    private BigDecimal amount;
    
    @NotNull(message = "预算年月不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "预算年月格式应为YYYY-MM")
    private String budgetMonth;
    
    private String remark;
}
