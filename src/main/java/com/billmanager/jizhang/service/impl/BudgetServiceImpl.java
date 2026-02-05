package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.BudgetRequest;
import com.billmanager.jizhang.dto.BudgetStatistics;
import com.billmanager.jizhang.entity.Budget;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.entity.FamilyMember;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.exception.FamilyPermissionException;
import com.billmanager.jizhang.mapper.BudgetMapper;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.service.BudgetService;
import com.billmanager.jizhang.service.FamilyGroupService;
import com.billmanager.jizhang.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预算服务实现
 * 
 * 简化的权限逻辑：
 * 1. budget_view 权限：可以查看预算数据
 * 2. budget_edit 权限：可以创建/编辑/删除预算数据
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {
    
    private final BudgetMapper budgetMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final ExpenseMapper expenseMapper;
    private final FamilyGroupService familyGroupService;
    private final PermissionService permissionService;
    
    @Override
    @Transactional
    public Budget add(BudgetRequest request, Long userId) {
        if (!permissionService.canEdit(userId, "budget")) {
            throw new FamilyPermissionException("没有创建预算的权限");
        }
        
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setBudgetMonth(request.getBudgetMonth());
        budget.setSpent(BigDecimal.ZERO);
        budget.setRemark(request.getRemark());
        
        // 如果用户在家庭组中，设置 familyGroupId
        FamilyMember member = permissionService.getFamilyMember(userId);
        if (member != null) {
            budget.setFamilyGroupId(member.getFamilyGroupId());
            log.info("【预算创建】用户{}在家庭组{}中，设置familyGroupId", userId, member.getFamilyGroupId());
        }
        
        budgetMapper.insert(budget);
        return budget;
    }
    
    @Override
    @Transactional
    public Budget update(Long id, BudgetRequest request, Long userId) {
        if (!permissionService.canEdit(userId, "budget")) {
            throw new FamilyPermissionException("没有编辑预算的权限");
        }
        
        Budget budget = budgetMapper.findById(id);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }
        
        checkDataOwnership(userId, budget.getUserId());
        
        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setBudgetMonth(request.getBudgetMonth());
        budget.setRemark(request.getRemark());
        
        budgetMapper.update(budget);
        return budget;
    }
    
    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        if (!permissionService.canEdit(userId, "budget")) {
            throw new FamilyPermissionException("没有删除预算的权限");
        }
        
        Budget budget = budgetMapper.findById(id);
        if (budget == null) {
            throw new BusinessException("预算不存在");
        }
        
        checkDataOwnership(userId, budget.getUserId());
        
        budgetMapper.deleteById(id);
    }
    
    @Override
    public Budget findById(Long id, Long userId) {
        if (!permissionService.canView(userId, "budget")) {
            throw new FamilyPermissionException("没有查看预算的权限");
        }
        
        Budget budget = budgetMapper.findById(id);
        if (budget == null) {
            return null;
        }
        
        checkDataOwnership(userId, budget.getUserId());
        return budget;
    }
    
    @Override
    public List<Budget> findByUserId(Long userId) {
        if (!permissionService.canView(userId, "budget")) {
            throw new FamilyPermissionException("没有查看预算的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        if (member == null) {
            // 不在家庭组，返回个人预算
            return budgetMapper.findByUserId(userId);
        }
        
        return budgetMapper.findByFamilyGroupId(member.getFamilyGroupId());
    }
    
    @Override
    public List<Budget> findByUserIdAndBudgetMonth(Long userId, String budgetMonth) {
        if (!permissionService.canView(userId, "budget")) {
            throw new FamilyPermissionException("没有查看预算的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        if (member == null) {
            return budgetMapper.findByUserIdAndYearMonth(userId, budgetMonth);
        }
        
        return budgetMapper.findByFamilyGroupIdAndYearMonth(member.getFamilyGroupId(), budgetMonth);
    }
    
    @Override
    public List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        if (!permissionService.canView(userId, "budget")) {
            throw new FamilyPermissionException("没有查看预算的权限");
        }
        
        FamilyMember member = permissionService.getFamilyMember(userId);
        if (member == null) {
            return budgetMapper.findByUserIdAndCategoryId(userId, categoryId);
        }
        
        return budgetMapper.findByFamilyGroupIdAndCategoryId(member.getFamilyGroupId(), categoryId);
    }
    
    @Override
    public List<BudgetStatistics> getStatistics(Long userId, String budgetMonth) {
        List<Budget> budgets = findByUserIdAndBudgetMonth(userId, budgetMonth);
        
        return budgets.stream().map(budget -> {
            BudgetStatistics stat = new BudgetStatistics();
            stat.setId(budget.getId());
            stat.setCategoryId(budget.getCategoryId());
            
            ExpenseCategory category = expenseCategoryMapper.findById(budget.getCategoryId());
            if (category != null) {
                stat.setCategoryName(category.getName());
            }
            
            stat.setBudgetAmount(budget.getAmount());
            
            BigDecimal spentAmount = calculateSpentAmount(userId, budget.getCategoryId(), budgetMonth);
            stat.setSpentAmount(spentAmount);
            
            BigDecimal remaining = budget.getAmount().subtract(spentAmount);
            stat.setRemainingAmount(remaining);
            
            BigDecimal percentage = BigDecimal.ZERO;
            if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                percentage = spentAmount.divide(budget.getAmount(), 2, java.math.RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));
            }
            stat.setPercentageUsed(percentage);
            
            stat.setBudgetMonth(budget.getBudgetMonth());
            stat.setRemark(budget.getRemark());
            
            return stat;
        }).collect(Collectors.toList());
    }
    
    /**
     * 计算该月份该分类的实际支出
     */
    private BigDecimal calculateSpentAmount(Long userId, Long categoryId, String budgetMonth) {
        try {
            YearMonth yearMonth = YearMonth.parse(budgetMonth);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            
            FamilyMember member = permissionService.getFamilyMember(userId);
            if (member == null) {
                // 不在家庭组，计算个人支出
                List<Expense> expenses = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
                return expenses.stream()
                        .filter(e -> e.getCategoryId().equals(categoryId))
                        .map(Expense::getAmount)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }
            
            // 查询家庭组在该时间范围内该分类的所有支出
            List<Expense> expenses = expenseMapper.findByFamilyGroupIdAndDateRange(
                    member.getFamilyGroupId(), startDate, endDate);
            
            BigDecimal total = expenses.stream()
                    .filter(e -> e.getCategoryId().equals(categoryId))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            return total;
        } catch (Exception e) {
            log.error("【支出计算错误】{}", e.getMessage());
            return BigDecimal.ZERO;
        }
    }
    
    @Override
    public BudgetStatistics getStatisticsById(Long budgetId, Long userId) {
        Budget budget = findById(budgetId, userId);
        if (budget == null) {
            return null;
        }
        
        BudgetStatistics stat = new BudgetStatistics();
        stat.setId(budget.getId());
        stat.setCategoryId(budget.getCategoryId());
        
        ExpenseCategory category = expenseCategoryMapper.findById(budget.getCategoryId());
        if (category != null) {
            stat.setCategoryName(category.getName());
        }
        
        stat.setBudgetAmount(budget.getAmount());
        stat.setSpentAmount(budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO);
        
        BigDecimal remaining = budget.getAmount().subtract(
                budget.getSpent() != null ? budget.getSpent() : BigDecimal.ZERO
        );
        stat.setRemainingAmount(remaining);
        
        BigDecimal percentage = BigDecimal.ZERO;
        if (budget.getAmount().compareTo(BigDecimal.ZERO) > 0 && budget.getSpent() != null) {
            percentage = budget.getSpent().divide(budget.getAmount(), 2, java.math.RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }
        stat.setPercentageUsed(percentage);
        
        stat.setBudgetMonth(budget.getBudgetMonth());
        stat.setRemark(budget.getRemark());
        
        return stat;
    }
    
    @Override
    @Transactional
    public void updateSpent(Long budgetId, java.math.BigDecimal amount) {
        budgetMapper.updateSpent(budgetId, amount);
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
