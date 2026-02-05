package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.RegisterRequest;
import com.billmanager.jizhang.dto.SendVerificationCodeRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
import com.billmanager.jizhang.service.CaptchaService;
import com.billmanager.jizhang.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 注册相关 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class RegisterController {
    
    private final VerificationCodeService verificationCodeService;
    private final CaptchaService captchaService;
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-verification-code")
    public ApiResponse<String> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            log.info("【API】收到发送验证码请求 - 类型: {}", request.getType());
            
            // 验证人机验证 token
            if (request.getCaptchaToken() == null || request.getCaptchaToken().isEmpty()) {
                log.warn("【API】发送验证码失败 - captcha token 为空");
                return ApiResponse.error("请先完成人机验证");
            }
            
            if (!captchaService.isValidCaptchaToken(request.getCaptchaToken())) {
                log.warn("【API】发送验证码失败 - captcha token 无效或已过期");
                return ApiResponse.error("人机验证已过期，请重新验证");
            }
            
            verificationCodeService.sendVerificationCode(request);
            log.info("【API】验证码发送成功");
            
            // 标记 captcha token 为已使用
            captchaService.useCaptchaToken(request.getCaptchaToken());
            
            return ApiResponse.success("验证码已发送");
        } catch (Exception e) {
            log.error("【API】发送验证码失败 - 错误: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 验证码验证（检查邮箱/手机是否已注册，并验证验证码）
     */
    @PostMapping("/verify-code")
    public ApiResponse<VerifyCodeResponse> verifyCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            log.info("【API】收到验证验证码请求 - 类型: {}", request.getType());
            VerifyCodeResponse response = verificationCodeService.verifyCode(request);
            log.info("【API】验证结果 - 状态: {}", response.getStatus());
            if (response.getSuccess()) {
                return ApiResponse.success(response);
            } else {
                return ApiResponse.error(response.getMessage());
            }
        } catch (Exception e) {
            log.error("【API】验证验证码失败 - 错误: {}", e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
    
    /**
     * 用户注册
     */
    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterRequest request) {
        try {
            log.info("【API】收到注册请求 - 用户名: {}, 类型: {}", request.getUsername(), request.getType());
            verificationCodeService.registerWithVerificationCode(request);
            log.info("【API】用户注册成功 - 用户名: {}", request.getUsername());
            return ApiResponse.success("注册成功，请登录");
        } catch (Exception e) {
            log.error("【API】用户注册失败 - 用户名: {}, 错误: {}", request.getUsername(), e.getMessage(), e);
            return ApiResponse.error(e.getMessage());
        }
    }
}
