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
    
    // 按家庭组查询
    List<IncomeCategory> findByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);
    
    IncomeCategory findByFamilyGroupIdAndName(@Param("familyGroupId") Long familyGroupId,
                                              @Param("name") String name);
    
    // 更新指定用户的所有分类的家庭组ID
    int updateFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 删除指定用户的所有分类
    int deleteByUserId(@Param("userId") Long userId);
    
    // 删除指定用户在指定家族组中的分类
    int deleteByUserIdAndFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 将指定用户在指定家族组中的分类转换为个人数据
    int updateFamilyGroupIdToPersonal(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    // 查询指定家庭组中所有同名的分类
    List<IncomeCategory> findAllByFamilyGroupIdAndName(@Param("familyGroupId") Long familyGroupId,
                                                        @Param("name") String name);
}
