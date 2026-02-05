package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.Budget;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.YearMonth;
import java.util.List;

@Mapper
public interface BudgetMapper {
    
    int insert(Budget budget);
    
    int update(Budget budget);
    
    int deleteById(@Param("id") Long id);
    
    Budget findById(@Param("id") Long id);
    
    List<Budget> findByUserId(@Param("userId") Long userId);
    
    Budget findByUserIdAndCategoryIdAndYearMonth(@Param("userId") Long userId, 
                                                  @Param("categoryId") Long categoryId, 
                                                  @Param("yearMonth") String yearMonth);
    
    List<Budget> findByUserIdAndYearMonth(@Param("userId") Long userId, 
                                          @Param("yearMonth") String yearMonth);
    
    List<Budget> findByUserIdAndCategoryId(@Param("userId") Long userId, 
                                           @Param("categoryId") Long categoryId);
    
    int updateSpent(@Param("id") Long id, @Param("spent") java.math.BigDecimal spent);
    
    // 按家庭组查询
    List<Budget> findByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);
    
    Budget findByFamilyGroupIdAndCategoryIdAndYearMonth(@Param("familyGroupId") Long familyGroupId,
                                                         @Param("categoryId") Long categoryId,
                                                         @Param("yearMonth") String yearMonth);
    
    List<Budget> findByFamilyGroupIdAndYearMonth(@Param("familyGroupId") Long familyGroupId,
                                                  @Param("yearMonth") String yearMonth);
    
    List<Budget> findByFamilyGroupIdAndCategoryId(@Param("familyGroupId") Long familyGroupId,
                                                   @Param("categoryId") Long categoryId);
    
    // 退出家庭组时的数据处理
    int deleteByUserIdAndFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    int updateFamilyGroupIdToPersonal(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
}
