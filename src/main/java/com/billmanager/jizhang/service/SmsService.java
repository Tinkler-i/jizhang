package com.billmanager.jizhang.service;

/**
 * 短信服务接口
 */
public interface SmsService {
    
    /**
     * 发送验证码短信
     *
     * @param phone 收件人手机号
     * @param code 验证码
     */
    void sendVerificationCode(String phone, String code);
    
    /**
     * 发送自定义短信
     *
     * @param phone 收件人手机号
     * @param content 短信内容
     */
    void sendCustomMessage(String phone, String content);
}
