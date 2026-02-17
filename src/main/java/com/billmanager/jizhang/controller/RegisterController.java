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
 * 
 * 【重要】此控制器处理用户注册流程，与登录防护系统完全隔离。
 * 即使用户的浏览器由于多次登录失败而被标记为需要人机验证，
 * 注册流程仍应正常可用。注册使用独立的验证码系统（邮箱/短信）
 * 和独立的人机验证码（与登录页面共享同一验证码库，但不受登录防护影响）。
 * 
 * 流程：
 * 1. 用户完成人机验证，获取 captchaToken
 * 2. 调用 /send-verification-code 发送邮箱/短信验证码（需要 captchaToken）
 * 3. 用户输入验证码，调用 /verify-code 进行验证
 * 4. 验证成功后，调用 /register 完成注册
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
        long startTime = System.currentTimeMillis();
        try {
            log.info("【注册API】========== 开始处理注册请求 ==========");
            log.info("【注册API】用户名: {}, 邮箱: {}, 手机: {}, 验证类型: {}", 
                request.getUsername(), request.getEmail(), request.getPhone(), request.getType());
            
            log.debug("【注册API】开始验证验证码...");
            verificationCodeService.registerWithVerificationCode(request);
            
            long duration = System.currentTimeMillis() - startTime;
            log.info("【注册API】✓ 用户注册成功 - 用户名: {} - 用时: {}ms", request.getUsername(), duration);
            return ApiResponse.success("注册成功，请登录");
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("【注册API】✗ 用户注册失败 - 用户名: {}, 用时: {}ms, 错误类型: {}, 错误详情: {}", 
                request.getUsername(), duration, e.getClass().getSimpleName(), e.getMessage(), e);
            
            // 返回详细的错误信息给前端
            String errorMsg = e.getMessage() != null ? e.getMessage() : "注册失败，请重试";
            return ApiResponse.error(errorMsg);
        }
    }
}
