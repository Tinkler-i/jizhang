package com.billmanager.jizhang.service;

import com.billmanager.jizhang.dto.IncomeRequest;
import com.billmanager.jizhang.dto.IncomeStatistics;
import com.billmanager.jizhang.entity.Income;

import java.time.LocalDate;
import java.util.List;

public interface IncomeService {
    
    Income add(IncomeRequest request, Long userId);
    
    Income update(Long id, IncomeRequest request, Long userId);
    
    void delete(Long id, Long userId);
    
    Income findById(Long id, Long userId);
    
    List<Income> findByUserId(Long userId);
    
    List<Income> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<Income> findByUserIdAndCategoryId(Long userId, Long categoryId);
    
    IncomeStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate);
}
