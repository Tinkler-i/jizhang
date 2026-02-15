-- ================================================
-- 家庭账务系统测试数据 - 一年日常收支记录
-- 用户ID: 23, 家庭组ID: 0
-- ================================================

-- ================================================
-- 1. 先查询或确保user_id=23存在
-- ================================================
-- 假设user_id=23已存在，如果不存在需要先插入

-- ================================================
-- 2. 插入收入分类（income_category）
-- ================================================
DELETE FROM income_category WHERE user_id = 23 AND family_group_id = 0;

INSERT INTO income_category (user_id, family_group_id, name, description, is_built_in) VALUES
(23, 0, '工资', '月度工资收入', 0),
(23, 0, '奖金', '各类奖金和补贴', 0),
(23, 0, '投资收益', '股票、基金、理财收益', 0),
(23, 0, '兼职', '兼职或副业收入', 0),
(23, 0, '其他收入', '其他杂项收入', 0);

-- ================================================
-- 3. 插入支出分类（expense_category）
-- ================================================
DELETE FROM expense_category WHERE user_id = 23 AND family_group_id = 0;

INSERT INTO expense_category (user_id, family_group_id, name, description, is_built_in) VALUES
(23, 0, '房租', '住房租赁费用', 0),
(23, 0, '食物', '日常饮食和食材', 0),
(23, 0, '交通', '公车、地铁、打车、油费', 0),
(23, 0, '娱乐', '电影、游戏、聚会等', 0),
(23, 0, '购物', '衣服、日用品等', 0),
(23, 0, '医疗', '医疗和健康支出', 0),
(23, 0, '水电气', '水电气费用', 0),
(23, 0, '手机网络', '手机费、网费', 0),
(23, 0, '运动健身', '健身房、运动器材', 0),
(23, 0, '学习培训', '课程、书籍等学习费用', 0);

-- ================================================
-- 4. 插入收入记录（income）- 2025年全年数据
-- ================================================
DELETE FROM income WHERE user_id = 23 AND family_group_id = 0;

-- 2025年1月-12月，每月工资和奖金
INSERT INTO income (user_id, family_group_id, category_id, amount, transaction_date, description) VALUES
-- 1月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-01-01', '1月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='其他收入' LIMIT 1), 200, '2025-01-15', '过年红包'),

-- 2月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-02-01', '2月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='兼职' LIMIT 1), 500, '2025-02-10', '周末兼职'),

-- 3月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-03-01', '3月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='投资收益' LIMIT 1), 300, '2025-03-15', '基金分红'),

-- 4月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-04-01', '4月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='奖金' LIMIT 1), 2000, '2025-04-20', '项目奖金'),

-- 5月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-05-01', '5月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='其他收入' LIMIT 1), 300, '2025-05-10', '旧物出售'),

-- 6月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-06-01', '6月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='投资收益' LIMIT 1), 400, '2025-06-15', '理财产品'),

-- 7月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-07-01', '7月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='兼职' LIMIT 1), 800, '2025-07-20', '暑期兼职'),

-- 8月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-08-01', '8月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='奖金' LIMIT 1), 1500, '2025-08-15', '绩效奖金'),

-- 9月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-09-01', '9月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='其他收入' LIMIT 1), 250, '2025-09-10', '写作稿费'),

-- 10月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-10-01', '10月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='投资收益' LIMIT 1), 500, '2025-10-20', '股票分红'),

-- 11月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-11-01', '11月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='奖金' LIMIT 1), 3000, '2025-11-25', '年底奖金'),

-- 12月
(23, 0, (SELECT id FROM income_category WHERE name='工资' LIMIT 1), 8000, '2025-12-01', '12月工资'),
(23, 0, (SELECT id FROM income_category WHERE name='其他收入' LIMIT 1), 1000, '2025-12-20', '圣诞奖励');

-- ================================================
-- 5. 插入支出记录（expense）- 2025年全年数据
-- ================================================
DELETE FROM expense WHERE user_id = 23 AND family_group_id = 0;

-- 每月房租和日常支出
INSERT INTO expense (user_id, family_group_id, category_id, amount, transaction_date, description) VALUES
-- 1月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-01-01', '1月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-01-02', '超市购物'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 45, '2025-01-03', '公交卡充值'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-01-04', '外出就餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 80, '2025-01-05', '电影票'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 120, '2025-01-06', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-01-07', '餐饮'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-01-08', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-01-09', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-01-10', '买药'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 130, '2025-01-11', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-01-12', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 150, '2025-01-13', '在线课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-01-14', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-01-15', '健身房'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-01-16', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-01-17', '游戏充值'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 80, '2025-01-18', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 120, '2025-01-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-01-20', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 30, '2025-01-21', '地铁'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 125, '2025-01-22', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 60, '2025-01-23', '唱歌'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-01-24', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 200, '2025-01-25', '箱包'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 130, '2025-01-26', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 80, '2025-01-27', '口腔护理'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-01-28', '酒店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-01-29', '看演出'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 40, '2025-01-30', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-01-31', '聚餐'),

