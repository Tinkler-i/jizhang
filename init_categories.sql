-- 初始化分类数据
-- 先查询用户ID（假设用户名为 test）
-- 替换 1 为实际的用户ID

-- 收入分类
INSERT INTO income_category (user_id, name, description, created_at, updated_at) VALUES 
(1, '工资', '工作薪水', NOW(), NOW()),
(1, '奖金', '绩效奖金', NOW(), NOW()),
(1, '利息', '存款利息', NOW(), NOW()),
(1, '投资收益', '股票分红等', NOW(), NOW()),
(1, '其他收入', '其他收入来源', NOW(), NOW());

-- 支出分类
INSERT INTO expense_category (user_id, name, description, created_at, updated_at) VALUES 
(1, '食物', '日常饮食', NOW(), NOW()),
(1, '交通', '车费、打车等', NOW(), NOW()),
(1, '娱乐', '电影、游戏等', NOW(), NOW()),
(1, '购物', '衣服、生活用品', NOW(), NOW()),
(1, '医疗', '看病、药物', NOW(), NOW()),
(1, '工作', '工作相关支出', NOW(), NOW()),
(1, '其他支出', '其他支出', NOW(), NOW());
