package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.IncomeRequest;
import com.billmanager.jizhang.dto.IncomeStatistics;
import com.billmanager.jizhang.entity.Income;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.IncomeMapper;
import com.billmanager.jizhang.service.IncomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {
    
    private final IncomeMapper incomeMapper;
    
    @Override
    public Income add(IncomeRequest request, Long userId) {
        Income income = new Income();
        income.setUserId(userId);
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setTransactionDate(request.getTransactionDate());
        income.setDescription(request.getDescription());
        
        incomeMapper.insert(income);
        return income;
    }
    
    @Override
    public Income update(Long id, IncomeRequest request, Long userId) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        if (!income.getUserId().equals(userId)) {
            throw new BusinessException("无权修改此记录");
        }
        
        income.setCategoryId(request.getCategoryId());
        income.setAmount(request.getAmount());
        income.setTransactionDate(request.getTransactionDate());
        income.setDescription(request.getDescription());
        
        incomeMapper.update(income);
        return income;
    }
    
    @Override
    public void delete(Long id, Long userId) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        if (!income.getUserId().equals(userId)) {
            throw new BusinessException("无权删除此记录");
        }
        
        incomeMapper.deleteById(id);
    }
    
    @Override
    public Income findById(Long id, Long userId) {
        Income income = incomeMapper.findById(id);
        if (income == null) {
            throw new BusinessException("收入记录不存在");
        }
        if (!income.getUserId().equals(userId)) {
            throw new BusinessException("无权查看此记录");
        }
        return income;
    }
    
    @Override
    public List<Income> findByUserId(Long userId) {
        return incomeMapper.findByUserIdOrderByDateDesc(userId);
    }
    
    @Override
    public List<Income> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Override
    public List<Income> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        return incomeMapper.findByUserIdAndCategoryId(userId, categoryId);
    }
    
    @Override
    public IncomeStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Income> incomes;
        if (startDate != null && endDate != null) {
            incomes = incomeMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            incomes = incomeMapper.findByUserIdOrderByDateDesc(userId);
        }
        
        IncomeStatistics statistics = new IncomeStatistics();
        statistics.setTotalIncome(incomes.stream()
                .map(Income::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        statistics.setRecordCount((long) incomes.size());
        
        if (!incomes.isEmpty()) {
            BigDecimal avgIncome = statistics.getTotalIncome().divide(
                    new BigDecimal(incomes.size()), 2, BigDecimal.ROUND_HALF_UP);
            statistics.setAvgIncome(avgIncome);
            
            BigDecimal maxIncome = incomes.stream()
                    .map(Income::getAmount)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.setMaxIncome(maxIncome);
        } else {
            statistics.setAvgIncome(BigDecimal.ZERO);
            statistics.setMaxIncome(BigDecimal.ZERO);
        }
        
        statistics.setStartDate(startDate);
        statistics.setEndDate(endDate);
        
        return statistics;
    }
}
