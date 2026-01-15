-- 迁移脚本：为 user 表添加 nickname 字段
-- 添加昵称字段，用于家庭组成员管理中显示和区分用户

ALTER TABLE `user` 
ADD COLUMN `nickname` VARCHAR(50) COMMENT '昵称，用于家庭组成员管理中显示' AFTER `username`;

-- 设置现有用户的昵称为其用户名
UPDATE `user` SET `nickname` = `username` WHERE `nickname` IS NULL;

-- 确保 nickname 字段有非空约束
ALTER TABLE `user` 
MODIFY COLUMN `nickname` VARCHAR(50) NOT NULL DEFAULT '';
