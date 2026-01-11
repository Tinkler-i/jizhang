package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.RegisterRequest;
import com.billmanager.jizhang.dto.SendVerificationCodeRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
import com.billmanager.jizhang.entity.VerificationCode;

/**
 * 验证码服务接口
 */
public interface VerificationCodeService {
    
    /**
     * 发送验证码
     */
    void sendVerificationCode(SendVerificationCodeRequest request);
    
    /**
     * 验证验证码（用于注册前验证邮箱/手机是否已注册）
     */
    VerifyCodeResponse verifyCode(SendVerificationCodeRequest request);
    
    /**
     * 使用验证码进行注册
     */
    void registerWithVerificationCode(RegisterRequest request);
    
    /**
     * 检查验证码是否有效（未过期、未使用）
     */
    boolean isValidCode(VerificationCode verificationCode);
    
    /**
     * 生成4位随机验证码
     */
    String generateCode();
}
