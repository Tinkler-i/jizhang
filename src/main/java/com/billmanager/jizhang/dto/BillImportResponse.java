package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 账单导入识别响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillImportResponse {
    
    /**
     * 识别到的交易记录列表
     */
    private List<BillRecordDTO> records;
}
