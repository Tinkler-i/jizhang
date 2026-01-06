package com.billmanager.jizhang.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ExpenseCategoryRequest {
    @NotBlank(message = "分类名称不能为空")
    private String name;
    
    private String description;
}
