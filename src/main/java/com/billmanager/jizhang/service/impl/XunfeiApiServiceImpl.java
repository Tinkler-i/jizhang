package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.config.XunfeiApiProperties;
import com.billmanager.jizhang.dto.BillRecordDTO;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
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
import java.util.stream.Collectors;

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
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final IncomeCategoryMapper incomeCategoryMapper;
    
    /**
     * 调用讯飞大模型进行图像识别
     */
    @Override
    public List<BillRecordDTO> recognizeBillFromImage(String base64Image, String accountType, String currentDate, Long userId) {
        log.info("开始调用讯飞API识别图像，账单类型: {}, 当前日期: {}, 用户ID: {}", accountType, currentDate, userId);
        
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
            String responseText = callXunfeiApi(base64Image, accountType, currentDate, userId);
            log.info("✅ 讯飞API调用成功，响应长度: {} 字符", responseText != null ? responseText.length() : 0);
            log.debug("API响应内容: {}", responseText);
            
            // 解析响应并提取账单信息
            log.info("开始解析识别结果...");
            List<BillRecordDTO> records = parseRecognitionResponse(responseText, userId);
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
    private String callXunfeiApi(String base64Image, String accountType, String currentDate, Long userId) throws Exception {
        log.info("初始化讯飞WebSocket客户端...");
        
        // 获取用户的分类信息用于提示词
        String categoryContext = buildCategoryContext(userId);
        
        XunfeiWebSocketClient client = new XunfeiWebSocketClient(
            properties.getAppId(),
            properties.getApiKey(),
            properties.getApiSecret(),
            base64Image,
            accountType,
            categoryContext,
            currentDate,
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
    private List<BillRecordDTO> parseRecognitionResponse(String responseText, Long userId) throws Exception {
        List<BillRecordDTO> records = new ArrayList<>();
        
        try {
            log.debug("解析API响应，长度: {} 字符", responseText != null ? responseText.length() : 0);
            log.debug("响应前100字符: {}", responseText != null && responseText.length() > 100 ? 
                responseText.substring(0, 100) : responseText);
            
            // 首先尝试直接解析 JSON
            JsonNode responseNode = null;
            try {
                responseNode = objectMapper.readTree(responseText);
                log.debug("✅ 直接JSON解析成功");
            } catch (Exception e) {
                log.warn("⚠️ 直接JSON解析失败，尝试提取JSON部分: {}", e.getMessage());
                
                // 如果直接解析失败，尝试从文本中提取JSON
                String jsonPart = extractJsonFromText(responseText);
                if (!jsonPart.isEmpty()) {
                    try {
                        responseNode = objectMapper.readTree(jsonPart);
                        log.debug("✅ 从文本中提取JSON后成功解析");
                    } catch (Exception e2) {
                        log.error("❌ 提取JSON后仍然解析失败: {}", e2.getMessage());
                        log.debug("尝试解析的JSON内容: {}", jsonPart);
                        throw new RuntimeException("JSON解析失败: " + e2.getMessage(), e2);
                    }
                } else {
                    throw new RuntimeException("无法从响应中提取JSON: " + e.getMessage());
                }
            }
            
            // 检查响应是否为数组格式
            if (responseNode != null) {
                if (responseNode.isArray()) {
                    log.debug("响应为JSON数组格式，包含 {} 个元素", responseNode.size());
                    for (int i = 0; i < responseNode.size(); i++) {
                        JsonNode recordNode = responseNode.get(i);
                        BillRecordDTO record = parseSingleRecord(recordNode, userId);
                        if (record != null) {
                            records.add(record);
                        }
                    }
                } else if (responseNode.isObject()) {
                    log.debug("响应为JSON对象格式");
                    // 如果是单个对象，直接解析
                    BillRecordDTO record = parseSingleRecord(responseNode, userId);
                    if (record != null) {
                        records.add(record);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("❌ 解析识别结果失败，响应内容长度: {}, 错误: {}", 
                responseText != null ? responseText.length() : 0, 
                e.getMessage(), e);
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
     * 解析单条账单记录，并尝试匹配分类ID
     */
    private BillRecordDTO parseSingleRecord(JsonNode node, Long userId) throws Exception {
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
            
            // 匹配分类ID
            Long categoryId = matchCategoryId(type, categoryName, userId);
            
            BillRecordDTO record = BillRecordDTO.builder()
                .type(type)
                .amount(amount)
                .transactionDate(transactionDate)
                .categoryName(categoryName)
                .categoryId(categoryId)
                .description(description)
                .build();
            
            log.debug("✅ 成功解析账单记录: {} {} {} {} (分类ID: {})", 
                type, amount, dateStr, categoryName, categoryId);
            
            return record;
            
        } catch (Exception e) {
            log.warn("⚠️ 解析账单记录失败: {}", node, e);
            return null;
        }
    }
    
    /**
     * 构建用于提示词的分类上下文
     * 注意：这里只包括用户自定义分类（is_built_in=0），不包括系统内置分类如'待分类'
     * AI会尽量选择用户分类中最接近的，没有接近的才使用'待分类'作为默认分类
     */
    private String buildCategoryContext(Long userId) {
        log.debug("为用户 {} 构建分类上下文...", userId);
        
        try {
            // 获取用户的支出分类（排除内置分类）
            List<String> expenseCategories = expenseCategoryMapper.findByUserId(userId).stream()
                .filter(cat -> cat.getIsBuiltIn() == null || cat.getIsBuiltIn() == 0)
                .map(cat -> "- " + cat.getName())
                .collect(Collectors.toList());
            
            // 获取用户的收入分类（排除内置分类）
            List<String> incomeCategories = incomeCategoryMapper.findByUserId(userId).stream()
                .filter(cat -> cat.getIsBuiltIn() == null || cat.getIsBuiltIn() == 0)
                .map(cat -> "- " + cat.getName())
                .collect(Collectors.toList());
            
            StringBuilder context = new StringBuilder();
            context.append("\n【用户的支出分类】\n");
            if (expenseCategories.isEmpty()) {
                context.append("暂无用户自定义分类\n");
            } else {
                for (String cat : expenseCategories) {
                    context.append(cat).append("\n");
                }
            }
            
            context.append("\n【用户的收入分类】\n");
            if (incomeCategories.isEmpty()) {
                context.append("暂无用户自定义分类\n");
            } else {
                for (String cat : incomeCategories) {
                    context.append(cat).append("\n");
                }
            }
            
            log.debug("分类上下文构建完成");
            return context.toString();
            
        } catch (Exception e) {
            log.warn("构建分类上下文失败，将使用默认提示: {}", e.getMessage());
            return "";
        }
    }
    
    /**
     * 根据交易类型和分类名称匹配分类ID
     * 如果匹配不到，返回"待分类"分类的ID
     */
    private Long matchCategoryId(String type, String categoryName, Long userId) {
        try {
            if ("EXPENSE".equals(type)) {
                // 先尝试精确匹配
                com.billmanager.jizhang.entity.ExpenseCategory category = 
                    expenseCategoryMapper.findByUserIdAndName(userId, categoryName);
                if (category != null && (category.getIsBuiltIn() == null || category.getIsBuiltIn() == 0)) {
                    log.debug("支出分类精确匹配成功: {}", categoryName);
                    return category.getId();
                }
                
                // 尝试模糊匹配
                List<com.billmanager.jizhang.entity.ExpenseCategory> categories = 
                    expenseCategoryMapper.findByUserId(userId);
                for (com.billmanager.jizhang.entity.ExpenseCategory cat : categories) {
                    if ((cat.getIsBuiltIn() == null || cat.getIsBuiltIn() == 0) && 
                        isCategoryMatch(categoryName, cat.getName())) {
                        log.debug("支出分类模糊匹配成功: {} -> {}", categoryName, cat.getName());
                        return cat.getId();
                    }
                }
                
                // 返回"待分类"分类ID
                com.billmanager.jizhang.entity.ExpenseCategory unclassified = 
                    expenseCategoryMapper.findByUserIdAndName(userId, "待分类");
                if (unclassified != null) {
                    log.debug("使用待分类分类: {}", categoryName);
                    return unclassified.getId();
                }
                
            } else if ("INCOME".equals(type)) {
                // 先尝试精确匹配
                com.billmanager.jizhang.entity.IncomeCategory category = 
                    incomeCategoryMapper.findByUserIdAndName(userId, categoryName);
                if (category != null && (category.getIsBuiltIn() == null || category.getIsBuiltIn() == 0)) {
                    log.debug("收入分类精确匹配成功: {}", categoryName);
                    return category.getId();
                }
                
                // 尝试模糊匹配
                List<com.billmanager.jizhang.entity.IncomeCategory> categories = 
                    incomeCategoryMapper.findByUserId(userId);
                for (com.billmanager.jizhang.entity.IncomeCategory cat : categories) {
                    if ((cat.getIsBuiltIn() == null || cat.getIsBuiltIn() == 0) && 
                        isCategoryMatch(categoryName, cat.getName())) {
                        log.debug("收入分类模糊匹配成功: {} -> {}", categoryName, cat.getName());
                        return cat.getId();
                    }
                }
                
                // 返回"待分类"分类ID
                com.billmanager.jizhang.entity.IncomeCategory unclassified = 
                    incomeCategoryMapper.findByUserIdAndName(userId, "待分类");
                if (unclassified != null) {
                    log.debug("使用待分类分类: {}", categoryName);
                    return unclassified.getId();
                }
            }
            
            log.warn("未找到匹配的分类，类型: {}, 分类名: {}", type, categoryName);
            return null;
            
        } catch (Exception e) {
            log.error("匹配分类ID失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查两个分类名称是否匹配（支持模糊匹配）
     */
    private boolean isCategoryMatch(String recognized, String systemCategory) {
        if (recognized == null || systemCategory == null) {
            return false;
        }
        
        recognized = recognized.toLowerCase().trim();
        systemCategory = systemCategory.toLowerCase().trim();
        
        // 完全相同
        if (recognized.equals(systemCategory)) {
            return true;
        }
        
        // 包含关系（AI识别的分类包含系统分类）
        if (recognized.contains(systemCategory) || systemCategory.contains(recognized)) {
            return true;
        }
        
        // 关键词匹配
        Map<String, List<String>> keywordMap = new HashMap<>();
        keywordMap.put("餐饮", Arrays.asList("饭店", "餐厅", "早餐", "午餐", "晚餐", "咖啡", "饮料"));
        keywordMap.put("交通", Arrays.asList("车", "地铁", "公交", "出租", "滴滴", "汽车", "停车"));
        keywordMap.put("购物", Arrays.asList("商场", "超市", "便利店", "购物", "买", "衣服", "鞋"));
        keywordMap.put("娱乐", Arrays.asList("电影", "游戏", "KTV", "酒吧", "演唱会", "娱乐"));
        keywordMap.put("住房", Arrays.asList("房租", "房贷", "水电", "气", "物业", "宽带"));
        keywordMap.put("医疗", Arrays.asList("医院", "诊所", "药", "医生", "体检", "手术"));
        keywordMap.put("教育", Arrays.asList("学费", "培训", "课程", "学校", "教育", "补习"));
        keywordMap.put("工资", Arrays.asList("薪资", "工资", "薪水", "月薪", "年薪"));
        keywordMap.put("奖金", Arrays.asList("奖金", "红包", "提成", "奖励", "分红"));
        keywordMap.put("投资收益", Arrays.asList("投资", "理财", "收益", "利息", "分红"));
        
        if (keywordMap.containsKey(systemCategory)) {
            for (String keyword : keywordMap.get(systemCategory)) {
                if (recognized.contains(keyword)) {
                    return true;
                }
            }
        }
        
        return false;
    }
}
