package com.billmanager.jizhang.service.impl;

import com.billmanager.jizhang.dto.ExpenseRequest;
import com.billmanager.jizhang.dto.ExpenseStatistics;
import com.billmanager.jizhang.entity.Expense;
import com.billmanager.jizhang.exception.BusinessException;
import com.billmanager.jizhang.mapper.ExpenseMapper;
import com.billmanager.jizhang.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {
    
    private final ExpenseMapper expenseMapper;
    
    @Override
    public Expense add(ExpenseRequest request, Long userId) {
        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setCategoryId(request.getCategoryId());
        expense.setAmount(request.getAmount());
        expense.setTransactionDate(request.getTransactionDate());
        expense.setDescription(request.getDescription());
        
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
    public List<Expense> findByUserId(Long userId) {
        return expenseMapper.findByUserIdOrderByDateDesc(userId);
    }
    
    @Override
    public List<Expense> findByUserIdAndDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        return expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
    }
    
    @Override
    public List<Expense> findByUserIdAndCategoryId(Long userId, Long categoryId) {
        return expenseMapper.findByUserIdAndCategoryId(userId, categoryId);
    }
    
    @Override
    public ExpenseStatistics getStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses;
        if (startDate != null && endDate != null) {
            expenses = expenseMapper.findByUserIdAndDateRange(userId, startDate, endDate);
        } else {
            expenses = expenseMapper.findByUserIdOrderByDateDesc(userId);
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
