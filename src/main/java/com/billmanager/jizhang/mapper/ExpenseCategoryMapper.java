package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.ExpenseCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ExpenseCategoryMapper {
    
    int insert(ExpenseCategory expenseCategory);
    
    int update(ExpenseCategory expenseCategory);
    
    int deleteById(@Param("id") Long id);
    
    ExpenseCategory findById(@Param("id") Long id);
    
    List<ExpenseCategory> findByUserId(@Param("userId") Long userId);
    
    ExpenseCategory findByUserIdAndName(@Param("userId") Long userId, 
                                        @Param("name") String name);
}
