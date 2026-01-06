package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.ExpenseCategoryRequest;
import com.billmanager.jizhang.entity.ExpenseCategory;

import java.util.List;

public interface ExpenseCategoryService {
    
    ExpenseCategory add(ExpenseCategoryRequest request, Long userId);
    
    ExpenseCategory update(Long id, ExpenseCategoryRequest request, Long userId);
    
    void delete(Long id, Long userId);
    
    ExpenseCategory findById(Long id, Long userId);
    
    List<ExpenseCategory> findByUserId(Long userId);
}
