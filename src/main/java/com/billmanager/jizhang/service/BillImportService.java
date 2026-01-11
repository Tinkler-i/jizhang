package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.BillImportConfirmRequest;
import com.billmanager.jizhang.dto.BillImportConfirmResponse;
import com.billmanager.jizhang.dto.BillImportResponse;

/**
 * 账单导入服务接口
 */
public interface BillImportService {
    
    /**
     * 识别账单图像中的交易信息
     *
     * @param base64Image Base64编码的图像数据
     * @param accountType 账单类型提示
     * @param currentDate 当前日期，格式为 YYYY-MM-DD，用于转换相对日期
     * @param userId 用户ID，用于获取用户的分类信息
     * @return 识别结果
     */
    BillImportResponse recognize(String base64Image, String accountType, String currentDate, Long userId);
    
    /**
     * 确认并导入账单记录
     *
     * @param userId 用户ID
     * @param request 确认的记录列表
     * @return 导入结果
     */
    BillImportConfirmResponse confirm(Long userId, BillImportConfirmRequest request);
}

