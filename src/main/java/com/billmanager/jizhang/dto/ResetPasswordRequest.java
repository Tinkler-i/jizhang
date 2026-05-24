package com.billmanager.jizhang.dto;

import lombok.Data;

/**
 * 忘记密码 - 重置密码 DTO
 */
@Data
public class ResetPasswordRequest {
    
    /**
     * 验证类型：EMAIL 或 SMS
     */
    private String type;
    
    /**
     * 邮箱或手机号
     */
    private String email;
    private String phone;
    
    /**
     * 验证码
     */
    private String code;
    
    /**
     * 新密码
     */
    private String newPassword;
    
    /**
     * 确认密码
     */
    private String confirmPassword;
}
