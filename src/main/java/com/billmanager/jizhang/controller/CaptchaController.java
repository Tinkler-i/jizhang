package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.CaptchaInitResponse;
import com.billmanager.jizhang.dto.CaptchaVerifyRequest;
import com.billmanager.jizhang.dto.CaptchaVerifyResponse;
import com.billmanager.jizhang.service.CaptchaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 人机验证码 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    
    private final CaptchaService captchaService;
    
    /**
     * 初始化验证码 (Makeit Captcha 使用 GET 请求)
     */
    @GetMapping("/init")
    public ApiResponse<CaptchaInitResponse> initCaptcha() {
        try {
            log.info("【API】收到验证码初始化请求");
            CaptchaInitResponse response = captchaService.initCaptcha();
            log.info("【API】验证码初始化成功 - captchaId: {}", response.getCaptchaId());
            return ApiResponse.success(response);
        } catch (Exception e) {
            log.error("【API】验证码初始化失败 - 错误: {}", e.getMessage(), e);
            return ApiResponse.error("验证码初始化失败");
        }
    }
    
    /**
     * 校验验证码（返回库能直接识别的格式）
     */
    @PostMapping("/verify")
    public Map<String, Object> verifyCaptcha(
            @RequestBody java.util.Map<String, Object> rawBody,
            @RequestParam(value = "captchaId", required = false) String queryParamCaptchaId) {
        try {
            log.info("【API】收到验证码校验请求");
            
            // 打印原始的 JSON 请求体
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(rawBody);
            log.info("【API】原始 JSON 请求体: {}", jsonBody);
            log.info("【API】请求体所有字段: {}", rawBody.keySet());
            
            // 手动转换为 CaptchaVerifyRequest
            CaptchaVerifyRequest request = objectMapper.convertValue(rawBody, CaptchaVerifyRequest.class);
            
            log.info("【API】解析后的信息 - key: {}, moveDistance: {}, captchaId: {}, timestamp: {}", 
                    request.getKey(), request.getMoveDistance(), request.getCaptchaId(), 
                    request.getTimestamp());
            log.info("【API】查询参数 - captchaId: {}", queryParamCaptchaId);
            
            // 如果 request body 中没有 captchaId，使用查询参数中的
            if (request.getCaptchaId() == null && queryParamCaptchaId != null) {
                request.setCaptchaId(queryParamCaptchaId);
                log.info("【API】从查询参数获取 captchaId: {}", queryParamCaptchaId);
            }
            
            CaptchaVerifyResponse response = captchaService.verifyCaptcha(request);
            
            if (response.isSuccess()) {
                log.info("【API】验证码校验成功 - token: {}", response.getToken());
            } else {
                log.warn("【API】验证码校验失败 - 原因: {}", response.getMessage());
            }
            
            // 返回库期望的格式: { ret: { code: 200 }, data: {...} }
            Map<String, Object> result = new HashMap<>();
            Map<String, Integer> ret = new HashMap<>();
            ret.put("code", response.isSuccess() ? 200 : 401);
            result.put("ret", ret);
            result.put("data", response);
            
            return result;
        } catch (Exception e) {
            log.error("【API】验证码校验异常 - 错误: {}", e.getMessage(), e);
            // 返回失败响应
            Map<String, Object> result = new HashMap<>();
            Map<String, Integer> ret = new HashMap<>();
            ret.put("code", 500);
            result.put("ret", ret);
            
            CaptchaVerifyResponse errorResponse = new CaptchaVerifyResponse();
            errorResponse.setSuccess(false);
            errorResponse.setCode(1);
            errorResponse.setMessage("验证码校验异常: " + e.getMessage());
            result.put("data", errorResponse);
            
            return result;
        }
    }
}
