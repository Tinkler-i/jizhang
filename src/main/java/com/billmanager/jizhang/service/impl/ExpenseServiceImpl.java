package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.annotation.FamilyPermission;
import com.billmanager.jizhang.dto.ExpenseRequest;
import com.billmanager.jizhang.dto.ExpenseStatistics;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.FamilyGroup;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.service.ExpenseService;
import com.billmanager.jizhang.service.FamilyGroupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {
    
    private final ExpenseMapper expenseMapper;
    private final FamilyGroupService familyGroupService;
    
    @Override
    public Expense add(ExpenseRequest request, Long userId) {
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setDescription(request.getDescription());
        
        // 获取用户的家族组ID，如果没有则设为0（个人数据）
        try {
            FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
            if (familyGroup != null) {
                expense.setFamilyGroupId(familyGroup.getId());
            } else {
                expense.setFamilyGroupId(0L); // 0 表示个人数据
            }
        } catch (Exception e) {
            log.debug("获取用户的家族组失败，使用个人数据范围", e);
            expense.setFamilyGroupId(0L); // 默认使用个人数据
        }
        
        expenseMapper.insert(expense);
        return expense;
    }
    
    @Override
    public Expense update(Long id, ExpenseRequest request, Long userId) {
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new BusinessException("支出记录不存在");
        }
        if (!expense.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此记录");
        }
        
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setDescription(request.getDescription());
        
        expenseMapper.update(expense);
        return expense;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new BusinessException("支出记录不存在");
        }
        if (!expense.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此记录");
        }
        
        expenseMapper.deleteById(id);
    }
    
    @Override
    public Expense findById(Long id, Long userId) {
        Expense expense = expenseMapper.findById(id);
        if (expense == null) {
            throw new BusinessException("支出记录不存在");
        }
        if (!expense.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此记录");
        }
        return expense;
    }
    
    @Override
    @FamilyPermission("expense_view")
    public List<Expense> findByUserId(Long userId) {
        // 获取用户所属家庭组
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            // 用户属于某个家庭组，按familyGroupId查询
            return expenseMapper.findByFamilyGroupIdOrderByDateDesc(familyGroup.getId());
        } else {
            // 用户不属于任何家庭组，按userId查询（完全访问权限）
            log.info("【支出】用户{}不属于任何家庭组，按个人身份查询支出", userId);
            return expenseMapper.findByUserIdOrderByDateDesc(userId);
        }
    }
    
    @Override
    @FamilyPermission("expense_view")
    public List<Expense> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            return expenseMapper.findByFamilyGroupIdAndDateRange(familyGroup.getId(), startDate, endDate);
        } else {
            // 用户不属于任何家庭组，按userId查询
            return expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        }
    }
    
    @Override
    @FamilyPermission("expense_view")
    public List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        if (familyGroup != null) {
            return expenseMapper.findByFamilyGroupIdAndCategoryId(familyGroup.getId(), categoryId);
        } else {
            // 用户不属于任何家庭组，按userId查询
            return expenseMapper.findByUserIdAndCategoryId(userId, categoryId);
        }
    }
    
    @Override
    @FamilyPermission("expense_view")
    public ExpenseStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        FamilyGroup familyGroup = familyGroupService.getFamilyGroupByUserId(userId);
        List<Expense> expenses;
        
        if (familyGroup != null) {
            if (startDate != null && endDate != null) {
                expenses = expenseMapper.findByFamilyGroupIdAndDateRange(familyGroup.getId(), startDate, endDate);
            } else {
                expenses = expenseMapper.findByFamilyGroupIdOrderByDateDesc(familyGroup.getId());
            }
        } else {
            // 用户不属于任何家庭组，按userId查询
            if (startDate != null && endDate != null) {
                expenses = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
            } else {
                expenses = expenseMapper.findByUserIdOrderByDateDesc(userId);
            }
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
}
