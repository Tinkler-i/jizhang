package com.billmanager.jizhang.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 短信服务测试类
 * 
 * 注意：这是示例代码，实际使用时需要配置真实的 AppCode 和模板 ID
 */
@SpringBootTest
public class SmsServiceTest {
    
    @Autowired
    private SmsService smsService;
    
    @BeforeEach
    public void setUp() {
        // 测试前的初始化
    }
    
    /**
     * 测试发送验证码（测试模式）
     * enabled: false 时，只打印日志，不实际发送
     */
    @Test
    public void testSendVerificationCodeInTestMode() {
        // 在 application.yml 中设置 enabled: false
        String phone = "18612345678";
        String code = "1234";
        
        // 应该不抛出异常
        assertDoesNotThrow(() -> {
            smsService.sendVerificationCode(phone, code);
        });
        
        System.out.println("✓ 测试模式发送成功");
    }
    
    /**
     * 测试发送自定义消息
     */
    @Test
    public void testSendCustomMessage() {
        String phone = "18612345678";
        String content = "您的账户有异常登录，请检查！";
        
        assertDoesNotThrow(() -> {
            smsService.sendCustomMessage(phone, content);
        });
        
        System.out.println("✓ 自定义消息发送成功");
    }
    
    /**
     * 测试手机号格式验证
     */
    @Test
    public void testInvalidPhoneNumber() {
        String invalidPhone = "123456";  // 太短
        String code = "1234";
        
        // 应该抛出异常
        assertThrows(RuntimeException.class, () -> {
            smsService.sendVerificationCode(invalidPhone, code);
        });
        
        System.out.println("✓ 手机号格式验证成功");
    }
    
    /**
     * 测试空验证码
     */
    @Test
    public void testEmptyCode() {
        String phone = "18612345678";
        String code = "";
        
        assertThrows(RuntimeException.class, () -> {
            smsService.sendVerificationCode(phone, code);
        });
        
        System.out.println("✓ 空验证码检查成功");
    }
    
    /**
     * 测试生产模式（需要真实配置）
     * 注意：此测试仅在以下条件下运行：
     * 1. enabled: true
     * 2. 配置有效的 AppCode
     * 3. 配置有效的模板 ID
     */
    // @Test
    public void testSendVerificationCodeInProductMode() {
        // 此测试需要真实的阿里云配置
        // 取消注释 @Test 注解以运行
        
        String phone = "18612345678";  // 替换为真实手机号用于测试
        String code = "1234";
        
        assertDoesNotThrow(() -> {
            smsService.sendVerificationCode(phone, code);
        });
        
        System.out.println("✓ 生产模式发送成功");
        System.out.println("✓ 请检查手机是否收到短信");
    }
}
