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
    
    // 按家庭组查询
    List<Expense> findByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);
    
    List<Expense> findByFamilyGroupIdOrderByDateDesc(@Param("familyGroupId") Long familyGroupId);
    
    List<Expense> findByFamilyGroupIdAndDateRange(@Param("familyGroupId") Long familyGroupId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    List<Expense> findByFamilyGroupIdAndCategoryId(@Param("familyGroupId") Long familyGroupId,
                                                    @Param("categoryId") Long categoryId);
    
    // 删除指定用户的所有支出记录
    int deleteByUserId(@Param("userId") Long userId);
    
    // 删除指定用户在指定家族组中的支出记录
    int deleteByUserIdAndFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 更新指定用户的所有支出记录的家庭组ID
    int updateFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 将指定用户在指定家族组中的支出记录转换为个人数据
    int updateFamilyGroupIdToPersonal(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
}
