package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.ForgotPasswordRequest;
import com.billmanager.jizhang.dto.ResetPasswordRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
import com.billmanager.jizhang.service.CaptchaService;
import com.billmanager.jizhang.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 忘记密码相关 API 控制器
 * 
 * 【重要】此控制器处理密码重置流程，与登录防护系统完全隔离。
 * 即使用户的浏览器由于多次登录失败而被标记为需要人机验证，
 * 忘记密码流程仍应正常可用。密码重置使用独立的验证码系统（邮箱/短信）
 * 和独立的人机验证码（与登录页面共享同一验证码库，但不受登录防护影响）。
 * 
 * 流程：
 * 1. 用户完成人机验证，获取 captchaToken
 * 2. 调用 /send-reset-password-code 发送邮箱/短信验证码（需要 captchaToken）
 * 3. 用户输入验证码，调用 /verify-reset-password-code 进行验证
 * 4. 验证成功后，调用 /reset-password 完成密码重置
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgotPasswordController {
    
    private final ForgotPasswordService forgotPasswordService;
    private final CaptchaService captchaService;
    
    /**
     * 发送密码重置验证码
     */
    @PostMapping("/send-reset-password-code")
    public ApiResponse<String> sendResetPasswordCode(@RequestBody ForgotPasswordRequest request) {
        try {
            log.info("【API】收到发送密码重置验证码请求 - 类型: {}", request.getType());
            
            // 验证人机验证 token
            if (request.getCaptchaToken() == null || request.getCaptchaToken().isEmpty()) {
                log.warn("【API】发送密码重置验证码失败 - captcha token 为空");
                return ApiResponse.error("请先完成人机验证");
            }
            
            if (!captchaService.isValidCaptchaToken(request.getCaptchaToken())) {
                log.warn("【API】发送密码重置验证码失败 - captcha token 无效或已过期");
                return ApiResponse.error("人机验证已过期，请重新验证");
            }
            
            forgotPasswordService.sendResetPasswordCode(request);
            log.info("【API】密码重置验证码发送成功");
            
            // 标记 captcha token 为已使用
            captchaService.useCaptchaToken(request.getCaptchaToken());
            
            return ApiResponse.success("验证码已发送");
        } catch (Exception e) {
            log.error("【API】发送密码重置验证码失败 - 错误: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 验证密码重置验证码
     */
    @PostMapping("/verify-reset-password-code")
    public ApiResponse<VerifyCodeResponse> verifyResetPasswordCode(@RequestBody ForgotPasswordRequest request) {
        try {
            log.info("【API】收到验证密码重置验证码请求 - 类型: {}", request.getType());
            VerifyCodeResponse response = forgotPasswordService.verifyResetPasswordCode(request);
            log.info("【API】验证结果 - 状态: {}", response.getStatus());
            if (response.getSuccess()) {
                return ApiResponse.success(response);
            } else {
                return ApiResponse.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("【API】验证密码重置验证码失败 - 错误: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 重置密码
     */
    @PostMapping("/reset-password")
    public ApiResponse<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            log.info("【API】收到重置密码请求 - 类型: {}", request.getType());
            forgotPasswordService.resetPassword(request);
            log.info("【API】密码重置成功");
            return ApiResponse.success("密码重置成功，请重新登录");
        } catch (Exception e) {
            log.error("【API】密码重置失败 - 错误: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
