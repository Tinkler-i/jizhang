package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.config.XunfeiApiProperties;
import com.billmanager.jizhang.dto.BillRecordDTO;
import com.billmanager.jizhang.service.XunfeiApiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 讯飞大模型API服务实现
 * 使用WebSocket连接调用讯飞Spark图像理解API
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class XunfeiApiServiceImpl implements XunfeiApiService {
    
    private final XunfeiApiProperties properties;
    private final ObjectMapper objectMapper;
    
    /**
     * 调用讯飞大模型进行图像识别
     */
    @Override
    public List<BillRecordDTO> recognizeBillFromImage(String base64Image, String accountType) {
        log.info("开始调用讯飞API识别图像，账单类型: {}", accountType);
        
        try {
            // 验证配置
            if (properties.getAppId() == null || properties.getAppId().equals("YOUR_APP_ID")) {
                throw new RuntimeException("讯飞API未配置 - 请在application.yml中配置app-id");
            }
            if (properties.getApiKey() == null || properties.getApiKey().equals("YOUR_API_KEY")) {
                throw new RuntimeException("讯飞API未配置 - 请在application.yml中配置api-key");
            }
            if (properties.getApiSecret() == null || properties.getApiSecret().equals("YOUR_API_SECRET")) {
                throw new RuntimeException("讯飞API未配置 - 请在application.yml中配置api-secret");
            }
            
            log.info("✅ 讯飞API配置验证通过");
            
            // 调用讯飞API识别图像
            log.info("开始调用讯飞API...");
            String responseText = callXunfeiApi(base64Image, accountType);
            log.info("✅ 讯飞API调用成功，响应长度: {} 字符", responseText != null ? responseText.length() : 0);
            log.debug("API响应内容: {}", responseText);
            
            // 解析响应并提取账单信息
            log.info("开始解析识别结果...");
            List<BillRecordDTO> records = parseRecognitionResponse(responseText);
            log.info("✅ 识别结果解析成功，共识别到 {} 条记录", records.size());
            
            return records;
            
        } catch (Exception e) {
            log.error("❌ 调用讯飞API识别图像失败 - 错误类型: {}, 错误信息: {}", 
                e.getClass().getSimpleName(), 
                e.getMessage(), 
                e);
            throw new RuntimeException("图像识别失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用讯飞API识别图像 - 使用OkHttp3 WebSocket
     */
    private String callXunfeiApi(String base64Image, String accountType) throws Exception {
        log.info("初始化讯飞WebSocket客户端...");
        
        XunfeiWebSocketClient client = new XunfeiWebSocketClient(
            properties.getAppId(),
            properties.getApiKey(),
            properties.getApiSecret(),
            base64Image,
            accountType,
            properties.getTimeout()
        );
        
        // 等待响应完成
        long timeout = Math.max(properties.getTimeout(), 30000);
        log.info("等待讯飞API响应，超时时间: {} 毫秒", timeout);
        
        if (!client.waitForComplete(timeout, TimeUnit.MILLISECONDS)) {
            log.error("❌ 讯飞API响应超时（{}ms）", timeout);
            throw new RuntimeException("讯飞API响应超时（" + timeout + "ms）");
        }
        
        if (client.getError() != null) {
            log.error("❌ WebSocket错误: {}", client.getError());
            throw new RuntimeException("WebSocket错误: " + client.getError());
        }
        
        String response = client.getResponse();
        if (response == null || response.isEmpty()) {
            log.error("❌ 讯飞API未返回识别结果");
            throw new RuntimeException("讯飞API未返回识别结果");
        }
        
        log.info("✅ 成功获取讯飞API响应");
        return response;
    }
    
    /**
     * 解析讯飞API的响应
     */
    private List<BillRecordDTO> parseRecognitionResponse(String responseText) throws Exception {
        List<BillRecordDTO> records = new ArrayList<>();
        
        try {
            log.debug("解析API响应...");
            
            // 从响应中提取JSON数组
            JsonNode responseNode = objectMapper.readTree(responseText);
            
            // 检查响应是否为数组格式
            if (responseNode.isArray()) {
                log.debug("响应为JSON数组格式，包含 {} 个元素", responseNode.size());
                for (int i = 0; i < responseNode.size(); i++) {
                    JsonNode recordNode = responseNode.get(i);
                    BillRecordDTO record = parseSingleRecord(recordNode);
                    if (record != null) {
                        records.add(record);
                    }
                }
            } else if (responseNode.isObject()) {
                log.debug("响应为JSON对象格式");
                // 如果是单个对象，直接解析
                BillRecordDTO record = parseSingleRecord(responseNode);
                if (record != null) {
                    records.add(record);
                }
            } else {
                log.warn("响应格式不是JSON对象或数组，尝试提取JSON部分");
                // 尝试从响应中提取JSON
                String jsonPart = extractJsonFromText(responseText);
                if (!jsonPart.isEmpty()) {
                    responseNode = objectMapper.readTree(jsonPart);
                    if (responseNode.isArray()) {
                        for (JsonNode recordNode : responseNode) {
                            BillRecordDTO record = parseSingleRecord(recordNode);
                            if (record != null) {
                                records.add(record);
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 解析识别结果失败，响应内容: {}", responseText, e);
            throw new RuntimeException("识别结果解析失败: " + e.getMessage());
        }
        
        if (records.isEmpty()) {
            log.error("❌ 未能从响应中识别到有效的账单信息");
            throw new RuntimeException("未能从图像中识别到有效的账单信息");
        }
        
        log.info("✅ 成功解析 {} 条账单记录", records.size());
        return records;
    }
    
    /**
     * 从文本中提取JSON
     */
    private String extractJsonFromText(String text) {
        log.debug("从文本中提取JSON...");
        
        // 尝试找到 [ 或 {
        int startIdx = -1;
        int endIdx = -1;
        
        int bracketIdx = text.indexOf('[');
        int braceIdx = text.indexOf('{');
        
        if (bracketIdx >= 0 && (braceIdx < 0 || bracketIdx < braceIdx)) {
            startIdx = bracketIdx;
            // 找到匹配的 ]
            int depth = 0;
            for (int i = startIdx; i < text.length(); i++) {
                if (text.charAt(i) == '[') depth++;
                else if (text.charAt(i) == ']') {
                    depth--;
                    if (depth == 0) {
                        endIdx = i + 1;
                        break;
                    }
                }
            }
        } else if (braceIdx >= 0) {
            startIdx = braceIdx;
            // 找到匹配的 }
            int depth = 0;
            for (int i = startIdx; i < text.length(); i++) {
                if (text.charAt(i) == '{') depth++;
                else if (text.charAt(i) == '}') {
                    depth--;
                    if (depth == 0) {
                        endIdx = i + 1;
                        break;
                    }
                }
            }
        }
        
        if (startIdx >= 0 && endIdx > startIdx) {
            String json = text.substring(startIdx, endIdx);
            log.debug("成功提取JSON: {}...{}", 
                json.substring(0, Math.min(100, json.length())),
                json.length() > 100 ? json.substring(json.length() - 50) : "");
            return json;
        }
        
        return "";
    }
    
    /**
     * 解析单条账单记录
     */
    private BillRecordDTO parseSingleRecord(JsonNode node) throws Exception {
        if (!node.has("type") || !node.has("amount") || !node.has("transactionDate")) {
            log.warn("⚠️ 账单记录缺少必需字段: {}", node);
            return null;
        }
        
        try {
            String type = node.get("type").asText().toUpperCase().trim();
            String amountStr = node.get("amount").asText().trim();
            String dateStr = node.get("transactionDate").asText().trim();
            String categoryName = node.has("categoryName") ? 
                node.get("categoryName").asText().trim() : "其他";
            String description = node.has("description") ? 
                node.get("description").asText().trim() : "";
            
            // 验证类型
            if (!type.equals("INCOME") && !type.equals("EXPENSE")) {
                log.warn("⚠️ 无效的交易类型: {}", type);
                return null;
            }
            
            // 解析金额
            BigDecimal amount;
            try {
                amount = new BigDecimal(amountStr);
                if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                    log.warn("⚠️ 金额必须是正数: {}", amountStr);
                    return null;
                }
            } catch (Exception e) {
                log.warn("⚠️ 无效的金额格式: {}", amountStr);
                return null;
            }
            
            // 解析日期
            LocalDate transactionDate;
            try {
                transactionDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                log.warn("⚠️ 无效的日期格式: {} (期望: YYYY-MM-DD)", dateStr);
                return null;
            }
            
            BillRecordDTO record = BillRecordDTO.builder()
                .type(type)
                .amount(amount)
                .transactionDate(transactionDate)
                .categoryName(categoryName)
                .description(description)
                .build();
            
            log.debug("✅ 成功解析账单记录: {} {} {} {}", 
                type, amount, dateStr, categoryName);
            
            return record;
            
        } catch (Exception e) {
            log.warn("⚠️ 解析账单记录失败: {}", node, e);
            return null;
        }
    }
}
