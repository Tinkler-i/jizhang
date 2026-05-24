package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.constant.PermissionConstants;
import com.billmanager.jizhang.dto.IncomeRequest;
import com.billmanager.jizhang.dto.IncomeStatistics;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.exception.FamilyPermissionException;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.service.IncomeService;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 收入服务实现
 * 
 * 简化的权限逻辑：
 * 1. income_view 权限：可以查看收入数据
 * 2. income_edit 权限：可以创建/编辑/删除收入数据
 * 
 * 数据范围：
 * - 不在家庭组中：只能操作自己的数据
 * - 在家庭组中：可以操作家庭组所有成员的数据（有权限的前提下）
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {
    
    private final IncomeMapper incomeMapper;
    private final FamilyGroupService familyGroupService;
    private final PermissionService permissionService;
    
    @Override
    public Income add(IncomeRequest request, Long userId) {
        log.info("【收入】用户 {} 创建收入记录", userId);
        
        // 权限检查：需要 income_edit 权限
        if (!permissionService.canEdit(userId, "income")) {
            throw new FamilyPermissionException("没有创建收入的权限");
        }
        
        Income income = new Income();
        income.setUserId(userId);
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setTransactionDate(request.getTransactionDate());
        income.setDescription(request.getDescription());
        
        // 获取用户的家族组ID
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null) {
                income.setFamilyGroupId(familyGroup.getId());
            } else {
                income.setFamilyGroupId(0L);
            }
        } catch (Exception e) {
            log.debug("【收入】获取用户的家族组失败，使用个人数据范围", e);
            income.setFamilyGroupId(0L);
        }
        
        incomeMapper.insert(income);
        log.info("【收入】用户 {} 成功创建收入记录 ID: {}", userId, income.getId());
        return income;
    }
    
    @Override
    public Income update(Long id, IncomeRequest request, Long userId) {
        log.info("【收入】用户 {} 更新收入记录 {}", userId, id);
        
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        
        // 权限检查：需要 income_edit 权限
        if (!permissionService.canEdit(userId, "income")) {
            throw new FamilyPermissionException("没有编辑收入的权限");
        }
        
        // 检查数据归属（只能修改自己的数据或同一家庭组的数据）
        checkDataOwnership(userId, income.getUserId());
        
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setTransactionDate(request.getTransactionDate());
        income.setDescription(request.getDescription());
        
        incomeMapper.update(income);
        log.info("【收入】用户 {} 成功更新收入记录 {}", userId, id);
        return income;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        log.info("【收入】用户 {} 删除收入记录 {}", userId, id);
        
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        
        // 权限检查：需要 income_edit 权限
        if (!permissionService.canEdit(userId, "income")) {
            throw new FamilyPermissionException("没有删除收入的权限");
        }
        
        // 检查数据归属
        checkDataOwnership(userId, income.getUserId());
        
        incomeMapper.deleteById(id);
        log.info("【收入】用户 {} 成功删除收入记录 {}", userId, id);
    }
    
    @Override
    public Income findById(Long id, Long userId) {
        log.debug("【收入】用户 {} 查询收入记录 {}", userId, id);
        
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        
        // 权限检查：需要 income_view 权限
        if (!permissionService.canView(userId, "income")) {
            throw new FamilyPermissionException("没有查看收入的权限");
        }
        
        // 检查数据归属
        checkDataOwnership(userId, income.getUserId());
        
        return income;
    }
    
    @Override
    public List<Income> findByUserId(Long userId) {
        log.debug("【收入】用户 {} 查询收入列表", userId);
        
        // 权限检查：需要 income_view 权限
        if (!permissionService.canView(userId, "income")) {
            throw new FamilyPermissionException("没有查看收入的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        
        if (member == null) {
            // 不在家庭组中，只查自己的数据
            List<Income> result = incomeMapper.findByUserIdOrderByDateDesc(userId);
            return result != null ? result : new java.util.ArrayList<>();
        }
        
        // 在家庭组中，查询家庭组所有数据
        List<Income> result = incomeMapper.findByFamilyGroupIdOrderByDateDesc(member.getFamilyGroupId());
        return result != null ? result : new java.util.ArrayList<>();
    }
    
    @Override
    public List<Income> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("【收入】用户 {} 查询日期范围 {} - {} 的收入", userId, startDate, endDate);
        
        if (!permissionService.canView(userId, "income")) {
            throw new FamilyPermissionException("没有查看收入的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        
        if (member == null) {
            List<Income> result = incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
            return result != null ? result : new java.util.ArrayList<>();
        }
        
        List<Income> result = incomeMapper.findByFamilyGroupIdAndDateRange(member.getFamilyGroupId(), startDate, endDate);
        return result != null ? result : new java.util.ArrayList<>();
    }
    
    @Override
    public List<Income> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        log.debug("【收入】用户 {} 查询分类 {} 的收入", userId, categoryId);
        
        if (!permissionService.canView(userId, "income")) {
            throw new FamilyPermissionException("没有查看收入的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        
        if (member == null) {
            List<Income> result = incomeMapper.findByUserIdAndCategoryId(userId, categoryId);
            return result != null ? result : new java.util.ArrayList<>();
        }
        
        List<Income> result = incomeMapper.findByFamilyGroupIdAndCategoryId(member.getFamilyGroupId(), categoryId);
        return result != null ? result : new java.util.ArrayList<>();
    }
    
    @Override
    public IncomeStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("【收入】用户 {} 查询统计信息", userId);
        
        List<Income> incomes;
        if (startDate != null && endDate != null) {
            incomes = findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            incomes = findByUserId(userId);
        }
        
        IncomeStatistics statistics = new IncomeStatistics();
        statistics.setTotalIncome(incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        statistics.setRecordCount((long) incomes.size());
        
        if (!incomes.isEmpty()) {
            BigDecimal avgIncome = statistics.getTotalIncome().divide(
                    new BigDecimal(incomes.size()), 2, java.math.RoundingMode.HALF_UP);
            statistics.setAvgIncome(avgIncome);
            
            BigDecimal maxIncome = incomes.stream()
                    .map(Income::getAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.setMaxIncome(maxIncome);
        } else {
            statistics.setAvgIncome(BigDecimal.ZERO);
            statistics.setMaxIncome(BigDecimal.ZERO);
        }
        
        statistics.setStartDate(startDate);
        statistics.setEndDate(endDate);
        
        return statistics;
    }
    
    /**
     * 检查数据归属权限
     * - 自己的数据：总是可以访问
     * - 他人的数据：需要在同一家庭组
     */
    private void checkDataOwnership(Long currentUserId, Long dataOwnerId) {
        if (currentUserId.equals(dataOwnerId)) {
            return; // 自己的数据，允许
        }
        
        FamilyMember currentMember = permissionService.getFamilyMember(currentUserId);
        FamilyMember ownerMember = permissionService.getFamilyMember(dataOwnerId);
        
        // 不在家庭组中，不能访问他人数据
        if (currentMember == null) {
            throw new FamilyPermissionException("无权访问他人数据");
        }
        
        // 数据所有者不在家庭组或不在同一家庭组
        if (ownerMember == null || !currentMember.getFamilyGroupId().equals(ownerMember.getFamilyGroupId())) {
            throw new FamilyPermissionException("无权访问该数据");
        }
    }
}
