package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.ExpenseRequest;
import com.billmanager.jizhang.dto.ExpenseStatistics;
import com.billmanager.jizhang.entity.Expense;

import java.time.LocalDate;
import java.util.List;

public interface ExpenseService {
    
    Expense add(ExpenseRequest request, Long userId);
    
    Expense update(Long id, ExpenseRequest request, Long userId);
    
    void delete(Long id, Long userId);
    
    Expense findById(Long id, Long userId);
    
    List<Expense> findByUserId(Long userId);
    
    List<Expense> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    ExpenseStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate);
}
