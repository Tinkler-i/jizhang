package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.FamilyMember;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 家庭成员 Mapper
 */
@Mapper
public interface FamilyMemberMapper {
    
    /**
     * 创建家庭成员
     */
    @Insert("INSERT INTO family_member (family_group_id, user_id, role, permissions, status) " +
            "VALUES (#{familyGroupId}, #{userId}, #{role}, #{permissions}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FamilyMember familyMember);
    
    /**
     * 根据ID查询家庭成员
     */
    @Select("SELECT * FROM family_member WHERE id = #{id}")
    FamilyMember selectById(Long id);
    
    /**
     * 根据用户ID查询家庭成员
     */
    @Select("SELECT * FROM family_member WHERE user_id = #{userId} AND status = 1")
    FamilyMember selectByUserId(Long userId);
    
    /**
     * 根据家庭组ID查询所有成员
     */
    @Select("SELECT * FROM family_member WHERE family_group_id = #{familyGroupId} AND status = 1 ORDER BY join_time ASC")
    List<FamilyMember> selectByFamilyGroupId(Long familyGroupId);
    
    /**
     * 根据家庭组ID和用户ID查询成员
     */
    @Select("SELECT * FROM family_member WHERE family_group_id = #{familyGroupId} AND user_id = #{userId} AND status = 1")
    FamilyMember selectByFamilyGroupAndUserId(@Param("familyGroupId") Long familyGroupId, @Param("userId") Long userId);
    
    /**
     * 根据家庭组ID和用户ID查询成员（包括历史记录）
     * 注：现在采用硬删除，所以此方法等同于selectByFamilyGroupAndUserId
     */
    @Select("SELECT * FROM family_member WHERE family_group_id = #{familyGroupId} AND user_id = #{userId}")
    FamilyMember selectByFamilyGroupAndUserIdIncludeDeleted(@Param("familyGroupId") Long familyGroupId, @Param("userId") Long userId);
    
    /**
     * 删除家庭组的所有成员
     */
    @Delete("DELETE FROM family_member WHERE family_group_id = #{familyGroupId}")
    void deleteByFamilyGroupId(Long familyGroupId);
    
    /**
     * 更新家庭成员权限
     */
    @Update("UPDATE family_member SET permissions = #{permissions}, update_time = NOW() WHERE id = #{id}")
    void updatePermissions(@Param("id") Long id, @Param("permissions") String permissions);
    
    /**
     * 更新家庭成员角色
     */
    @Update("UPDATE family_member SET role = #{role}, update_time = NOW() WHERE id = #{id}")
    void updateRole(@Param("id") Long id, @Param("role") String role);
    
    /**
     * 删除家庭成员
     */
    @Delete("DELETE FROM family_member WHERE id = #{id}")
    void deleteById(Long id);
    
    /**
     * 更新家庭成员状态
     */
    @Update("UPDATE family_member SET status = #{status}, update_time = NOW() WHERE id = #{id}")
    void updateStatus(@Param("id") Long id, @Param("status") int status);
    
    /**
     * 获取家庭组成员数量
     */
    @Select("SELECT COUNT(*) FROM family_member WHERE family_group_id = #{familyGroupId} AND status = 1")
    int countByFamilyGroupId(Long familyGroupId);
}
