package com.billmanager.jizhang.controller;

import com.billmanager.jizhang.dto.ApiResponse;
import com.billmanager.jizhang.dto.ForgotPasswordRequest;
import com.billmanager.jizhang.dto.ResetPasswordRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
import com.billmanager.jizhang.service.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 忘记密码相关 API 控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ForgotPasswordController {
    
    private final ForgotPasswordService forgotPasswordService;
    
    /**
     * 发送密码重置验证码
     */
    @PostMapping("/send-reset-password-code")
    public ApiResponse<String> sendResetPasswordCode(@RequestBody ForgotPasswordRequest request) {
        try {
            log.info("【API】收到发送密码重置验证码请求 - 类型: {}", request.getType());
            forgotPasswordService.sendResetPasswordCode(request);
            log.info("【API】密码重置验证码发送成功");
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
