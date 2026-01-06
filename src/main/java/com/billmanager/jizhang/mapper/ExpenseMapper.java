package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.Expense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface ExpenseMapper {
    
    int insert(Expense expense);
    
    int update(Expense expense);
    
    int deleteById(@Param("id") Long id);
    
    Expense findById(@Param("id") Long id);
    
    List<Expense> findByUserId(@Param("userId") Long userId);
    
    List<Expense> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    List<Expense> findByUserIdAndCategoryId(@Param("userId") Long userId, 
                                            @Param("categoryId") Long categoryId);
    
    List<Expense> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
}
