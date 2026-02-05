package com.billmanager.jizhang.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人机验证初始化响应 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaptchaInitResponse {
    
    /**
     * 验证码密钥（前端用于验证时提交）
     */
    private String key;
    
    /**
     * 验证码背景图 Base64（可选，前端负责渲染）
     */
    private String backgroundImage;
    
    /**
     * 验证码 ID（服务器标识）
     */
    private String captchaId;
    
    /**
     * 响应消息
     */
    private String message;
}
