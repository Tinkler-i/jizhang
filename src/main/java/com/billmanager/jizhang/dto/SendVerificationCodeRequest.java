package com.billmanager.jizhang.dto;

import lombok.Data;

/**
 * 发送验证码请求
 */
@Data
public class SendVerificationCodeRequest {
    
    /**
     * 邮箱（邮箱注册时必填）
     */
    private String email;
    
    /**
     * 手机号（短信注册时必填）
     */
    private String phone;
    
    /**
     * 验证类型：EMAIL、SMS
     */
    private String type;
    
    /**
     * 验证码（在验证验证码正确性时使用）
     */
    private String code;
    
    /**
     * 滑动验证码 token
     */
    private String captchaToken;
}
