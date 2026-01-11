package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.BillRecordDTO;
import java.util.List;

/**
 * 讯飞大模型API服务接口
 */
public interface XunfeiApiService {
    
    /**
     * 调用讯飞大模型进行图像识别，提取账单信息
     *
     * @param base64Image Base64编码的图像数据
     * @param accountType 账单类型提示 (ALIPAY|WECHAT|BANK|UNKNOWN)
     * @param currentDate 当前日期，格式为 YYYY-MM-DD，用于转换相对日期（如今天、昨天等）
     * @param userId 用户ID，用于获取用户的分类信息
     * @return 识别到的账单记录列表
     */
    List<BillRecordDTO> recognizeBillFromImage(String base64Image, String accountType, String currentDate, Long userId);
}
