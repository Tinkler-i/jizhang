package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.entity.UserTarget;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.UserTargetMapper;
import com.billmanager.jizhang.service.UserTargetService;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.PermissionService;
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
    private final PermissionService permissionService;
    
    @Override
    public UserTarget findById(Long id) {
        return userTargetMapper.findById(id);
    }
    
    @Override
    public UserTarget findByUserIdAndMonth(Long userId, String targetMonth) {
        // 先检查用户是否在家庭组中
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null && familyGroup.getId() > 0) {
                // 用户在家庭组中，返回该家庭组的汇总目标
                return userTargetMapper.findByFamilyGroupIdAndMonthAggregated(familyGroup.getId(), targetMonth);
            }
        } catch (Exception e) {
            log.debug("【目标查询】查询用户ID: {} 的家庭组失败: {}", userId, e.getMessage());
        }
        
        // 用户不在家庭组中，返回个人目标
        return userTargetMapper.findByUserIdAndMonth(userId, targetMonth);
    }
    
    @Override
    public List<UserTarget> findByUserId(Long userId) {
        // 先检查用户是否在家庭组中
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null && familyGroup.getId() > 0) {
                // 用户在家庭组中，返回该家庭组的聚合目标 + 个人目标
                List<UserTarget> familyTargets = userTargetMapper.findByFamilyGroupIdAggregated(familyGroup.getId());
                List<UserTarget> personalTargets = userTargetMapper.findByUserIdAndMonthRange(userId, "1900-01", "2999-12");
                
                // 过滤个人目标（只保留family_group_id = 0的）
                personalTargets.removeIf(t -> t.getFamilyGroupId() != null && t.getFamilyGroupId() > 0);
                
                // 合并两个列表
                familyTargets.addAll(personalTargets);
                
                // 按月份倒序排列
                familyTargets.sort((a, b) -> b.getTargetMonth().compareTo(a.getTargetMonth()));
                
                log.debug("【目标查询】用户ID: {} 在家庭组ID: {} 中，返回家庭组聚合目标 {} 条 + 个人目标 {} 条", 
                        userId, familyGroup.getId(), 
                        userTargetMapper.findByFamilyGroupIdAggregated(familyGroup.getId()).size(),
                        personalTargets.size());
                
                return familyTargets;
            }
        } catch (Exception e) {
            log.debug("【目标查询】查询用户ID: {} 的家庭组失败: {}", userId, e.getMessage());
        }
        
        // 用户不在家庭组中，返回所有个人目标
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
        
        // 检查用户是否在家庭组中
        FamilyGroup familyGroup = null;
        try {
            familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        } catch (Exception e) {
            log.debug("【目标修改】查询用户ID: {} 的家庭组失败: {}", userId, e.getMessage());
        }
        
        // 如果用户在家庭组中，删除该月份的所有家庭组目标，然后新建
        if (familyGroup != null && familyGroup.getId() > 0) {
            // 删除该家庭组该月份的所有用户目标
            int deletedCount = userTargetMapper.deleteByFamilyGroupIdAndMonth(familyGroup.getId(), targetMonth);
            if (deletedCount > 0) {
                log.info("【目标修改】删除家庭组ID: {} 月份 {} 的 {} 条旧目标数据", familyGroup.getId(), targetMonth, deletedCount);
            }
            
            // 创建新的家庭组目标
            UserTarget newTarget = new UserTarget();
            newTarget.setUserId(userId);
            newTarget.setTargetMonth(targetMonth);
            newTarget.setIncomeTarget(incomeTarget);
            newTarget.setFamilyGroupId(familyGroup.getId());
            
            log.info("【目标修改】用户ID: {} 在家庭组ID: {} 中创建新目标，月份: {}, 金额: {}", 
                    userId, familyGroup.getId(), targetMonth, incomeTarget);
            
            return create(newTarget);
        } else {
            // 用户不在家庭组中，按原逻辑处理个人目标
            UserTarget existing = userTargetMapper.findByUserIdAndMonth(userId, targetMonth);
            
            if (existing != null) {
                // 更新现有的个人目标
                existing.setIncomeTarget(incomeTarget);
                return update(existing);
            } else {
                // 创建新的个人目标
                UserTarget newTarget = new UserTarget();
                newTarget.setUserId(userId);
                newTarget.setTargetMonth(targetMonth);
                newTarget.setIncomeTarget(incomeTarget);
                newTarget.setFamilyGroupId(0L);
                
                return create(newTarget);
            }
        }
    }
    
    @Override
    @Transactional
    public UserTarget create(UserTarget userTarget) {
        if (userTarget.getUserId() == null || userTarget.getUserId() <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        // 【新增】检查权限：用户是否有target_edit权限
        if (!permissionService.canEdit(userTarget.getUserId(), "target")) {
            throw new BusinessException("无权创建目标");
        }
        
        if (userTarget.getTargetMonth() == null || !userTarget.getTargetMonth().matches("^\\d{4}-\\d{2}$")) {
            throw new BusinessException("目标年月格式不正确，应为 YYYY-MM");
        }
        
        if (userTarget.getIncomeTarget() == null || userTarget.getIncomeTarget().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("收入目标不能为空或小于0");
        }
        
        // 确保familyGroupId有值，如果为null或0都需要检测
        if (userTarget.getFamilyGroupId() == null || userTarget.getFamilyGroupId() == 0) {
            // 自动检查用户是否在家庭组中，如果在则使用家庭组ID，否则使用个人目标(0)
            try {
                FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userTarget.getUserId());
                if (familyGroup != null) {
                    userTarget.setFamilyGroupId(familyGroup.getId());
                    log.info("【目标创建】用户ID: {} 在家庭组ID: {} 中，设置目标为家庭目标", 
                            userTarget.getUserId(), familyGroup.getId());
                } else {
                    userTarget.setFamilyGroupId(0L);
                    log.info("【目标创建】用户ID: {} 不在家庭组中，设置为个人目标(familyGroupId: 0)", userTarget.getUserId());
                }
            } catch (Exception e) {
                // 如果查询失败，设为个人目标
                userTarget.setFamilyGroupId(0L);
                log.warn("【目标创建】查询用户ID: {} 的家庭组失败: {}", userTarget.getUserId(), e.getMessage());
            }
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
        
        // 【新增】获取原目标信息以进行权限检查
        UserTarget original = userTargetMapper.findById(userTarget.getId());
        if (original == null) {
            throw new BusinessException("目标不存在");
        }
        
        // 【新增】检查权限：目标所有者是否有target_edit权限
        if (!permissionService.canEdit(original.getUserId(), "target")) {
            throw new BusinessException("无权修改目标");
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
        
        // 【改进】检查权限：用户是否有target_edit权限
        if (!permissionService.canEdit(userId, "target")) {
            throw new BusinessException("无权删除目标");
        }
        
        UserTarget target = userTargetMapper.findById(id);
        if (target == null) {
            throw new BusinessException("目标不存在");
        }
        
        // 检查是否是家庭组目标
        boolean isFamilyTarget = target.getFamilyGroupId() != null && target.getFamilyGroupId() > 0;
        
        if (isFamilyTarget) {
            // 【家庭组目标删除逻辑】
            // 验证用户是否在该家庭组中
            try {
                var userFamilyGroup = familyGroupService.getFamilyGroupByUserId(userId);
                if (userFamilyGroup == null || !userFamilyGroup.getId().equals(target.getFamilyGroupId())) {
                    throw new BusinessException("无权删除该目标");
                }
            } catch (BusinessException e) {
                throw e;
            } catch (Exception e) {
                log.error("【目标】验证家族组权限失败", e);
                throw new BusinessException("无权删除该目标");
            }
            
            // 删除该家庭组该月份的所有目标（与createOrUpdate保持一致）
            String targetMonth = target.getTargetMonth();
            Long familyGroupId = target.getFamilyGroupId();
            
            int deletedCount = userTargetMapper.deleteByFamilyGroupIdAndMonth(familyGroupId, targetMonth);
            log.info("【目标删除】删除家庭组ID: {} 月份 {} 的 {} 条目标数据", familyGroupId, targetMonth, deletedCount);
            
            if (deletedCount == 0) {
                throw new BusinessException("删除目标失败");
            }
        } else {
            // 【个人目标删除逻辑】
            // 只有创建者能删除
            if (!target.getUserId().equals(userId)) {
                throw new BusinessException("无权删除该目标");
            }
            
            int result = userTargetMapper.delete(id);
            if (result > 0) {
                log.info("【目标删除】个人目标ID: {}, 用户ID: {}", id, userId);
            } else {
                throw new BusinessException("删除目标失败");
            }
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
        
        // 先检查用户是否在家庭组中
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null && familyGroup.getId() > 0) {
                // 用户在家庭组中，返回该家庭组的聚合目标 + 个人目标
                List<UserTarget> familyTargets = userTargetMapper.findByFamilyGroupIdAndMonthRangeAggregated(
                        familyGroup.getId(), startMonth, endMonth);
                List<UserTarget> personalTargets = userTargetMapper.findByUserIdAndMonthRange(userId, startMonth, endMonth);
                
                // 过滤个人目标（只保留family_group_id = 0的）
                personalTargets.removeIf(t -> t.getFamilyGroupId() != null && t.getFamilyGroupId() > 0);
                
                // 合并两个列表
                familyTargets.addAll(personalTargets);
                
                // 按月份倒序排列
                familyTargets.sort((a, b) -> b.getTargetMonth().compareTo(a.getTargetMonth()));
                
                return familyTargets;
            }
        } catch (Exception e) {
            log.debug("【目标范围查询】查询用户ID: {} 的家庭组失败: {}", userId, e.getMessage());
        }
        
        // 用户不在家庭组中，返回所有个人目标
        return userTargetMapper.findByUserIdAndMonthRange(userId, startMonth, endMonth);
    }
    
    @Override
    @Transactional
    public void deleteByMonth(Long userId, String targetMonth) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID无效");
        }
        
        if (targetMonth == null || targetMonth.isEmpty()) {
            throw new BusinessException("目标月份无效");
        }
        
        // 检查权限：用户是否有target_edit权限
        if (!permissionService.canEdit(userId, "target")) {
            throw new BusinessException("无权删除目标");
        }
        
        // 检查用户是否在家庭组中
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null && familyGroup.getId() > 0) {
                // 【家庭组删除】删除该家庭组该月份的所有目标
                int deletedCount = userTargetMapper.deleteByFamilyGroupIdAndMonth(
                        familyGroup.getId(), targetMonth);
                log.info("【目标删除】删除家庭组ID: {} 月份 {} 的 {} 条目标数据", 
                        familyGroup.getId(), targetMonth, deletedCount);
                
                if (deletedCount == 0) {
                    throw new BusinessException("删除目标失败");
                }
                return;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.debug("【目标删除】查询用户ID: {} 的家庭组失败: {}", userId, e.getMessage());
        }
        
        // 【个人目标删除】删除用户该月份的个人目标
        int deletedCount = userTargetMapper.deleteByUserIdAndMonth(userId, targetMonth);
        if (deletedCount == 0) {
            throw new BusinessException("删除目标失败");
        }
        log.info("【目标删除】删除用户ID: {} 月份 {} 的目标数据", userId, targetMonth);
    }
}
