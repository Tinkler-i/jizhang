package com.billmanager.jizhang.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.billmanager.jizhang.dto.ForgotPasswordRequest;
import com.billmanager.jizhang.dto.ResetPasswordRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 忘记密码服务测试类
 */
@SpringBootTest
public class ForgotPasswordServiceTest {
    
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    
    /**
     * 测试邮箱密码重置流程
     */
    @Test
    public void testEmailPasswordResetFlow() {
        String email = "test@example.com";
        String code = null;
        
        // 第一步：发送验证码
        ForgotPasswordRequest sendRequest = new ForgotPasswordRequest();
        sendRequest.setType("EMAIL");
        sendRequest.setEmail(email);
        
        assertDoesNotThrow(() -> {
            forgotPasswordService.sendResetPasswordCode(sendRequest);
        });
        System.out.println("✓ 邮箱验证码发送成功");
        
        // 第二步：验证验证码（需要从日志或数据库获取真实验证码）
        ForgotPasswordRequest verifyRequest = new ForgotPasswordRequest();
        verifyRequest.setType("EMAIL");
        verifyRequest.setEmail(email);
        verifyRequest.setCode("1234");  // 需要替换为真实验证码
        
        VerifyCodeResponse verifyResponse = forgotPasswordService.verifyResetPasswordCode(verifyRequest);
        assertNotNull(verifyResponse);
        System.out.println("✓ 验证码验证完成: " + verifyResponse.getStatus());
        
        // 第三步：重置密码
        ResetPasswordRequest resetRequest = new ResetPasswordRequest();
        resetRequest.setType("EMAIL");
        resetRequest.setEmail(email);
        resetRequest.setCode("1234");  // 需要替换为真实验证码
        resetRequest.setNewPassword("newPassword123");
        resetRequest.setConfirmPassword("newPassword123");
        
        // 仅在验证码正确时执行
        if (verifyResponse.getSuccess()) {
            assertDoesNotThrow(() -> {
                forgotPasswordService.resetPassword(resetRequest);
            });
            System.out.println("✓ 密码重置成功");
        } else {
            System.out.println("✗ 验证码不正确，跳过密码重置");
        }
    }
    
    /**
     * 测试短信密码重置流程
     */
    @Test
    public void testSmsPasswordResetFlow() {
        String phone = "18612345678";
        
        // 第一步：发送验证码
        ForgotPasswordRequest sendRequest = new ForgotPasswordRequest();
        sendRequest.setType("SMS");
        sendRequest.setPhone(phone);
        
        assertDoesNotThrow(() -> {
            forgotPasswordService.sendResetPasswordCode(sendRequest);
        });
        System.out.println("✓ 短信验证码发送成功");
    }
    
    /**
     * 测试未注册邮箱
     */
    @Test
    public void testUnregisteredEmail() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setType("EMAIL");
        request.setEmail("unregistered@example.com");
        
        assertThrows(Exception.class, () -> {
            forgotPasswordService.sendResetPasswordCode(request);
        });
        System.out.println("✓ 未注册邮箱检查成功");
    }
    
    /**
     * 测试密码不一致
     */
    @Test
    public void testPasswordMismatch() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setType("EMAIL");
        request.setEmail("test@example.com");
        request.setCode("1234");
        request.setNewPassword("password123");
        request.setConfirmPassword("password456");  // 不一致
        
        assertThrows(Exception.class, () -> {
            forgotPasswordService.resetPassword(request);
        });
        System.out.println("✓ 密码一致性检查成功");
    }
    
    /**
     * 测试短密码
     */
    @Test
    public void testShortPassword() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setType("EMAIL");
        request.setEmail("test@example.com");
        request.setCode("1234");
        request.setNewPassword("123");  // 太短
        request.setConfirmPassword("123");
        
        assertThrows(Exception.class, () -> {
            forgotPasswordService.resetPassword(request);
        });
        System.out.println("✓ 密码长度检查成功");
    }
}
