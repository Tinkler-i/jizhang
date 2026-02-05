package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.FamilyGroup;
import org.apache.ibatis.annotations.*;

/**
 * 家庭组 Mapper
 */
@Mapper
public interface FamilyGroupMapper {
    
    /**
     * 创建家庭组
     */
    @Insert("INSERT INTO family_group (code, name, description, creator_id, status) " +
            "VALUES (#{code}, #{name}, #{description}, #{creatorId}, #{status})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(FamilyGroup familyGroup);
    
    /**
     * 根据ID查询家庭组
     */
    @Select("SELECT * FROM family_group WHERE id = #{id}")
    FamilyGroup selectById(Long id);
    
    /**
     * 根据编码查询家庭组
     */
    @Select("SELECT * FROM family_group WHERE code = #{code}")
    FamilyGroup selectByCode(String code);
    
    /**
     * 根据创建者ID查询家庭组
     */
    @Select("SELECT * FROM family_group WHERE creator_id = #{creatorId}")
    FamilyGroup selectByCreatorId(Long creatorId);
    
    /**
     * 更新家庭组
     */
    @Update("UPDATE family_group SET name = #{name}, description = #{description}, " +
            "update_time = NOW() WHERE id = #{id}")
    void update(FamilyGroup familyGroup);
    
    /**
     * 删除家庭组
     */
    @Delete("DELETE FROM family_group WHERE id = #{id}")
    void delete(Long id);
}
