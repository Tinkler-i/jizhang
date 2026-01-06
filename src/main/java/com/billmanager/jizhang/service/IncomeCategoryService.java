package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.IncomeCategoryRequest;
import com.billmanager.jizhang.entity.IncomeCategory;

import java.util.List;

public interface IncomeCategoryService {
    
    IncomeCategory add(IncomeCategoryRequest request, Long userId);
    
    IncomeCategory update(Long id, IncomeCategoryRequest request, Long userId);
    
    void delete(Long id, Long userId);
    
    IncomeCategory findById(Long id, Long userId);
    
    List<IncomeCategory> findByUserId(Long userId);
}
