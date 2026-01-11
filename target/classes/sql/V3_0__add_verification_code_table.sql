-- 创建验证码表
DROP TABLE IF EXISTS `verification_code`;
CREATE TABLE `verification_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `code` VARCHAR(10) NOT NULL COMMENT '验证码（4位数字）',
    `type` VARCHAR(20) NOT NULL COMMENT '验证方式：EMAIL、SMS',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `ttl` INT DEFAULT 300 COMMENT '生存时间（秒），默认5分钟',
    `is_used` TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
    PRIMARY KEY (`id`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';
