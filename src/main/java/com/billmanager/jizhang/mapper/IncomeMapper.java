package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.Income;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface IncomeMapper {
    
    int insert(Income income);
    
    int update(Income income);
    
    int deleteById(@Param("id") Long id);
    
    Income findById(@Param("id") Long id);
    
    List<Income> findByUserId(@Param("userId") Long userId);
    
    List<Income> findByUserIdAndDateRange(@Param("userId") Long userId, 
                                           @Param("startDate") LocalDate startDate, 
                                           @Param("endDate") LocalDate endDate);
    
    List<Income> findByUserIdAndCategoryId(@Param("userId") Long userId, 
                                            @Param("categoryId") Long categoryId);
    
    List<Income> findByUserIdOrderByDateDesc(@Param("userId") Long userId);
}
