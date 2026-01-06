package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.IncomeCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IncomeCategoryMapper {
    
    int insert(IncomeCategory incomeCategory);
    
    int update(IncomeCategory incomeCategory);
    
    int deleteById(@Param("id") Long id);
    
    IncomeCategory findById(@Param("id") Long id);
    
    List<IncomeCategory> findByUserId(@Param("userId") Long userId);
    
    IncomeCategory findByUserIdAndName(@Param("userId") Long userId, 
                                        @Param("name") String name);
}
