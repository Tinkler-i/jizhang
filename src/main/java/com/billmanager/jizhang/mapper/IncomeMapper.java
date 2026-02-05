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
    
    // 按家庭组查询
    List<Income> findByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);
    
    List<Income> findByFamilyGroupIdOrderByDateDesc(@Param("familyGroupId") Long familyGroupId);
    
    List<Income> findByFamilyGroupIdAndDateRange(@Param("familyGroupId") Long familyGroupId,
                                                  @Param("startDate") LocalDate startDate,
                                                  @Param("endDate") LocalDate endDate);
    
    List<Income> findByFamilyGroupIdAndCategoryId(@Param("familyGroupId") Long familyGroupId,
                                                   @Param("categoryId") Long categoryId);
    
    // 删除指定用户的所有收入记录
    int deleteByUserId(@Param("userId") Long userId);
    
    // 删除指定用户在指定家族组中的收入记录
    int deleteByUserIdAndFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 更新指定用户的所有收入记录的家庭组ID
    int updateFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 将指定用户在指定家族组中的收入记录转换为个人数据（family_group_id = 0）
    int updateFamilyGroupIdToPersonal(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
}
