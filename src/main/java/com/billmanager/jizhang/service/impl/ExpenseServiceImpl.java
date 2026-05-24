package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.ExpenseRequest;
import com.billmanager.jizhang.dto.ExpenseStatistics;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.exception.FamilyPermissionException;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.service.ExpenseService;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 支出服务实现
 * 
 * 简化的权限逻辑：
 * 1. expense_view 权限：可以查看支出数据
 * 2. expense_edit 权限：可以创建/编辑/删除支出数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {
    
    private final ExpenseMapper expenseMapper;
    private final FamilyGroupService familyGroupService;
    private final PermissionService permissionService;
    
    @Override
    public Expense add(ExpenseRequest request, Long userId) {
        log.info("【支出】用户 {} 创建支出记录", userId);
        
        if (!permissionService.canEdit(userId, "expense")) {
            throw new FamilyPermissionException("没有创建支出的权限");
        }
        
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setDescription(request.getDescription());
        
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null) {
                expense.setFamilyGroupId(familyGroup.getId());
            } else {
                expense.setFamilyGroupId(0L);
            }
        } catch (Exception e) {
            log.debug("【支出】获取用户的家族组失败，使用个人数据范围", e);
            expense.setFamilyGroupId(0L);
        }
        
        expenseMapper.insert(expense);
        log.info("【支出】用户 {} 成功创建支出记录 ID: {}", userId, expense.getId());
        return expense;
    }
    
    @Override
    public Expense update(Long id, ExpenseRequest request, Long userId) {
        log.info("【支出】用户 {} 更新支出记录 {}", userId, id);
        
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new BusinessException("支出记录不存在");
        }
        
        if (!permissionService.canEdit(userId, "expense")) {
            throw new FamilyPermissionException("没有编辑支出的权限");
        }
        
        checkDataOwnership(userId, expense.getUserId());
        
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setDescription(request.getDescription());
        
        expenseMapper.update(expense);
        log.info("【支出】用户 {} 成功更新支出记录 {}", userId, id);
        return expense;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        log.info("【支出】用户 {} 删除支出记录 {}", userId, id);
        
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new BusinessException("支出记录不存在");
        }
        
        if (!permissionService.canEdit(userId, "expense")) {
            throw new FamilyPermissionException("没有删除支出的权限");
        }
        
        checkDataOwnership(userId, expense.getUserId());
        
        expenseMapper.deleteById(id);
        log.info("【支出】用户 {} 成功删除支出记录 {}", userId, id);
    }
    
    @Override
    public Expense findById(Long id, Long userId) {
        log.debug("【支出】用户 {} 查询支出记录 {}", userId, id);
        
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new BusinessException("支出记录不存在");
        }
        
        if (!permissionService.canView(userId, "expense")) {
            throw new FamilyPermissionException("没有查看支出的权限");
        }
        
        checkDataOwnership(userId, expense.getUserId());
        
        return expense;
    }
    
    @Override
    public List<Expense> findByUserId(Long userId) {
        log.debug("【支出】用户 {} 查询支出列表", userId);
        
        if (!permissionService.canView(userId, "expense")) {
            throw new FamilyPermissionException("没有查看支出的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        
        if (member == null) {
            List<Expense> result = expenseMapper.findByUserIdOrderByDateDesc(userId);
            return result != null ? result : new java.util.ArrayList<>();
        }
        
        List<Expense> result = expenseMapper.findByFamilyGroupIdOrderByDateDesc(member.getFamilyGroupId());
        return result != null ? result : new java.util.ArrayList<>();
    }
    
    @Override
    public List<Expense> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("【支出】用户 {} 查询日期范围 {} - {} 的支出", userId, startDate, endDate);
        
        if (!permissionService.canView(userId, "expense")) {
            throw new FamilyPermissionException("没有查看支出的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        
        if (member == null) {
            List<Expense> result = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
            return result != null ? result : new java.util.ArrayList<>();
        }
        
        List<Expense> result = expenseMapper.findByFamilyGroupIdAndDateRange(member.getFamilyGroupId(), startDate, endDate);
        return result != null ? result : new java.util.ArrayList<>();
    }
    
    @Override
    public List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        log.debug("【支出】用户 {} 查询分类 {} 的支出", userId, categoryId);
        
        if (!permissionService.canView(userId, "expense")) {
            throw new FamilyPermissionException("没有查看支出的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        
        if (member == null) {
            List<Expense> result = expenseMapper.findByUserIdAndCategoryId(userId, categoryId);
            return result != null ? result : new java.util.ArrayList<>();
        }
        
        List<Expense> result = expenseMapper.findByFamilyGroupIdAndCategoryId(member.getFamilyGroupId(), categoryId);
        return result != null ? result : new java.util.ArrayList<>();
    }
    
    @Override
    public ExpenseStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        log.debug("【支出】用户 {} 查询统计信息", userId);
        
        List<Expense> expenses;
        if (startDate != null && endDate != null) {
            expenses = findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            expenses = findByUserId(userId);
        }
        
        ExpenseStatistics statistics = new ExpenseStatistics();
        statistics.setTotalExpense(expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        statistics.setRecordCount((long) expenses.size());
        
        if (!expenses.isEmpty()) {
            BigDecimal avgExpense = statistics.getTotalExpense().divide(
                    new BigDecimal(expenses.size()), 2, java.math.RoundingMode.HALF_UP);
            statistics.setAvgExpense(avgExpense);
            
            BigDecimal maxExpense = expenses.stream()
                    .map(Expense::getAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.setMaxExpense(maxExpense);
        } else {
            statistics.setAvgExpense(BigDecimal.ZERO);
            statistics.setMaxExpense(BigDecimal.ZERO);
        }
        
        statistics.setStartDate(startDate);
        statistics.setEndDate(endDate);
        
        return statistics;
    }
    
    private void checkDataOwnership(Long currentUserId, Long dataOwnerId) {
        if (currentUserId.equals(dataOwnerId)) {
            return;
        }
        
        FamilyMember currentMember = permissionService.getFamilyMember(currentUserId);
        FamilyMember ownerMember = permissionService.getFamilyMember(dataOwnerId);
        
        if (currentMember == null) {
            throw new FamilyPermissionException("无权访问他人数据");
        }
        
        if (ownerMember == null || !currentMember.getFamilyGroupId().equals(ownerMember.getFamilyGroupId())) {
            throw new FamilyPermissionException("无权访问该数据");
        }
    }
}
