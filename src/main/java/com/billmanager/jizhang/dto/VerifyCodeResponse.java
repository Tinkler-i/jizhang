package com.billmanager.jizhang.dto;

import lombok.Data;

/**
 * 验证码验证响应
 */
@Data
public class VerifyCodeResponse {
    
    /**
     * 是否验证成功
     */
    private Boolean success;
    
    /**
     * 用户信息或错误信息
     */
    private String message;
    
    /**
     * 用户状态：REGISTERED（已注册）、CAN_REGISTER（可以注册）
     */
    private String status;
}
