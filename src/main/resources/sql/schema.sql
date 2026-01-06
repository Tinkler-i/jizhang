CREATE DATABASE IF NOT EXISTS bill_manager DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE bill_manager;

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL COMMENT '用户名',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-正常，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

DROP TABLE IF EXISTS `income_category`;
CREATE TABLE `income_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) COMMENT '分类描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收入分类表';

DROP TABLE IF EXISTS `income`;
CREATE TABLE `income` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收入ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '收入金额',
    `transaction_date` DATE NOT NULL COMMENT '交易日期',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收入记录表';

DROP TABLE IF EXISTS `expense_category`;
CREATE TABLE `expense_category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `description` VARCHAR(200) COMMENT '分类描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支出分类表';

DROP TABLE IF EXISTS `expense`;
CREATE TABLE `expense` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '支出ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '支出金额',
    `transaction_date` DATE NOT NULL COMMENT '交易日期',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`),
    KEY `idx_transaction_date` (`transaction_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支出记录表';

DROP TABLE IF EXISTS `budget`;
CREATE TABLE `budget` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '预算ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `category_id` BIGINT COMMENT '分类ID（可为空，表示总预算）',
    `name` VARCHAR(100) NOT NULL COMMENT '预算名称',
    `amount` DECIMAL(10,2) NOT NULL COMMENT '预算金额',
    `start_date` DATE NOT NULL COMMENT '开始日期',
    `end_date` DATE NOT NULL COMMENT '结束日期',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_category_id` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预算表';

DROP TABLE IF EXISTS `tax_record`;
CREATE TABLE `tax_record` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '税务记录ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tax_type` VARCHAR(50) NOT NULL COMMENT '税种',
    `taxable_amount` DECIMAL(10,2) NOT NULL COMMENT '应税金额',
    `tax_rate` DECIMAL(5,4) NOT NULL COMMENT '税率',
    `tax_amount` DECIMAL(10,2) NOT NULL COMMENT '税额',
    `tax_date` DATE NOT NULL COMMENT '税务日期',
    `description` VARCHAR(500) COMMENT '描述',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tax_date` (`tax_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='税务记录表';

DROP TABLE IF EXISTS `tax_rule`;
CREATE TABLE `tax_rule` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '规则ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `tax_type` VARCHAR(50) NOT NULL COMMENT '税种',
    `tax_rate` DECIMAL(5,4) NOT NULL COMMENT '税率',
    `min_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '最小金额',
    `max_amount` DECIMAL(10,2) COMMENT '最大金额',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1-启用，0-禁用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_tax_type` (`tax_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='税率规则表';

INSERT INTO `user` (`username`, `password`, `phone`, `email`, `status`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '13800138000', 'admin@example.com', 1);

INSERT INTO `income_category` (`user_id`, `name`, `description`) VALUES
(1, '工资', '工资收入'),
(1, '奖金', '奖金收入'),
(1, '投资收益', '投资理财收益'),
(1, '兼职收入', '兼职工作收入'),
(1, '其他', '其他收入');

INSERT INTO `expense_category` (`user_id`, `name`, `description`) VALUES
(1, '餐饮', '餐饮消费'),
(1, '交通', '交通出行'),
(1, '购物', '购物消费'),
(1, '娱乐', '娱乐消费'),
(1, '住房', '住房相关支出'),
(1, '医疗', '医疗健康'),
(1, '教育', '教育培训'),
(1, '其他', '其他支出');