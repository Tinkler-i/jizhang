-- 添加 is_built_in 字段到现有的income_category表（如果字段不存在）
ALTER TABLE `income_category` ADD COLUMN `is_built_in` TINYINT DEFAULT 0 COMMENT '是否系统内置分类：0-用户自定义，1-系统内置' 
AFTER `description`;

-- 添加 is_built_in 字段到现有的expense_category表（如果字段不存在）
ALTER TABLE `expense_category` ADD COLUMN `is_built_in` TINYINT DEFAULT 0 COMMENT '是否系统内置分类：0-用户自定义，1-系统内置' 
AFTER `description`;

-- 为所有用户创建"待分类"收入分类（如果不存在）
-- 从user表中获取所有用户，确保即使某个用户没有任何分类记录也会创建"待分类"
INSERT INTO `income_category` (`user_id`, `name`, `description`, `is_built_in`, `create_time`, `update_time`)
SELECT u.`id`, '待分类', '自动导入账单时未匹配分类的默认分类', 1, NOW(), NOW()
FROM `user` u
WHERE NOT EXISTS (
    SELECT 1 FROM `income_category` ic 
    WHERE ic.`user_id` = u.`id` 
    AND ic.`name` = '待分类'
    AND ic.`is_built_in` = 1
);

-- 为所有用户创建"待分类"支出分类（如果不存在）
-- 从user表中获取所有用户，确保即使某个用户没有任何分类记录也会创建"待分类"
INSERT INTO `expense_category` (`user_id`, `name`, `description`, `is_built_in`, `create_time`, `update_time`)
SELECT u.`id`, '待分类', '自动导入账单时未匹配分类的默认分类', 1, NOW(), NOW()
FROM `user` u
WHERE NOT EXISTS (
    SELECT 1 FROM `expense_category` ec 
    WHERE ec.`user_id` = u.`id` 
    AND ec.`name` = '待分类'
    AND ec.`is_built_in` = 1
);
