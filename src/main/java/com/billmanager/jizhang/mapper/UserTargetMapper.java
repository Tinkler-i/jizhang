package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.UserTarget;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户收入目标 Mapper
 */
@Mapper
public interface UserTargetMapper {
    
    /**
     * 按ID查询
     */
    @Select("SELECT * FROM user_target WHERE id = #{id}")
    UserTarget findById(Long id);
    
    /**
     * 查询用户指定月份的目标
     */
    @Select("SELECT * FROM user_target WHERE user_id = #{userId} AND target_month = #{targetMonth} AND family_group_id = 0")
    UserTarget findByUserIdAndMonth(@Param("userId") Long userId, @Param("targetMonth") String targetMonth);
    
    /**
     * 查询家庭指定月份的目标
     */
    @Select("SELECT * FROM user_target WHERE family_group_id = #{familyGroupId} AND target_month = #{targetMonth}")
    UserTarget findByFamilyGroupIdAndMonth(@Param("familyGroupId") Long familyGroupId, @Param("targetMonth") String targetMonth);
    
    /**
     * 新增
     */
    @Insert("INSERT INTO user_target (user_id, family_group_id, target_month, income_target) VALUES (#{userId}, #{familyGroupId}, #{targetMonth}, #{incomeTarget})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserTarget userTarget);
    
    /**
     * 更新
     */
    @Update("UPDATE user_target SET income_target = #{incomeTarget}, update_time = NOW() WHERE id = #{id}")
    int update(UserTarget userTarget);
    
    /**
     * 删除
     */
    @Delete("DELETE FROM user_target WHERE id = #{id}")
    int delete(Long id);
    
    /**
     * 查询用户的所有目标（个人 + 所在家庭的）
     */
    @Select("SELECT * FROM user_target WHERE (user_id = #{userId} AND family_group_id = 0) OR family_group_id IN (SELECT family_group_id FROM family_member WHERE user_id = #{userId}) ORDER BY target_month DESC")
    List<UserTarget> findByUserId(Long userId);
    
    /**
     * 查询家庭组的目标
     */
    @Select("SELECT * FROM user_target WHERE family_group_id = #{familyGroupId} AND family_group_id > 0 ORDER BY target_month DESC")
    List<UserTarget> findByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);
    
    /**
     * 查询用户在某个月份范围内的目标（个人 + 家庭）
     */
    @Select("SELECT * FROM user_target WHERE ((user_id = #{userId} AND family_group_id = 0) OR family_group_id IN (SELECT family_group_id FROM family_member WHERE user_id = #{userId})) AND target_month >= #{startMonth} AND target_month <= #{endMonth} ORDER BY target_month DESC")
    List<UserTarget> findByUserIdAndMonthRange(
            @Param("userId") Long userId, 
            @Param("startMonth") String startMonth, 
            @Param("endMonth") String endMonth);
    
    /**
     * 删除家庭组的所有目标
     */
    @Delete("DELETE FROM user_target WHERE family_group_id = #{familyGroupId} AND family_group_id > 0")
    int deleteByFamilyGroupId(@Param("familyGroupId") Long familyGroupId);
}
