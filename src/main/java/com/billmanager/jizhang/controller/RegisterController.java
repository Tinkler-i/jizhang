package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.RegisterRequest;
import com.billmanager.jizhang.dto.SendVerificationCodeRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
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
    
    /**
     * 发送验证码
     */
    @PostMapping("/send-verification-code")
    public ApiResponse<String> sendVerificationCode(@RequestBody SendVerificationCodeRequest request) {
        try {
            log.info("【API】收到发送验证码请求 - 类型: {}", request.getType());
            verificationCodeService.sendVerificationCode(request);
            log.info("【API】验证码发送成功");
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
