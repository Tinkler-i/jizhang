package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.ForgotPasswordRequest;
import com.billmanager.jizhang.dto.ResetPasswordRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.entity.VerificationCode;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.mapper.VerificationCodeMapper;
import com.billmanager.jizhang.service.EmailService;
import com.billmanager.jizhang.service.ForgotPasswordService;
import com.billmanager.jizhang.service.SmsService;
import com.billmanager.jizhang.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 忘记密码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {
    
    private final UserMapper userMapper;
    private final VerificationCodeMapper verificationCodeMapper;
    private final VerificationCodeService verificationCodeService;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SmsService smsService;
    
    // 密码重置验证码标记
    private static final String RESET_PASSWORD_TYPE_PREFIX = "RESET_";
    
    @Override
    public void sendResetPasswordCode(ForgotPasswordRequest request) {
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException("验证类型不能为空");
        }
        
        String type = request.getType().toUpperCase();
        String email = request.getEmail();
        String phone = request.getPhone();
        
        log.info("【密码重置】开始发送密码重置验证码 - 类型: {}", type);
        
        // 验证必填字段并检查用户是否存在
        if ("EMAIL".equals(type)) {
            if (!StringUtils.hasText(email)) {
                throw new BusinessException("邮箱不能为空");
            }
            
            // 检查邮箱是否已注册
            User user = userMapper.findByEmail(email);
            if (user == null) {
                // 出于安全考虑，不透露用户是否存在
                log.warn("【密码重置】邮箱未注册: {}", email);
                throw new BusinessException("该邮箱未注册");
            }
            
            log.info("【密码重置】邮箱重置密码 - 邮箱: {}", email);
        } else if ("SMS".equals(type)) {
            if (!StringUtils.hasText(phone)) {
                throw new BusinessException("手机号不能为空");
            }
            
            // 检查手机号是否已注册
            User user = userMapper.findByPhone(phone);
            if (user == null) {
                log.warn("【密码重置】手机号未注册: {}", phone);
                throw new BusinessException("该手机号未注册");
            }
            
            log.info("【密码重置】短信重置密码 - 手机: {}", phone);
        } else {
            throw new BusinessException("不支持的验证类型");
        }
        
        // 生成验证码
        String code = verificationCodeService.generateCode();
        log.info("【密码重置】已生成验证码: {}", code);
        
        // 创建验证码对象（标记为密码重置用）
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(RESET_PASSWORD_TYPE_PREFIX + type);
        verificationCode.setCreatedTime(LocalDateTime.now());
        verificationCode.setTtl(300);  // 5分钟有效期
        verificationCode.setIsUsed(0);
        
        // 保存到数据库
        verificationCodeMapper.insert(verificationCode);
        log.info("【密码重置】验证码已保存到数据库");
        
        // 发送邮件或短信
        if ("EMAIL".equals(type)) {
            sendResetPasswordEmail(email, code);
        } else if ("SMS".equals(type)) {
            sendResetPasswordSms(phone, code);
        }
    }
    
    @Override
    public VerifyCodeResponse verifyResetPasswordCode(ForgotPasswordRequest request) {
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException("验证类型不能为空");
        }
        
        String type = request.getType().toUpperCase();
        String email = request.getEmail();
        String phone = request.getPhone();
        String code = request.getCode();
        
        VerifyCodeResponse response = new VerifyCodeResponse();
        
        try {
            if ("EMAIL".equals(type)) {
                if (!StringUtils.hasText(email)) {
                    throw new BusinessException("邮箱不能为空");
                }
                
                // 检查邮箱是否已注册
                User user = userMapper.findByEmail(email);
                if (user == null) {
                    response.setSuccess(false);
                    response.setStatus("NOT_REGISTERED");
                    response.setMessage("该邮箱未注册");
                    return response;
                }
                
                // 如果验证码非空，则验证验证码
                if (StringUtils.hasText(code)) {
                    VerificationCode verificationCode = verificationCodeMapper.findByEmailAndCode(email, code);
                    
                    // 检查是否是密码重置验证码
                    if (verificationCode != null && !verificationCode.getType().startsWith(RESET_PASSWORD_TYPE_PREFIX)) {
                        response.setSuccess(false);
                        response.setStatus("INVALID_CODE");
                        response.setMessage("验证码类型不匹配");
                        return response;
                    }
                    
                    if (verificationCode == null || !isValidCode(verificationCode)) {
                        response.setSuccess(false);
                        response.setStatus("INVALID_CODE");
                        response.setMessage("验证码错误或已过期");
                        return response;
                    }
                }
            } else if ("SMS".equals(type)) {
                if (!StringUtils.hasText(phone)) {
                    throw new BusinessException("手机号不能为空");
                }
                
                // 检查手机号是否已注册
                User user = userMapper.findByPhone(phone);
                if (user == null) {
                    response.setSuccess(false);
                    response.setStatus("NOT_REGISTERED");
                    response.setMessage("该手机号未注册");
                    return response;
                }
                
                // 如果验证码非空，则验证验证码
                if (StringUtils.hasText(code)) {
                    VerificationCode verificationCode = verificationCodeMapper.findByPhoneAndCode(phone, code);
                    
                    // 检查是否是密码重置验证码
                    if (verificationCode != null && !verificationCode.getType().startsWith(RESET_PASSWORD_TYPE_PREFIX)) {
                        response.setSuccess(false);
                        response.setStatus("INVALID_CODE");
                        response.setMessage("验证码类型不匹配");
                        return response;
                    }
                    
                    if (verificationCode == null || !isValidCode(verificationCode)) {
                        response.setSuccess(false);
                        response.setStatus("INVALID_CODE");
                        response.setMessage("验证码错误或已过期");
                        return response;
                    }
                }
            }
            
            response.setSuccess(true);
            response.setStatus("CAN_RESET_PASSWORD");
            response.setMessage("验证码正确，可以重置密码");
            return response;
        } catch (BusinessException e) {
            response.setSuccess(false);
            response.setStatus("ERROR");
            response.setMessage(e.getMessage());
            return response;
        }
    }
    
    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        // 基本验证
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException("验证类型不能为空");
        }
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException("验证码不能为空");
        }
        if (!StringUtils.hasText(request.getNewPassword())) {
            throw new BusinessException("新密码不能为空");
        }
        if (!StringUtils.hasText(request.getConfirmPassword())) {
            throw new BusinessException("确认密码不能为空");
        }
        
        // 检查密码一致性
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 检查密码强度（至少6位）
        if (request.getNewPassword().length() < 6) {
            throw new BusinessException("密码长度至少为6位");
        }
        
        String type = request.getType().toUpperCase();
        User user = null;
        VerificationCode verificationCode = null;
        
        if ("EMAIL".equals(type)) {
            String email = request.getEmail();
            if (!StringUtils.hasText(email)) {
                throw new BusinessException("邮箱不能为空");
            }
            
            // 查找用户
            user = userMapper.findByEmail(email);
            if (user == null) {
                throw new BusinessException("该邮箱未注册");
            }
            
            // 验证验证码
            verificationCode = verificationCodeMapper.findByEmailAndCode(email, request.getCode());
            if (verificationCode == null || !isValidCode(verificationCode)) {
                throw new BusinessException("验证码错误或已过期");
            }
            
            // 检查验证码类型
            if (!verificationCode.getType().startsWith(RESET_PASSWORD_TYPE_PREFIX)) {
                throw new BusinessException("验证码类型不匹配");
            }
        } else if ("SMS".equals(type)) {
            String phone = request.getPhone();
            if (!StringUtils.hasText(phone)) {
                throw new BusinessException("手机号不能为空");
            }
            
            // 查找用户
            user = userMapper.findByPhone(phone);
            if (user == null) {
                throw new BusinessException("该手机号未注册");
            }
            
            // 验证验证码
            verificationCode = verificationCodeMapper.findByPhoneAndCode(phone, request.getCode());
            if (verificationCode == null || !isValidCode(verificationCode)) {
                throw new BusinessException("验证码错误或已过期");
            }
            
            // 检查验证码类型
            if (!verificationCode.getType().startsWith(RESET_PASSWORD_TYPE_PREFIX)) {
                throw new BusinessException("验证码类型不匹配");
            }
        } else {
            throw new BusinessException("不支持的验证类型");
        }
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdateTime(LocalDateTime.now());
        userMapper.update(user);
        
        log.info("【密码重置】用户密码已更新 - 用户ID: {}", user.getId());
        
        // 标记验证码已使用
        verificationCodeMapper.markAsUsed(verificationCode.getId());
        log.info("【密码重置】验证码已标记为已使用 - 验证码ID: {}", verificationCode.getId());
    }
    
    /**
     * 检查验证码是否有效
     */
    private boolean isValidCode(VerificationCode verificationCode) {
        if (verificationCode == null) {
            return false;
        }
        
        // 检查是否已使用
        if (verificationCode.getIsUsed() == 1) {
            return false;
        }
        
        // 检查是否过期
        LocalDateTime createdTime = verificationCode.getCreatedTime();
        LocalDateTime now = LocalDateTime.now();
        long secondsDiff = ChronoUnit.SECONDS.between(createdTime, now);
        
        return secondsDiff <= verificationCode.getTtl();
    }
    
    /**
     * 发送密码重置邮件
     */
    private void sendResetPasswordEmail(String email, String code) {
        try {
            emailService.sendVerificationCode(email, code);
            log.info("【密码重置】密码重置邮件已发送: {}", email);
        } catch (Exception e) {
            log.error("【密码重置】密码重置邮件发送失败: {}", email, e);
            // 邮件发送失败不中断流程
        }
    }
    
    /**
     * 发送密码重置短信
     */
    private void sendResetPasswordSms(String phone, String code) {
        try {
            smsService.sendVerificationCode(phone, code);
            log.info("【密码重置】密码重置短信已发送: {}", phone);
        } catch (Exception e) {
            log.error("【密码重置】密码重置短信发送失败: {}", phone, e);
            // 短信发送失败不中断流程
        }
    }
}
