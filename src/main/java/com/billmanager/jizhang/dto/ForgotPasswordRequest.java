package com.billmanager.jizhang.dto;

import lombok.Data;

/**
 * 忘记密码 - 请求验证码 DTO
 */
@Data
public class ForgotPasswordRequest {
    
    /**
     * 验证类型：EMAIL 或 SMS
     */
    private String type;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 验证码（可选，用于验证验证码时需要）
     */
    private String code;
    
    /**
     * 人机验证 token
     */
    private String captchaToken;
}