-- 2月（28天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-02-01', '2月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-02-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-02-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-02-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 50, '2025-02-05', '网费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 130, '2025-02-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 150, '2025-02-07', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-02-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 80, '2025-02-09', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-02-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 100, '2025-02-11', '书籍'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-02-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-02-13', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 120, '2025-02-14', '足疗'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 200, '2025-02-15', '情人节大餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-02-16', '彩票'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-02-17', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 100, '2025-02-18', '箱包'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 110, '2025-02-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-02-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 150, '2025-02-21', '健身卡'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 130, '2025-02-22', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 45, '2025-02-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-02-24', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 70, '2025-02-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-02-26', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 120, '2025-02-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-02-28', '餐厅'),

-- 3月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-03-01', '3月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-03-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-03-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-03-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 90, '2025-03-05', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-03-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 180, '2025-03-07', '鞋子'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-03-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-03-09', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-03-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-03-11', '在线课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-03-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 150, '2025-03-13', '体检'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-03-14', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-03-15', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-03-16', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-03-17', '唱歌'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 140, '2025-03-18', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 125, '2025-03-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-03-20', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 180, '2025-03-21', '健身房'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-03-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-03-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 130, '2025-03-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 80, '2025-03-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-03-26', '火锅'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-03-27', '箱包'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-03-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-03-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-03-30', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 110, '2025-03-31', '看演出(春游'),

-- 4月（30天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-04-01', '4月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-04-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 55, '2025-04-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-04-04', '清明节饺子'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-04-05', '春游'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-04-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 200, '2025-04-07', '春装'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-04-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 150, '2025-04-09', '书籍'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-04-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-04-11', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-04-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-04-13', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-04-14', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 180, '2025-04-15', '眼镜'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-04-16', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 85, '2025-04-17', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 120, '2025-04-18', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 115, '2025-04-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-04-20', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-04-21', '健身卡续费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-04-22', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 45, '2025-04-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-04-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 90, '2025-04-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-04-26', '烧烤'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-04-27', '箱包'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-04-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-04-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-04-30', '五一聚餐'),

-- 5月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-05-01', '5月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-05-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 100, '2025-05-03', '五一出行'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 250, '2025-05-04', '五一自驾'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-05-05', '景点门票'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-05-06', '旅游餐饮'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 150, '2025-05-07', '旅游纪念品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-05-08', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-05-09', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-05-10', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-05-11', '在线课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-05-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-05-13', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-05-14', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 120, '2025-05-15', '理疗'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-05-16', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-05-17', '看演出'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 180, '2025-05-18', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 130, '2025-05-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-05-20', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 150, '2025-05-21', '健身房'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-05-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 55, '2025-05-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-05-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 95, '2025-05-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-05-26', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 140, '2025-05-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-05-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 110, '2025-05-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-05-30', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-05-31', '演唱会'),

-- 6月（30天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-06-01', '6月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-06-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-06-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-06-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-06-05', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-06-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-06-07', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-06-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 180, '2025-06-09', '书籍'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-06-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-06-11', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-06-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-06-13', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-06-14', '米面'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-06-15', '口腔'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-06-16', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 110, '2025-06-17', '唱歌'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 150, '2025-06-18', '箱包'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 140, '2025-06-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-06-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-06-21', '健身课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-06-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-06-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-06-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 85, '2025-06-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-06-26', '日料'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 130, '2025-06-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-06-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-06-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-06-30', '外卖'),

-- 7月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-07-01', '7月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-07-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-07-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-07-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-07-05', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-07-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 200, '2025-07-07', '夏装'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-07-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-07-09', '在线课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-07-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-07-11', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-07-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 55, '2025-07-13', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-07-14', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 150, '2025-07-15', '体检'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-07-16', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-07-17', '游泳'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-07-18', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 180, '2025-07-19', '水电费-夏季'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-07-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-07-21', '健身房'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-07-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-07-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-07-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-07-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 190, '2025-07-26', '烤肉'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 150, '2025-07-27', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-07-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-07-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-07-30', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 130, '2025-07-31', '看演出'),

-- 8月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-08-01', '8月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-08-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 100, '2025-08-03', '自驾'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 250, '2025-08-04', '旅游'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 150, '2025-08-05', '景点'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 200, '2025-08-06', '旅游餐饮'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 180, '2025-08-07', '纪念品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-08-08', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-08-09', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-08-10', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-08-11', '书籍'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-08-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-08-13', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-08-14', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 180, '2025-08-15', '按摩'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-08-16', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-08-17', '唱歌'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-08-18', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 170, '2025-08-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-08-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-08-21', '健身卡'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-08-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 55, '2025-08-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-08-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-08-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 185, '2025-08-26', '烤肉'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 140, '2025-08-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-08-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 110, '2025-08-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-08-30', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 130, '2025-08-31', '看演出'),

