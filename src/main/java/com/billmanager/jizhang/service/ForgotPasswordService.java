package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.ForgotPasswordRequest;
import com.billmanager.jizhang.dto.ResetPasswordRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;

/**
 * 忘记密码服务接口
 */
public interface ForgotPasswordService {
    
    /**
     * 发送密码重置验证码
     *
     * @param request 请求参数（包含邮箱或手机号）
     */
    void sendResetPasswordCode(ForgotPasswordRequest request);
    
    /**
     * 验证密码重置验证码
     *
     * @param request 请求参数（包含邮箱/手机号和验证码）
     * @return 验证结果
     */
    VerifyCodeResponse verifyResetPasswordCode(ForgotPasswordRequest request);
    
    /**
     * 重置密码
     *
     * @param request 请求参数（包含邮箱/手机号、验证码、新密码等）
     */
    void resetPassword(ResetPasswordRequest request);
}
