package com.billmanager.jizhang.entity;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationCode {
    private Long id;
    private String email;
    private String phone;
    private String code;
    private String type; // EMAIL, SMS
    private LocalDateTime createdTime;
    private Integer ttl; // 生存时间（秒），默认300秒（5分钟）
    private Integer isUsed; // 0-未使用，1-已使用
}
