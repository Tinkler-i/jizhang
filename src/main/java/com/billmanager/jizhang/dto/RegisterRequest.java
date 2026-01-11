package com.billmanager.jizhang.dto;

import lombok.Data;

/**
 * 注册请求
 */
@Data
public class RegisterRequest {
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 确认密码
     */
    private String confirmPassword;
    
    /**
     * 邮箱（邮箱注册时必填）
     */
    private String email;
    
    /**
     * 手机号（短信注册时必填）
     */
    private String phone;
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 注册类型：EMAIL、SMS
     */
    private String type;
    
    /**
     * 滑动验证码 token（服务端存储此token，验证成功后删除）
     */
    private String captchaToken;
}
