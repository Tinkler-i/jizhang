-- ========================================
-- 家庭组管理模块数据库迁移脚本
-- 执行顺序：先执行此脚本，再更新Java代码
-- ========================================

-- 1. 创建家庭组表
CREATE TABLE IF NOT EXISTS family_group (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭组ID',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '家庭组编号（6位，如：FA8K3M）',
    name VARCHAR(100) NOT NULL DEFAULT '我的家庭' COMMENT '家庭组名称',
    description TEXT COMMENT '家庭介绍',
    creator_id BIGINT NOT NULL COMMENT '创建者ID（用户ID）',
    status TINYINT DEFAULT 1 COMMENT '1正常、0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_creator (creator_id) COMMENT '一个用户只能创建一个家庭组',
    KEY idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='家庭组表';

-- 2. 创建家庭成员表
CREATE TABLE IF NOT EXISTS family_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭成员ID',
    family_group_id BIGINT NOT NULL COMMENT '家庭组ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID（唯一约束：一个用户只在一个家庭组）',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'ADMIN管理员、MEMBER普通成员',
    permissions JSON COMMENT '权限配置JSON',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status TINYINT DEFAULT 1 COMMENT '1正常、0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (family_group_id) REFERENCES family_group(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    KEY idx_family (family_group_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='家庭成员表';

-- 3. 创建权限模板表
CREATE TABLE IF NOT EXISTS permission_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '模板名称（如：查看者、记账员、管理员）',
    description VARCHAR(200) COMMENT '模板描述',
    permissions JSON NOT NULL COMMENT '权限配置JSON',
    built_in TINYINT DEFAULT 1 COMMENT '1系统内置、0自定义',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT='权限模板表';

-- 4. 为现有表添加 family_group_id 字段
-- 注意：使用 DEFAULT 0 表示个人数据（非家族数据），当 family_group_id=0 时表示用户个人数据
ALTER TABLE income ADD COLUMN family_group_id BIGINT NOT NULL DEFAULT 0 COMMENT '家庭组ID（0表示个人数据）' AFTER user_id;
ALTER TABLE expense ADD COLUMN family_group_id BIGINT NOT NULL DEFAULT 0 COMMENT '家庭组ID（0表示个人数据）' AFTER user_id;
ALTER TABLE budget ADD COLUMN family_group_id BIGINT NOT NULL DEFAULT 0 COMMENT '家庭组ID（0表示个人数据）' AFTER user_id;
ALTER TABLE income_category ADD COLUMN family_group_id BIGINT NOT NULL DEFAULT 0 COMMENT '家庭组ID（0表示个人数据）' AFTER user_id;
ALTER TABLE expense_category ADD COLUMN family_group_id BIGINT NOT NULL DEFAULT 0 COMMENT '家庭组ID（0表示个人数据）' AFTER user_id;

-- 5. 为 family_group_id 添加索引
ALTER TABLE income ADD KEY idx_family_group (family_group_id);
ALTER TABLE expense ADD KEY idx_family_group (family_group_id);
ALTER TABLE budget ADD KEY idx_family_group (family_group_id);
ALTER TABLE income_category ADD KEY idx_family_group (family_group_id);
ALTER TABLE expense_category ADD KEY idx_family_group (family_group_id);

-- 6. 删除旧的 user_id 单字段索引（可选，保留以兼容旧查询）
-- 注意：这会影响旧代码，需要同时更新查询逻辑
-- ALTER TABLE income DROP KEY idx_user_id;
-- ALTER TABLE expense DROP KEY idx_user_id;
-- ALTER TABLE budget DROP KEY idx_user_id;

-- 7. 插入默认权限模板（必须执行）
INSERT INTO permission_template (name, description, permissions, built_in) VALUES
('查看者', '只能查看数据，不可创建和编辑', JSON_OBJECT(
    'income', JSON_OBJECT('view', true, 'create', false, 'edit', false, 'delete', false),
    'expense', JSON_OBJECT('view', true, 'create', false, 'edit', false, 'delete', false),
    'budget', JSON_OBJECT('view', true, 'create', false, 'edit', false, 'delete', false),
    'category', JSON_OBJECT('view', true, 'create', false, 'edit', false, 'delete', false),
    'member_management', JSON_OBJECT('view', false, 'invite', false, 'edit_permission', false, 'remove_member', false)
), 1),
('记账员', '可以创建和编辑，但不可删除', JSON_OBJECT(
    'income', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', false),
    'expense', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', false),
    'budget', JSON_OBJECT('view', true, 'create', false, 'edit', false, 'delete', false),
    'category', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', false),
    'member_management', JSON_OBJECT('view', true, 'invite', false, 'edit_permission', false, 'remove_member', false)
), 1),
('管理员', '拥有所有权限', JSON_OBJECT(
    'income', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', true),
    'expense', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', true),
    'budget', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', true),
    'category', JSON_OBJECT('view', true, 'create', true, 'edit', true, 'delete', true),
    'member_management', JSON_OBJECT('view', true, 'invite', true, 'edit_permission', true, 'remove_member', true)
), 1);
