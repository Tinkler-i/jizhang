package com.billmanager.jizhang.service.impl;

import com.alibaba.fastjson.JSON;
import com.billmanager.jizhang.config.SmsConfig;
import com.billmanager.jizhang.dto.SmsResponse;
import com.billmanager.jizhang.service.SmsService;
import com.billmanager.jizhang.util.HttpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 短信服务实现（使用阿里云短信 API）
 * 
 * 当前使用的短信服务商：dfsns.market.alicloudapi.com
 * 接口文档：https://market.aliyun.com/apidetail/api-service
 * 
 * 错误码说明：
 * - 400: INVALID_ARGUMENT - 请求参数错误
 * - 403: RATE_LIMIT / Quota Exhausted - 触发限发机制或套餐余额用完
 *        限制：单个号码 10 分钟内限发 3 条短信，10 分钟后重置
 * - 500: INTERNAL_ERROR - 服务器内部错误
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SmsServiceImpl implements SmsService {
    
    private final SmsConfig smsConfig;
    
    @Override
    public void sendVerificationCode(String phone, String code) {
        // 检查短信功能是否启用
        if (!smsConfig.isEnabled()) {
            log.warn("【短信】短信功能未启用，模拟发送验证码 - 手机: {}, 验证码: {}", phone, code);
            return;
        }
        
        // 验证手机号
        if (!StringUtils.hasText(phone)) {
            throw new RuntimeException("手机号不能为空");
        }
        
        // 验证验证码
        if (!StringUtils.hasText(code)) {
            throw new RuntimeException("验证码不能为空");
        }
        
        // 构建短信内容（格式：code:1234）
        String content = "code:" + code;
        
        try {
            log.info("【短信】开始发送验证码短信 - 手机: {}, 验证码: {}", phone, code);
            sendSms(phone, content);
            log.info("【短信】验证码短信已发送 - 手机: {}", phone);
        } catch (Exception e) {
            log.error("【短信】发送验证码短信失败 - 手机: {}, 错误: {}", phone, e.getMessage(), e);
            throw new RuntimeException("发送短信失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void sendCustomMessage(String phone, String content) {
        // 检查短信功能是否启用
        if (!smsConfig.isEnabled()) {
            log.warn("【短信】短信功能未启用，模拟发送自定义消息 - 手机: {}, 内容: {}", phone, content);
            return;
        }
        
        // 验证手机号
        if (!StringUtils.hasText(phone)) {
            throw new RuntimeException("手机号不能为空");
        }
        
        // 验证内容
        if (!StringUtils.hasText(content)) {
            throw new RuntimeException("短信内容不能为空");
        }
        
        try {
            log.info("【短信】开始发送自定义短信 - 手机: {}, 内容: {}", phone, content);
            sendSms(phone, content);
            log.info("【短信】自定义短信已发送 - 手机: {}", phone);
        } catch (Exception e) {
            log.error("【短信】发送自定义短信失败 - 手机: {}, 错误: {}", phone, e.getMessage(), e);
            throw new RuntimeException("发送短信失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 调用阿里云短信 API 发送短信
     * 
     * 请求参数说明：
     * - content: 短信内容（格式：code:1234）
     * - template_id: 短信模板 ID（CST_ptdie100 为测试模板）
     * - phone_number: 收件人手机号
     * 
     * @param phone   收件人手机号
     * @param content 短信内容
     */
    private void sendSms(String phone, String content) {
        // 检查配置
        if (!StringUtils.hasText(smsConfig.getHost()) || 
            !StringUtils.hasText(smsConfig.getPath()) || 
            !StringUtils.hasText(smsConfig.getAppcode())) {
            throw new RuntimeException("短信服务配置不完整");
        }
        
        // 构建请求参数
        // 注意：新接口使用 template_id 和 phone_number（带下划线）
        Map<String, String> bodys = new HashMap<>();
        bodys.put("content", content);            // 短信内容
        bodys.put("template_id", smsConfig.getTemplateId());  // 短信模板 ID
        bodys.put("phone_number", phone);         // 收件人手机号
        
        log.debug("【短信】API 请求参数: host={}, path={}, phone={}, templateId={}", 
                smsConfig.getHost(), smsConfig.getPath(), phone, smsConfig.getTemplateId());
        
        try {
            // 构建请求头
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "APPCODE " + smsConfig.getAppcode());
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            
            // 调用 HTTP 请求
            HttpUtils.HttpResponse response = HttpUtils.doPost(
                    smsConfig.getHost(),
                    smsConfig.getPath(),
                    "POST",
                    headers,
                    null,
                    bodys
            );
            
            log.debug("【短信】API 响应: {}", response.toString());
            
            // 检查响应状态码
            int statusCode = response.getStatusCode();
            if (statusCode != 200) {
                log.error("【短信】API 返回错误状态码: {}", statusCode);
                throw new RuntimeException("短信 API 返回错误: HTTP " + statusCode);
            }
            
            // 解析响应 JSON
            String responseBody = response.getBody();
            log.debug("【短信】响应内容: {}", responseBody);
            
            try {
                SmsResponse smsResponse = JSON.parseObject(responseBody, SmsResponse.class);
                
                if (!smsResponse.isSuccess()) {
                    log.error("【短信】API 返回失败 - 状态: {}, 原因: {}, 请求ID: {}", 
                            smsResponse.getStatus(), smsResponse.getReason(), smsResponse.getRequestId());
                    
                    // 根据错误码提供更详细的错误信息
                    String errorMessage = buildErrorMessage(smsResponse.getStatus(), smsResponse.getReason());
                    throw new RuntimeException(errorMessage);
                }
                
                log.info("【短信】短信发送成功 - 请求ID: {}", smsResponse.getRequestId());
            } catch (com.alibaba.fastjson.JSONException e) {
                log.error("【短信】响应 JSON 解析失败: {}", responseBody, e);
                throw new RuntimeException("短信服务响应格式错误: " + e.getMessage(), e);
            }
        } catch (Exception e) {
            log.error("【短信】调用短信 API 异常", e);
            throw new RuntimeException("调用短信 API 失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据错误码构建详细的错误信息
     */
    private String buildErrorMessage(String status, String reason) {
        switch (status) {
            case "INVALID_ARGUMENT":
                return "请求参数错误: " + reason + " (请检查手机号格式、模板 ID 等参数)";
            case "RATE_LIMIT":
                return "触发限发机制: 单个号码 10 分钟内限发 3 条短信，请稍后再试";
            case "Quota Exhausted":
                return "套餐余额用完: 请充值后再试";
            case "INTERNAL_ERROR":
                return "服务器内部错误: 请稍后重试";
            default:
                return "短信发送失败: " + status + " - " + reason;
        }
    }
}
