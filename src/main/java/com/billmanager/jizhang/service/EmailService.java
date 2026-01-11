package com.billmanager.jizhang.service;

/**
 * 邮箱服务接口
 */
public interface EmailService {
    
    /**
     * 发送验证码邮件
     *
     * @param to 收件人邮箱
     * @param code 验证码
     */
    void sendVerificationCode(String to, String code);
    
    /**
     * 发送文本邮件
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     */
    void sendSimpleMail(String to, String subject, String content);
}
