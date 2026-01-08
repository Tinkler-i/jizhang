package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.UserTarget;
import org.apache.ibatis.annotations.*;

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
    @Select("SELECT * FROM user_target WHERE user_id = #{userId} AND target_month = #{targetMonth}")
    UserTarget findByUserIdAndMonth(@Param("userId") Long userId, @Param("targetMonth") String targetMonth);
    
    /**
     * 新增
     */
    @Insert("INSERT INTO user_target (user_id, target_month, income_target) VALUES (#{userId}, #{targetMonth}, #{incomeTarget})")
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
}
