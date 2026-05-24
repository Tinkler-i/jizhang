package com.billmanager.jizhang.dto;

import lombok.Data;

/**
 * 人机验证请求 DTO
 */
@Data
public class CaptchaVerifyRequest {
    
    /**
     * 验证码密钥（用于前后端对应关系）
     */
    private String key;
    
    /**
     * 用户拖动的距离（单位：像素）
     */
    private Integer moveDistance;
    
    /**
     * 验证码唯一标识
     */
    private String captchaId;
    
    /**
     * 校验时间戳
     */
    private Long timestamp;
}
