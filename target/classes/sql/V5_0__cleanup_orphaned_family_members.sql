-- ========================================
-- 清理孤立的家庭成员记录
-- 删除引用不存在的family_group的family_member记录
-- ========================================

DELETE FROM family_member 
WHERE family_group_id NOT IN (SELECT id FROM family_group);

-- 清理status字段（改为硬删除模式，不再使用status标记删除）
-- 如果有status=0的记录，现在也应该直接删除
DELETE FROM family_member WHERE status = 0;
