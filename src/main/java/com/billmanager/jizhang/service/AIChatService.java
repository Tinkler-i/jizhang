package com.billmanager.jizhang.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * AI聊天服务 - 调用NVIDIA NIM API处理用户问题
 * 使用OkHttp3进行HTTP调用
 */
@Slf4j
@Service
public class AIChatService {

    @Autowired
    private AIDataProvider aiDataProvider;

    @Value("${nvidia.nim.base-url}")
    private String baseUrl;

    @Value("${nvidia.nim.api-key}")
    private String apiKey;

    @Value("${nvidia.nim.model}")
    private String model;

    @Value("${nvidia.nim.temperature}")
    private Double temperature;

    @Value("${nvidia.nim.top-p}")
    private Double topP;

    @Value("${nvidia.nim.max-tokens}")
    private Integer maxTokens;

    private OkHttpClient httpClient;

    /**
     * 初始化HTTP客户端(延迟初始化)
     */
    private synchronized OkHttpClient getHttpClient() {
        if (httpClient == null) {
            httpClient = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .build();
        }
        return httpClient;
    }

    /**
     * 调用AI进行对话 - 获取完整响应
     */
    public String chat(Long userId, String userMessage) {
        try {
            // 获取用户全部财务数据
            String financialData = aiDataProvider.getAllUserFinancialDataText(userId);
            
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(financialData);
            
            // 输出调试信息
            log.info("========== AI财务顾问请求 ==========");
            log.info("用户ID: {}", userId);
            log.info("用户问题: {}", userMessage);
            log.info("财务数据大小: {} 字节", financialData.length());
            log.info("========== 系统提示词 ==========");
            log.info("{}", systemPrompt);
            log.info("==================================");
            
            // 构建请求体
            JSONObject requestBody = buildRequestBody(systemPrompt, userMessage, false);
            
            // 发送API请求
            String response = callNvidiaAPI(requestBody.toJSONString());
            
            log.info("AI返回响应: {}", response);
            
            return response;
            
        } catch (Exception e) {
            log.error("AI调用出错", e);
            return "抱歉，AI服务暂时出错，请稍后重试。错误: " + e.getMessage();
        }
    }

    /**
     * 调用AI进行流式对话 - 实时返回响应
     */
    public String chatStream(Long userId, String userMessage) {
        try {
            // 获取用户全部财务数据
            String financialData = aiDataProvider.getAllUserFinancialDataText(userId);
            
            // 构建系统提示词
            String systemPrompt = buildSystemPrompt(financialData);
            
            // 输出调试信息
            log.info("========== AI财务顾问流式请求 ==========");
            log.info("用户ID: {}", userId);
            log.info("用户问题: {}", userMessage);
            log.info("财务数据大小: {} 字节", financialData.length());
            log.info("========== 系统提示词 ==========");
            log.info("{}", systemPrompt);
            log.info("==================================");
            
            // 构建请求体
            JSONObject requestBody = buildRequestBody(systemPrompt, userMessage, true);
            
            // 发送API请求
            String response = callNvidiaAPI(requestBody.toJSONString());
            
            log.info("AI流式返回响应: {}", response);
            
            return response;
            
        } catch (Exception e) {
            log.error("流式AI调用出错", e);
            return "抱歉，AI服务暂时出错，请稍后重试。错误: " + e.getMessage();
        }
    }

    /**
     * 调用NVIDIA NIM API
     */
    private String callNvidiaAPI(String requestBodyJson) throws IOException {
        Request request = new Request.Builder()
                .url(baseUrl + "/chat/completions")
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(requestBodyJson, MediaType.get("application/json")))
                .build();
        
        try (Response response = getHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                log.error("NVIDIA API错误: {}", errorBody);
                throw new RuntimeException("API调用失败: " + response.code() + " " + errorBody);
            }
            
            String responseBody = response.body().string();
            log.debug("API响应: {}", responseBody);
            
            // 解析响应
            JSONObject jsonResponse = JSON.parseObject(responseBody);
            
            // 非流式响应
            if (jsonResponse.containsKey("choices")) {
                JSONArray choices = jsonResponse.getJSONArray("choices");
                if (choices.size() > 0) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getString("content");
                }
            }
            
            return "无法获取响应";
        }
    }

    /**
     * 构建请求体
     */
    private JSONObject buildRequestBody(String systemPrompt, String userMessage, boolean stream) {
        JSONObject request = new JSONObject();
        request.put("model", model);
        request.put("temperature", temperature);
        request.put("top_p", topP);
        request.put("max_tokens", maxTokens);
        request.put("stream", stream);
        
        // 构建消息列表
        JSONArray messages = new JSONArray();
        
        JSONObject systemMsg = new JSONObject();
        systemMsg.put("role", "system");
        systemMsg.put("content", systemPrompt);
        messages.add(systemMsg);
        
        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messages.add(userMsg);
        
        request.put("messages", messages);
        
        return request;
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String financialData) {
        java.time.LocalDate today = java.time.LocalDate.now();
        String todayStr = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy年MM月dd日"));
        
        return "你是一个专业的个人财务顾问。用户将提出关于自己账单和财务情况的问题。\n" +
               "当前日期为：" + todayStr + "\n\n" +
               "请根据下面提供的用户完整的历史财务数据来回答问题。数据包含用户之前所有月份的账单记录。\n" +
               "回答时要准确、简洁、有见地。可以基于历史数据进行趋势分析。\n" +
               "如果用户问的问题超出财务数据范围，可以礼貌地告诉用户您无法回答。\n\n" +
               "用户财务数据如下：\n" +
               "=".repeat(50) + "\n" +
               financialData +
               "=".repeat(50) + "\n";
    }
}
