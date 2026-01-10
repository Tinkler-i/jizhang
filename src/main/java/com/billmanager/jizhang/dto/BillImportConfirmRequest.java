package com.billmanager.jizhang.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 账单导入确认请求DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BillImportConfirmRequest {
    
    @NotEmpty(message = "记录列表不能为空")
    @Valid
    private List<BillRecordConfirmDTO> records;
    
    /**
     * 确认的单条账单记录
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BillRecordConfirmDTO {
        
        private String type;
        
        private BigDecimal amount;
        
        private LocalDate transactionDate;
        
        /**
         * 用户在前端选择的分类ID
         */
        private Long categoryId;
        
        private String description;
    }
}
