package com.billmanager.jizhang.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 权限模板实体
 */
@Data
public class PermissionTemplate {
    private Long id;
    private String name;              // 模板名称（查看者、记账员、管理员）
    private String description;       // 模板描述
    private String permissions;       // JSON格式的权限配置
    private Integer builtIn;          // 1系统内置、0自定义
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
