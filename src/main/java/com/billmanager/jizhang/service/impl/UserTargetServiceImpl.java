package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.entity.UserTarget;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserTargetMapper;
import com.billmanager.jizhang.service.UserTargetService;
import com.billmanager.jizhang.service.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户目标服务实现
 * 处理用户收入目标的业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserTargetServiceImpl implements UserTargetService {
    
    private final UserTargetMapper userTargetMapper;
    private final FamilyGroupService familyGroupService;
    
    @Override
    public UserTarget findById(Long id) {
        return userTargetMapper.findById(id);
    }
    
    @Override
    public UserTarget findByUserIdAndMonth(Long userId, String targetMonth) {
        return userTargetMapper.findByUserIdAndMonth(userId, targetMonth);
    }
    
    @Override
    public List<UserTarget> findByUserId(Long userId) {
        return userTargetMapper.findByUserId(userId);
    }
    
    @Override
    @Transactional
    public UserTarget createOrUpdate(Long userId, String targetMonth, BigDecimal incomeTarget) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        if (targetMonth == null || !targetMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("目标年月格式不正确，应为 YYYY-MM");
        }
        
        if (incomeTarget == null || incomeTarget.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("收入目标不能为空或小于0");
        }
        
        // 查询是否已存在
        UserTarget existing = userTargetMapper.findByUserIdAndMonth(userId, targetMonth);
        
        if (existing != null) {
            // 更新现有目标
            existing.setIncomeTarget(incomeTarget);
            return update(existing);
        } else {
            // 创建新目标
            UserTarget newTarget = new UserTarget();
            newTarget.setUserId(userId);
            newTarget.setTargetMonth(targetMonth);
            newTarget.setIncomeTarget(incomeTarget);
            return create(newTarget);
        }
    }
    
    @Override
    @Transactional
    public UserTarget create(UserTarget userTarget) {
        if (userTarget.getUserId() == null || userTarget.getUserId() <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        if (userTarget.getTargetMonth() == null || !userTarget.getTargetMonth().matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("目标年月格式不正确，应为 YYYY-MM");
        }
        
        if (userTarget.getIncomeTarget() == null || userTarget.getIncomeTarget().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("收入目标不能为空或小于0");
        }
        
        // 确保familyGroupId有值（个人目标设为0）
        if (userTarget.getFamilyGroupId() == null) {
            userTarget.setFamilyGroupId(0L);
        }
        
        // 检查是否已存在相同的目标
        UserTarget existing = userTargetMapper.findByUserIdAndMonth(
                userTarget.getUserId(), 
                userTarget.getTargetMonth());
        if (existing != null) {
            throw new BusinessException("该月份的目标已存在，请编辑修改或删除后重试");
        }
        
        // 如果familyGroupId为0（个人目标），保持不变
        // 如果familyGroupId>0（家庭目标），检查用户是否在该家庭组中
        if (userTarget.getFamilyGroupId() > 0) {
            try {
                var familyGroup = familyGroupService.getFamilyGroupByUserId(userTarget.getUserId());
                if (familyGroup == null || !familyGroup.getId().equals(userTarget.getFamilyGroupId())) {
                    throw new BusinessException("用户不在指定的家庭组中");
                }
            } catch (Exception e) {
                log.debug("【目标】检查家族组权限失败", e);
                throw new BusinessException("检查家族组权限失败");
            }
        }
        
        int result = userTargetMapper.insert(userTarget);
        if (result > 0) {
            log.info("【目标创建】用户ID: {}, 目标年月: {}, 收入目标: {}, familyGroupId: {}", 
                    userTarget.getUserId(), userTarget.getTargetMonth(), userTarget.getIncomeTarget(), userTarget.getFamilyGroupId());
            return userTarget;
        } else {
            throw new BusinessException("创建目标失败");
        }
    }
    
    @Override
    @Transactional
    public UserTarget update(UserTarget userTarget) {
        if (userTarget.getId() == null || userTarget.getId() <= 0) {
            throw new BusinessException("目标ID无效");
        }
        
        if (userTarget.getIncomeTarget() == null || userTarget.getIncomeTarget().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("收入目标不能为空或小于0");
        }
        
        int result = userTargetMapper.update(userTarget);
        if (result > 0) {
            log.info("【目标更新】目标ID: {}, 收入目标: {}", userTarget.getId(), userTarget.getIncomeTarget());
            return userTarget;
        } else {
            throw new BusinessException("更新目标失败或目标不存在");
        }
    }
    
    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        if (id == null || id <= 0) {
            throw new BusinessException("目标ID无效");
        }
        
        UserTarget target = userTargetMapper.findById(id);
        if (target == null) {
            throw new BusinessException("目标不存在");
        }
        
        // 验证目标归属于指定用户或用户所在的家庭组
        boolean isOwner = target.getUserId().equals(userId);
        boolean isFamilyTarget = target.getFamilyGroupId() != null && target.getFamilyGroupId() > 0;
        
        if (isFamilyTarget) {
            // 家庭目标：需要检查用户是否在该家庭组中
            try {
                var familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
                if (familyGroup == null || !familyGroup.getId().equals(target.getFamilyGroupId())) {
                    throw new BusinessException("无权删除该目标");
                }
            } catch (Exception e) {
                log.error("【目标】验证家族组权限失败", e);
                throw new BusinessException("无权删除该目标");
            }
        } else {
            // 个人目标：只有创建者能删除
            if (!isOwner) {
                throw new BusinessException("无权删除该目标");
            }
        }
        
        int result = userTargetMapper.delete(id);
        if (result > 0) {
            log.info("【目标删除】目标ID: {}, 用户ID: {}", id, userId);
        } else {
            throw new BusinessException("删除目标失败");
        }
    }
    
    @Override
    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new BusinessException("目标ID无效");
        }
        
        UserTarget target = userTargetMapper.findById(id);
        if (target == null) {
            throw new BusinessException("目标不存在");
        }
        
        int result = userTargetMapper.delete(id);
        if (result > 0) {
            log.info("【目标删除】目标ID: {}", id);
        } else {
            throw new BusinessException("删除目标失败");
        }
    }
    
    @Override
    public List<UserTarget> findByUserIdAndMonthRange(Long userId, String startMonth, String endMonth) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        if (startMonth == null || !startMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("起始年月格式不正确，应为 YYYY-MM");
        }
        
        if (endMonth == null || !endMonth.matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("结束年月格式不正确，应为 YYYY-MM");
        }
        
        // 验证起始月份不晚于结束月份
        if (startMonth.compareTo(endMonth) > 0) {
            throw new BusinessException("起始年月不能晚于结束年月");
        }
        
        return userTargetMapper.findByUserIdAndMonthRange(userId, startMonth, endMonth);
    }
}
