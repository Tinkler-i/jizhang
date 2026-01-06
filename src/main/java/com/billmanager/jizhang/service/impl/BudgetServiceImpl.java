package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.BudgetRequest;
import com.billmanager.jizhang.dto.BudgetStatistics;
import com.billmanager.jizhang.entity.Budget;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.mapper.BudgetMapper;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    
    private final BudgetMapper budgetMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    
    @Override
    @Transactional
    public Budget add(BudgetRequest request, Long userId) {
        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setCategoryId(request.getCategoryId());
        budget.setAmount(request.getAmount());
        budget.setBudgetMonth(request.getBudgetMonth());
        budget.setSpent(BigDecimal.ZERO);
        budget.setRemark(request.getRemark());
        
        budgetMapper.insert(budget);
        return budget;
    }
    
    @Override
    @Transactional
    public Budget update(Long id, BudgetRequest request, Long userId) {
        Budget budget = findById(id, userId);
        if (budget == null) {
            throw new RuntimeException("预算不存在");
        }
        
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
        Budget budget = findById(id, userId);
        if (budget == null) {
            throw new RuntimeException("预算不存在");
        }
        budgetMapper.deleteById(id);
    }
    
    @Override
    public Budget findById(Long id, Long userId) {
        Budget budget = budgetMapper.findById(id);
        if (budget != null && !budget.getUserId().equals(userId)) {
            return null;
        }
        return budget;
    }
    
    @Override
    public List<Budget> findByUserId(Long userId) {
        return budgetMapper.findByUserId(userId);
    }
    
    @Override
    public List<Budget> findByUserIdAndBudgetMonth(Long userId, String budgetMonth) {
        return budgetMapper.findByUserIdAndYearMonth(userId, budgetMonth);
    }
    
    @Override
    public List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        return budgetMapper.findByUserIdAndCategoryId(userId, categoryId);
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
        }).collect(Collectors.toList());
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
}
