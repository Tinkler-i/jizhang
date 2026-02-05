package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.CaptchaInitResponse;
import com.billmanager.jizhang.dto.CaptchaVerifyRequest;
import com.billmanager.jizhang.dto.CaptchaVerifyResponse;

/**
 * 人机验证码服务接口
 */
public interface CaptchaService {
    
    /**
     * 初始化验证码
     * @return 验证码初始化响应
     */
    CaptchaInitResponse initCaptcha();
    
    /**
     * 校验验证码
     * @param request 验证码校验请求
     * @return 验证码校验响应
     */
    CaptchaVerifyResponse verifyCaptcha(CaptchaVerifyRequest request);
    
    /**
     * 检查 captcha token 是否有效
     * @param token captcha token
     * @return 是否有效
     */
    boolean isValidCaptchaToken(String token);
    
    /**
     * 使用 captcha token（验证后标记为已使用）
     * @param token captcha token
     */
    void useCaptchaToken(String token);
}
