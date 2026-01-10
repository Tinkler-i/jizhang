package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 账单导入确认响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillImportConfirmResponse {
    
    /**
     * 导入成功的记录ID列表
     */
    private List<Long> importedIds;
}