-- 9月（30天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-09-01', '9月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-09-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-09-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-09-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-09-05', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-09-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 170, '2025-09-07', '秋装'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-09-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-09-09', '在线课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-09-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-09-11', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-09-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-09-13', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-09-14', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 150, '2025-09-15', '检查'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-09-16', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 110, '2025-09-17', '演唱会'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 150, '2025-09-18', '箱包'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 125, '2025-09-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-09-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-09-21', '健身房'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-09-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-09-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-09-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 95, '2025-09-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-09-26', '火锅'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 130, '2025-09-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-09-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-09-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-09-30', '便利店'),

-- 10月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-10-01', '10月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-10-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 80, '2025-10-03', '十一出行'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 280, '2025-10-04', '十一旅游'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 180, '2025-10-05', '景点门票'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 220, '2025-10-06', '旅游餐饮'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 200, '2025-10-07', '旅游购物'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-10-08', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-10-09', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-10-10', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-10-11', '书籍'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 135, '2025-10-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-10-13', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-10-14', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 140, '2025-10-15', '体检'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-10-16', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-10-17', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-10-18', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 120, '2025-10-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-10-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-10-21', '健身卡'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-10-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-10-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-10-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-10-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-10-26', '烤肉'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 140, '2025-10-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-10-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-10-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-10-30', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-10-31', '看演出'),

-- 11月（30天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-11-01', '11月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-11-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-11-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-11-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 100, '2025-11-05', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-11-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 200, '2025-11-07', '双十一'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-11-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-11-09', '在线课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-11-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-11-11', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-11-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-11-13', '出租车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-11-14', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 160, '2025-11-15', '理疗'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-11-16', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 110, '2025-11-17', '唱歌'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-11-18', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 110, '2025-11-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-11-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-11-21', '健身课程'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-11-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-11-23', '公交'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 140, '2025-11-24', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 95, '2025-11-25', '游戏'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 185, '2025-11-26', '火锅'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 140, '2025-11-27', '日用品'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-11-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-11-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 160, '2025-11-30', '便利店'),

-- 12月（31天）
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-12-01', '12月房租'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-12-02', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-12-03', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-12-04', '餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 120, '2025-12-05', '电影'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-12-06', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 220, '2025-12-07', '圣诞采购'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-12-08', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 200, '2025-12-09', '书籍'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-12-10', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 99, '2025-12-11', '手机费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 145, '2025-12-12', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 50, '2025-12-13', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 165, '2025-12-14', '圣诞餐厅'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 150, '2025-12-15', '检查'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 180, '2025-12-16', '聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 130, '2025-12-17', '演唱会'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 180, '2025-12-18', '衣服'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 140, '2025-12-19', '水电费'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 155, '2025-12-20', '便利店'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 200, '2025-12-21', '健身房'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 170, '2025-12-22', '外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 60, '2025-12-23', '打车'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-12-24', '圣诞外卖'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 150, '2025-12-25', '圣诞派对'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 200, '2025-12-26', '圣诞大餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 160, '2025-12-27', '新年采购'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 150, '2025-12-28', '超市'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 100, '2025-12-29', '药店'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 175, '2025-12-30', '跨年聚餐'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 200, '2025-12-31', '跨年庆祝');

-- ================================================
-- 6. 插入预算记录（budget）- 按月配置
-- ================================================
DELETE FROM budget WHERE user_id = 23 AND family_group_id = 0;

-- 2025年全月度预算设置
INSERT INTO budget (user_id, family_group_id, category_id, amount, budget_month) VALUES
-- 1月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 500, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 800, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-01'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-01'),

-- 2月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 500, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 800, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-02'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-02'),

-- 3月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 600, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 1000, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 300, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-03'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-03'),

-- 4月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 400, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 600, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 1000, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 300, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-04'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-04'),

-- 5月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2500, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 800, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 1000, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 1500, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 300, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-05'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-05'),

-- 6月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 500, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 800, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-06'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-06'),

-- 7月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 600, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 1000, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 300, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 300, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-07'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-07'),

-- 8月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2500, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 800, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 1000, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 1500, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 300, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 300, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-08'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-08'),

-- 9月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 500, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 800, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 400, '2025-09'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-09'),

-- 10月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2500, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 800, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 1200, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 1500, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 300, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-10'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-10'),

-- 11月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 300, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 500, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 2000, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 200, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-11'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-11'),

-- 12月预算
(23, 0, (SELECT id FROM expense_category WHERE name='房租' LIMIT 1), 2500, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='食物' LIMIT 1), 2000, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='交通' LIMIT 1), 400, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='娱乐' LIMIT 1), 800, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='购物' LIMIT 1), 2500, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='医疗' LIMIT 1), 200, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='水电气' LIMIT 1), 250, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='手机网络' LIMIT 1), 150, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='学习培训' LIMIT 1), 300, '2025-12'),
(23, 0, (SELECT id FROM expense_category WHERE name='运动健身' LIMIT 1), 300, '2025-12');

-- ================================================
-- 数据插入完成
-- ================================================
SELECT '✓ 测试数据插入完成！共插入：' as '提示';
SELECT concat(count(*), ' 条收入记录') FROM income WHERE user_id = 23;
SELECT concat(count(*), ' 条支出记录') FROM expense WHERE user_id = 23;
SELECT concat(count(*), ' 条预算配置') FROM budget WHERE user_id = 23;
