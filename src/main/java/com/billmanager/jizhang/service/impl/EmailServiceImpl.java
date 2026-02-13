package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

/**
 * 邮箱服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Value("${spring.application.name:AI记账管家}")
    private String applicationName;
    
    @Override
    public void sendVerificationCode(String to, String code) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(code)) {
            log.warn("【邮件】邮箱或验证码为空，无法发送邮件");
            return;
        }
        
        try {
            log.info("【邮件】开始发送验证码邮件 - 收件人: {}", to);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(applicationName + " - 邮箱验证码");
            
            // HTML 格式的邮件内容
            String htmlContent = buildVerificationCodeEmail(code);
            helper.setText(htmlContent, true);
            
            log.info("【邮件】邮件内容已准备，开始发送...");
            mailSender.send(message);
            log.info("【邮件】验证码邮件已成功发送 - 收件人: {} - 验证码: {}", to, code);
        } catch (Exception e) {
            log.error("【邮件】发送邮件失败 - 收件人: {} - 错误: {}", to, e.getMessage(), e);
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
    }
    
    @Override
    public void sendSimpleMail(String to, String subject, String content) {
        if (!StringUtils.hasText(to) || !StringUtils.hasText(subject) || !StringUtils.hasText(content)) {
            log.warn("邮件参数不完整");
            return;
        }
        
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            log.info("邮件已发送: {}", to);
        } catch (Exception e) {
            log.error("发送邮件失败: {}", to, e);
            throw new RuntimeException("发送邮件失败: " + e.getMessage());
        }
    }
    
    /**
     * 构建验证码邮件 HTML 内容
     */
    private String buildVerificationCodeEmail(String code) {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <style>\n" +
                "        body { font-family: Arial, sans-serif; color: #333; }\n" +
                "        .container { max-width: 600px; margin: 0 auto; padding: 20px; }\n" +
                "        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 20px; border-radius: 5px 5px 0 0; text-align: center; }\n" +
                "        .content { background: #f9f9f9; padding: 30px 20px; border-radius: 0 0 5px 5px; }\n" +
                "        .code-box { background: white; border: 2px dashed #667eea; padding: 20px; text-align: center; margin: 20px 0; border-radius: 5px; }\n" +
                "        .code-box .code { font-size: 36px; font-weight: bold; color: #667eea; letter-spacing: 8px; }\n" +
                "        .code-box .tip { color: #999; font-size: 12px; margin-top: 10px; }\n" +
                "        .footer { text-align: center; color: #999; font-size: 12px; margin-top: 20px; }\n" +
                "        .warning { color: #ff4757; font-size: 13px; margin: 10px 0; }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <h2>账户验证</h2>\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <p>尊敬的用户，您好！</p>\n" +
                "            <p>感谢您使用" + applicationName + "。您正在进行账户注册，请使用以下验证码：</p>\n" +
                "            \n" +
                "            <div class=\"code-box\">\n" +
                "                <div class=\"code\">" + code + "</div>\n" +
                "                <div class=\"tip\">验证码有效期：5分钟</div>\n" +
                "            </div>\n" +
                "            \n" +
                "            <p class=\"warning\">⚠️ 为了您的账户安全，请勿将验证码告诉任何人或任何机构。</p>\n" +
                "            <p class=\"warning\">⚠️ 如果您未进行此操作，请忽略此邮件。</p>\n" +
                "            \n" +
                "            <p>如有任何问题，请联系我们的客服团队。</p>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>© 2024 " + applicationName + "。保留所有权利。</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }
}
