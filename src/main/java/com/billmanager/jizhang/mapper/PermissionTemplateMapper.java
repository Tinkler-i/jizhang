package com.billmanager.jizhang.mapper;

import com.billmanager.jizhang.entity.PermissionTemplate;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限模板 Mapper
 */
@Mapper
public interface PermissionTemplateMapper {
    
    /**
     * 创建权限模板
     */
    @Insert("INSERT INTO permission_template (name, description, permissions, built_in) " +
            "VALUES (#{name}, #{description}, #{permissions}, #{builtIn})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(PermissionTemplate template);
    
    /**
     * 根据ID查询权限模板
     */
    @Select("SELECT * FROM permission_template WHERE id = #{id}")
    PermissionTemplate selectById(Long id);
    
    /**
     * 根据名称查询权限模板
     */
    @Select("SELECT * FROM permission_template WHERE name = #{name}")
    PermissionTemplate selectByName(String name);
    
    /**
     * 查询所有权限模板
     */
    @Select("SELECT * FROM permission_template ORDER BY built_in DESC, id ASC")
    List<PermissionTemplate> selectAll();
    
    /**
     * 查询系统内置权限模板
     */
    @Select("SELECT * FROM permission_template WHERE built_in = 1 ORDER BY id ASC")
    List<PermissionTemplate> selectBuiltInTemplates();
    
    /**
     * 更新权限模板
     */
    @Update("UPDATE permission_template SET name = #{name}, description = #{description}, " +
            "permissions = #{permissions}, update_time = NOW() WHERE id = #{id}")
    void update(PermissionTemplate template);
    
    /**
     * 删除权限模板
     */
    @Delete("DELETE FROM permission_template WHERE id = #{id}")
    void deleteById(Long id);
}
