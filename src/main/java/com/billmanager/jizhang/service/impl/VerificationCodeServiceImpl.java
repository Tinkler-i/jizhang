package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.RegisterRequest;
import com.billmanager.jizhang.dto.SendVerificationCodeRequest;
import com.billmanager.jizhang.dto.VerifyCodeResponse;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.entity.IncomeCategory;
import com.billmanager.jizhang.entity.User;
import com.billmanager.jizhang.entity.VerificationCode;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.mapper.IncomeCategoryMapper;
import com.billmanager.jizhang.mapper.UserMapper;
import com.billmanager.jizhang.mapper.VerificationCodeMapper;
import com.billmanager.jizhang.service.EmailService;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.FamilyMemberService;
import com.billmanager.jizhang.service.PermissionService;
import com.billmanager.jizhang.service.VerificationCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * 验证码服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    private final VerificationCodeMapper verificationCodeMapper;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final FamilyGroupService familyGroupService;
    private final FamilyMemberService familyMemberService;
    private final PermissionService permissionService;
    private final IncomeCategoryMapper incomeCategoryMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final Random random = new Random();
    
    // 验证码存活时间：5分钟（300秒）
    private static final int VERIFICATION_CODE_TTL = 300;
    
    @Override
    public void sendVerificationCode(SendVerificationCodeRequest request) {
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException("验证类型不能为空");
        }
        
        String type = request.getType().toUpperCase();
        String email = request.getEmail();
        String phone = request.getPhone();
        
        log.info("【验证码】开始发送验证码 - 类型: {}", type);
        
        // 验证必填字段
        if ("EMAIL".equals(type)) {
            if (!StringUtils.hasText(email)) {
                throw new BusinessException("邮箱不能为空");
            }
            log.info("【验证码】邮箱注册 - 邮箱: {}", email);
        } else if ("SMS".equals(type)) {
            if (!StringUtils.hasText(phone)) {
                throw new BusinessException("手机号不能为空");
            }
            log.info("【验证码】短信注册 - 手机: {}", phone);
        } else {
            throw new BusinessException("不支持的验证类型");
        }
        
        // 生成验证码
        String code = generateCode();
        log.info("【验证码】已生成验证码: {}", code);
        
        // 创建验证码对象
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setPhone(phone);
        verificationCode.setCode(code);
        verificationCode.setType(type);
        verificationCode.setCreatedTime(LocalDateTime.now());
        verificationCode.setTtl(VERIFICATION_CODE_TTL);
        verificationCode.setIsUsed(0);
        
        // 保存到数据库
        verificationCodeMapper.insert(verificationCode);
        log.info("【验证码】验证码已保存到数据库");
        
        // 调用邮件/短信发送服务
        // 邮件发送
        if ("EMAIL".equals(type)) {
            sendEmailCode(email, code);
        }
        // 短信发送
        else if ("SMS".equals(type)) {
            sendSmsCode(phone, code);
        }
    }
    
    @Override
    public VerifyCodeResponse verifyCode(SendVerificationCodeRequest request) {
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
                User existUser = userMapper.findByEmail(email);
                if (existUser != null) {
                    response.setSuccess(false);
                    response.setStatus("REGISTERED");
                    response.setMessage("该邮箱已注册");
                    return response;
                }
                
                // 如果验证码非空，则验证验证码
                if (StringUtils.hasText(code)) {
                    VerificationCode verificationCode = verificationCodeMapper.findByEmailAndCode(email, code);
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
                User existUser = userMapper.findByPhone(phone);
                if (existUser != null) {
                    response.setSuccess(false);
                    response.setStatus("REGISTERED");
                    response.setMessage("该手机号已注册");
                    return response;
                }
                
                // 如果验证码非空，则验证验证码
                if (StringUtils.hasText(code)) {
                    VerificationCode verificationCode = verificationCodeMapper.findByPhoneAndCode(phone, code);
                    if (verificationCode == null || !isValidCode(verificationCode)) {
                        response.setSuccess(false);
                        response.setStatus("INVALID_CODE");
                        response.setMessage("验证码错误或已过期");
                        return response;
                    }
                }
            }
            
            response.setSuccess(true);
            response.setStatus("CAN_REGISTER");
            response.setMessage("验证码正确，可以注册");
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
    public void registerWithVerificationCode(RegisterRequest request) {
        // 检查基本信息
        if (!StringUtils.hasText(request.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (!StringUtils.hasText(request.getType())) {
            throw new BusinessException("验证类型不能为空");
        }
        if (!StringUtils.hasText(request.getCode())) {
            throw new BusinessException("验证码不能为空");
        }
        
        // 检查密码确认
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        
        // 检查用户名是否已存在
        User existByUsername = userMapper.findByUsername(request.getUsername());
        if (existByUsername != null) {
            throw new BusinessException("用户名已存在");
        }
        
        String type = request.getType().toUpperCase();
        VerificationCode verificationCode = null;
        
        if ("EMAIL".equals(type)) {
            String email = request.getEmail();
            if (!StringUtils.hasText(email)) {
                throw new BusinessException("邮箱不能为空");
            }
            // 检查邮箱是否已注册
            User existByEmail = userMapper.findByEmail(email);
            if (existByEmail != null) {
                throw new BusinessException("该邮箱已注册");
            }
            
            // 验证验证码
            verificationCode = verificationCodeMapper.findByEmailAndCode(email, request.getCode());
            if (verificationCode == null || !isValidCode(verificationCode)) {
                throw new BusinessException("验证码错误或已过期");
            }
        } else if ("SMS".equals(type)) {
            String phone = request.getPhone();
            if (!StringUtils.hasText(phone)) {
                throw new BusinessException("手机号不能为空");
            }
            // 检查手机号是否已注册
            User existByPhone = userMapper.findByPhone(phone);
            if (existByPhone != null) {
                throw new BusinessException("该手机号已注册");
            }
            
            // 验证验证码
            verificationCode = verificationCodeMapper.findByPhoneAndCode(phone, request.getCode());
            if (verificationCode == null || !isValidCode(verificationCode)) {
                throw new BusinessException("验证码错误或已过期");
            }
        } else {
            throw new BusinessException("不支持的验证类型");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setNickname(request.getUsername()); // 昵称默认为用户名
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(1);
        user.setCreateTime(LocalDateTime.now());
        user.setUpdateTime(LocalDateTime.now());
        
        userMapper.insert(user);
        
        // 【新增】为新用户创建"待分类"系统内置分类
        log.info("【注册】为新用户ID: {} 创建'待分类'分类", user.getId());
        createUnclassifiedCategories(user.getId());
        
        try {
            // 【新增】自动为新用户创建家庭组
            log.info("【注册】为新用户ID: {} 创建家庭组", user.getId());
            com.billmanager.jizhang.entity.FamilyGroup familyGroup = 
                familyGroupService.createFamilyGroup(user.getId(), "我的家庭");
            
            // 【新增】自动为用户创建家庭成员记录（管理员角色）
            log.info("【注册】为用户ID: {} 创建家庭成员，家庭组ID: {}", user.getId(), familyGroup.getId());
            com.billmanager.jizhang.entity.PermissionTemplate adminTemplate = 
                permissionService.getTemplateByName("管理员");
            
            if (adminTemplate == null) {
                log.error("【注册】找不到'管理员'权限模板，使用默认权限");
                throw new BusinessException("系统配置错误：缺少管理员权限模板");
            }
            
            familyMemberService.createFamilyMember(
                familyGroup.getId(),
                user.getId(),
                "ADMIN",
                adminTemplate
            );
            log.info("【注册】用户ID: {} 家庭组初始化完成，家庭组编号: {}", user.getId(), familyGroup.getCode());
        } catch (Exception e) {
            log.error("【注册】为用户创建家庭组失败，用户ID: {}", user.getId(), e);
            throw new BusinessException("注册失败：创建家庭组出错 - " + e.getMessage());
        }
        
        // 标记验证码已使用
        verificationCodeMapper.markAsUsed(verificationCode.getId());
    }
    
    @Override
    public boolean isValidCode(VerificationCode verificationCode) {
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
        long secondsDiff = java.time.temporal.ChronoUnit.SECONDS.between(createdTime, now);
        
        return secondsDiff <= verificationCode.getTtl();
    }
    
    @Override
    public String generateCode() {
        // 生成4位随机数字验证码
        int code = random.nextInt(10000); // 0-9999
        return String.format("%04d", code);
    }
    
    /**
     * 为新用户创建"待分类"系统内置分类
     */
    private void createUnclassifiedCategories(Long userId) {
        try {
            // 创建收入分类 - "待分类"
            IncomeCategory incomeCategory = new IncomeCategory();
            incomeCategory.setUserId(userId);
            incomeCategory.setFamilyGroupId(0L); // 0 表示个人数据，不绑定到任何家族组
            incomeCategory.setName("待分类");
            incomeCategory.setDescription("自动导入账单时未匹配分类的默认分类");
            incomeCategory.setIsBuiltIn(1); // 1 表示系统内置
            incomeCategory.setCreateTime(LocalDateTime.now());
            incomeCategory.setUpdateTime(LocalDateTime.now());
            incomeCategoryMapper.insert(incomeCategory);
            log.info("【注册】为用户ID: {} 创建收入'待分类'分类成功", userId);
            
            // 创建支出分类 - "待分类"
            ExpenseCategory expenseCategory = new ExpenseCategory();
            expenseCategory.setUserId(userId);
            expenseCategory.setFamilyGroupId(0L); // 0 表示个人数据，不绑定到任何家族组
            expenseCategory.setName("待分类");
            expenseCategory.setDescription("自动导入账单时未匹配分类的默认分类");
            expenseCategory.setIsBuiltIn(1); // 1 表示系统内置
            expenseCategory.setCreateTime(LocalDateTime.now());
            expenseCategory.setUpdateTime(LocalDateTime.now());
            expenseCategoryMapper.insert(expenseCategory);
            log.info("【注册】为用户ID: {} 创建支出'待分类'分类成功", userId);
        } catch (Exception e) {
            log.error("【注册】为用户ID: {} 创建'待分类'分类失败", userId, e);
            throw new BusinessException("注册失败：创建系统分类出错 - " + e.getMessage());
        }
    }
    
    /**
     * 发送邮件验证码
     */
    private void sendEmailCode(String email, String code) {
        try {
            emailService.sendVerificationCode(email, code);
            log.info("邮箱验证码已发送: {}", email);
        } catch (Exception e) {
            log.error("邮箱验证码发送失败: {}", email, e);
            // 邮件发送失败不中断流程，用户仍然可以重试
        }
    }
    
    /**
     * 发送短信验证码（预留接口）
     */
    private void sendSmsCode(String phone, String code) {
        // TODO: 调用短信服务发送验证码
        // 示例日志
        System.out.println("发送短信验证码: " + phone + ", 验证码: " + code);
    }
}
