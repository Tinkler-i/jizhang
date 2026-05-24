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
    
    /**
     * 更新用户的所有目标为家庭目标（加入家庭组时调用）
     * 将用户的所有目标(family_group_id)更新为指定的家庭组ID
     * 不管family_group_id原来是什么值，都强制更新为新的家庭组ID
     */
    @Update("UPDATE user_target SET family_group_id = #{targetFamilyGroupId} WHERE user_id = #{userId}")
    int updateFamilyGroupId(@Param("userId") Long userId, @Param("targetFamilyGroupId") Long targetFamilyGroupId);
    
    /**
     * 将用户在此家庭组中的目标转换为个人目标（退出家庭组时调用）
     * 将family_group_id从指定值改为0
     */
    @Update("UPDATE user_target SET family_group_id = 0 WHERE user_id = #{userId} AND family_group_id = #{familyGroupId}")
    int updateFamilyGroupIdToPersonal(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    /**
     * 删除用户在此家庭组中的所有目标
     */
    @Delete("DELETE FROM user_target WHERE user_id = #{userId} AND family_group_id = #{familyGroupId}")
    int deleteByUserIdAndFamilyGroupId(@Param("userId") Long userId, @Param("familyGroupId") Long familyGroupId);
    
    /**
     * 查询家庭组指定月份的所有用户的目标汇总
     * @param familyGroupId 家庭组ID
     * @param targetMonth 目标年月（格式：YYYY-MM）
     * @return 汇总后的目标对象，如果没有数据返回null或创建一个incomeTarget为0的对象
     */
    @Select("SELECT MIN(id) as id, family_group_id as familyGroupId, #{targetMonth} as targetMonth, SUM(income_target) as incomeTarget FROM user_target WHERE family_group_id = #{familyGroupId} AND target_month = #{targetMonth}")
    UserTarget findByFamilyGroupIdAndMonthAggregated(@Param("familyGroupId") Long familyGroupId, @Param("targetMonth") String targetMonth);
    
    /**
     * 查询家庭组所有月份的目标汇总（按月份聚合）
     * @param familyGroupId 家庭组ID
     * @return 按月份聚合的目标列表
     */
    @Select("SELECT MIN(id) as id, family_group_id as familyGroupId, target_month as targetMonth, SUM(income_target) as incomeTarget FROM user_target WHERE family_group_id = #{familyGroupId} AND family_group_id > 0 GROUP BY target_month ORDER BY target_month DESC")
    List<UserTarget> findByFamilyGroupIdAggregated(@Param("familyGroupId") Long familyGroupId);
    
    /**
     * 查询家庭组指定月份范围的目标汇总
     * @param familyGroupId 家庭组ID
     * @param startMonth 起始年月（格式：YYYY-MM）
     * @param endMonth 结束年月（格式：YYYY-MM）
     * @return 按月份聚合的目标列表
     */
    @Select("SELECT MIN(id) as id, family_group_id as familyGroupId, target_month as targetMonth, SUM(income_target) as incomeTarget FROM user_target WHERE family_group_id = #{familyGroupId} AND family_group_id > 0 AND target_month >= #{startMonth} AND target_month <= #{endMonth} GROUP BY target_month ORDER BY target_month DESC")
    List<UserTarget> findByFamilyGroupIdAndMonthRangeAggregated(
            @Param("familyGroupId") Long familyGroupId,
            @Param("startMonth") String startMonth,
            @Param("endMonth") String endMonth);
    
    /**
     * 删除家庭组指定月份的所有用户的目标
     * 用于修改目标时清除该月份的所有用户源数据
     * @param familyGroupId 家庭组ID
     * @param targetMonth 目标年月（格式：YYYY-MM）
     * @return 删除的记录数
     */
    @Delete("DELETE FROM user_target WHERE family_group_id = #{familyGroupId} AND target_month = #{targetMonth}")
    int deleteByFamilyGroupIdAndMonth(@Param("familyGroupId") Long familyGroupId, @Param("targetMonth") String targetMonth);
    
    /**
     * 删除用户指定月份的个人目标
     * @param userId 用户ID
     * @param targetMonth 目标年月（格式：YYYY-MM）
     * @return 删除的记录数
     */
    @Delete("DELETE FROM user_target WHERE user_id = #{userId} AND target_month = #{targetMonth} AND family_group_id = 0")
    int deleteByUserIdAndMonth(@Param("userId") Long userId, @Param("targetMonth") String targetMonth);
}
