package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.BudgetRequest;
import com.billmanager.jizhang.dto.BudgetStatistics;
import com.billmanager.jizhang.entity.Budget;

import java.util.List;

public interface BudgetService {
    
    Budget add(BudgetRequest request, Long userId);
    
    Budget update(Long id, BudgetRequest request, Long userId);
    
    void delete(Long id, Long userId);
    
    Budget findById(Long id, Long userId);
    
    List<Budget> findByUserId(Long userId);
    
    List<Budget> findByUserIdAndBudgetMonth(Long userId, String budgetMonth);
    
    List<Budget> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    List<BudgetStatistics> getStatistics(Long userId, String budgetMonth);
    
    BudgetStatistics getStatisticsById(Long budgetId, Long userId);
    
    void updateSpent(Long budgetId, java.math.BigDecimal amount);
}
