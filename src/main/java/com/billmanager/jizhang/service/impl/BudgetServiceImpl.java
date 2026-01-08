package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.BudgetRequest;
import com.billmanager.jizhang.dto.BudgetStatistics;
import com.billmanager.jizhang.entity.Budget;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.entity.ExpenseCategory;
import com.billmanager.jizhang.mapper.BudgetMapper;
import com.billmanager.jizhang.mapper.ExpenseCategoryMapper;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetServiceImpl implements BudgetService {
    
    private final BudgetMapper budgetMapper;
    private final ExpenseCategoryMapper expenseCategoryMapper;
    private final ExpenseMapper expenseMapper;
    
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
            
            // 动态计算该月份该分类的实际支出
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
            
            System.out.println("【预算统计】分类: " + stat.getCategoryName() + 
                    ", 预算: " + stat.getBudgetAmount() + 
                    ", 已用: " + stat.getSpentAmount() + 
                    ", 百分比: " + stat.getPercentageUsed());
            
            return stat;
        }).collect(Collectors.toList());
    }
    
    /**
     * 计算该月份该分类的实际支出
     */
    private BigDecimal calculateSpentAmount(Long userId, Long categoryId, String budgetMonth) {
        try {
            // 解析月份字符串 (格式: 2026-01)
            YearMonth yearMonth = YearMonth.parse(budgetMonth);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();
            
            // 查询该用户在该时间范围内该分类的所有支出
            List<Expense> expenses = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
            
            // 过滤该分类的支出并求和
            BigDecimal total = expenses.stream()
                    .filter(e -> e.getCategoryId().equals(categoryId))
                    .map(Expense::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            System.out.println("【支出计算】用户: " + userId + ", 分类: " + categoryId + 
                    ", 月份: " + budgetMonth + ", 支出: " + total);
            
            return total;
        } catch (Exception e) {
            System.err.println("【支出计算错误】" + e.getMessage());
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
}
