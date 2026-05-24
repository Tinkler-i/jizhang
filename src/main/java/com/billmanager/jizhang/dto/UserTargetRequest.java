package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * 用户目标请求对象
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTargetRequest {
    
    /**
     * 目标年月（格式：YYYY-MM）
     */
    @NotBlank(message = "目标年月不能为空")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "目标年月格式不正确，应为 YYYY-MM")
    private String targetMonth;
    
    /**
     * 收入目标
     */
    @NotNull(message = "收入目标不能为空")
    @DecimalMin(value = "0", inclusive = false, message = "收入目标必须大于0")
    private BigDecimal incomeTarget;
}
