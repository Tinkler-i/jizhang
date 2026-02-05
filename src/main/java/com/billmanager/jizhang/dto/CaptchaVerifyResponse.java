package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人机验证响应 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaVerifyResponse {
    
    /**
     * 验证是否通过
     */
    private boolean success;
    
    /**
     * 状态码 (0=成功, 其他=失败) - 库可能期望这个字段
     */
    private int code;
    
    /**
     * 验证码密钥
     */
    private String key;
    
    /**
     * 验证通过后的 token（用于后续操作）
     */
    private String token;
    
    /**
     * 验证信息
     */
    private String message;
}
