package com.billmanager.jizhang.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 账单导入请求DTO
 */
@Data
public class BillImportRequest {
    
    @NotBlank(message = "图片数据不能为空")
    private String image;
    
    /**
     * 账单类型: ALIPAY | WECHAT | BANK | UNKNOWN
     */
    private String accountType;
}
