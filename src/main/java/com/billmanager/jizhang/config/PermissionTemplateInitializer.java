package com.billmanager.jizhang.config;

import com.billmanager.jizhang.entity.PermissionTemplate;
import com.billmanager.jizhang.mapper.PermissionTemplateMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 权限管理配置初始化器
 * 
 * 已禁用：前端不再使用权限模板选择
 * 在应用启动时，自动初始化系统权限模板
 * 如果模板已存在则不重复创建
 */
// @Component
@RequiredArgsConstructor
@Slf4j
public class PermissionTemplateInitializer implements CommandLineRunner {
    
    private final PermissionTemplateMapper permissionTemplateMapper;
    
    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("【权限配置】开始初始化权限模板");
            
            // 初始化查看者模板
            initializeViewerTemplate();
            
            // 初始化记账员模板
            initializeRecorderTemplate();
            
            // 初始化管理员模板
            initializeAdminTemplate();
            
            log.info("【权限配置】权限模板初始化完成");
        } catch (Exception e) {
            log.error("【权限配置】权限模板初始化失败", e);
            throw e;
        }
    }
    
    /**
     * 初始化查看者权限模板
     * 权限：仅查看，不能修改
     */
    private void initializeViewerTemplate() {
        String templateName = "查看者";
        
        PermissionTemplate existing = permissionTemplateMapper.selectByName(templateName);
        if (existing != null) {
            log.info("【权限配置】权限模板 {} 已存在，跳过初始化", templateName);
            return;
        }
        
        String permissionsJson = "{\n" +
            "  \"income\": { \"view\": true, \"create\": false, \"edit\": false, \"delete\": false },\n" +
            "  \"expense\": { \"view\": true, \"create\": false, \"edit\": false, \"delete\": false },\n" +
            "  \"budget\": { \"view\": true, \"create\": false, \"edit\": false, \"delete\": false },\n" +
            "  \"category\": { \"view\": true, \"create\": false, \"edit\": false, \"delete\": false },\n" +
            "  \"member_management\": { \"view\": false, \"invite\": false, \"edit_permission\": false, \"remove_member\": false }\n" +
            "}";
        
        PermissionTemplate template = new PermissionTemplate();
        template.setName(templateName);
        template.setDescription("查看者 - 仅可查看家庭账本数据，不能修改");
        template.setPermissions(permissionsJson);
        template.setBuiltIn(1);
        
        permissionTemplateMapper.insert(template);
        log.info("【权限配置】成功创建权限模板：{}", templateName);
    }
    
    /**
     * 初始化记账员权限模板
     * 权限：可以创建、编辑记账数据，但不能删除，不能管理成员
     */
    private void initializeRecorderTemplate() {
        String templateName = "记账员";
        
        PermissionTemplate existing = permissionTemplateMapper.selectByName(templateName);
        if (existing != null) {
            log.info("【权限配置】权限模板 {} 已存在，跳过初始化", templateName);
            return;
        }
        
        String permissionsJson = "{\n" +
            "  \"income\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": false },\n" +
            "  \"expense\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": false },\n" +
            "  \"budget\": { \"view\": true, \"create\": false, \"edit\": false, \"delete\": false },\n" +
            "  \"category\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": false },\n" +
            "  \"member_management\": { \"view\": true, \"invite\": false, \"edit_permission\": false, \"remove_member\": false }\n" +
            "}";
        
        PermissionTemplate template = new PermissionTemplate();
        template.setName(templateName);
        template.setDescription("记账员 - 可以新增和编辑账目和分类，但不能删除");
        template.setPermissions(permissionsJson);
        template.setBuiltIn(1);
        
        permissionTemplateMapper.insert(template);
        log.info("【权限配置】成功创建权限模板：{}", templateName);
    }
    
    /**
     * 初始化管理员权限模板
     * 权限：所有权限，包括成员管理
     */
    private void initializeAdminTemplate() {
        String templateName = "管理员";
        
        PermissionTemplate existing = permissionTemplateMapper.selectByName(templateName);
        if (existing != null) {
            log.info("【权限配置】权限模板 {} 已存在，跳过初始化", templateName);
            return;
        }
        
        String permissionsJson = "{\n" +
            "  \"income\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": true },\n" +
            "  \"expense\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": true },\n" +
            "  \"budget\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": true },\n" +
            "  \"category\": { \"view\": true, \"create\": true, \"edit\": true, \"delete\": true },\n" +
            "  \"member_management\": { \"view\": true, \"invite\": true, \"edit_permission\": true, \"remove_member\": true }\n" +
            "}";
        
        PermissionTemplate template = new PermissionTemplate();
        template.setName(templateName);
        template.setDescription("管理员 - 拥有所有权限，包括成员管理");
        template.setPermissions(permissionsJson);
        template.setBuiltIn(1);
        
        permissionTemplateMapper.insert(template);
        log.info("【权限配置】成功创建权限模板：{}", templateName);
    }
}
