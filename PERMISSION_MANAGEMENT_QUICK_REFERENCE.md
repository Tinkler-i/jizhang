# 家庭组权限管理 - 快速参考

**快速检查清单** ✓

---

## 🎯 核心功能

| # | 功能 | 文件 | 说明 |
|----|------|------|------|
| 1 | 权限验证工具 | `util/PermissionValidator.java` | 对象级权限检查 |
| 2 | 权限审计日志 | `entity/PermissionAuditLog.java` | 权限变更记录 |
| 3 | 批量权限操作 | `service/FamilyMemberService.java` | 批量更新权限/角色 |
| 4 | 权限冲突检查 | `util/PermissionValidator.java` | 自动检测配置冲突 |
| 5 | 权限初始化 | `config/PermissionTemplateInitializer.java` | 启动自动初始化 |
| 6 | API 接口 | `controller/FamilyMemberController.java` | 6个新增接口 |

---

## 🔌 API 速查

### 权限模板
```bash
GET /api/family-members/permission-templates
```

### 权限详情
```bash
GET /api/family-members/{memberId}/permission-details
```

### 权限冲突检查
```bash
POST /api/family-members/check-permission-conflicts
Body: { "permissions": "..." }
```

### 批量权限更新
```bash
POST /api/family-members/batch/permissions
Body: { "memberIds": [1,2,3], "permissionTemplateName": "记账员" }
```

### 审计日志
```bash
GET /api/family-members/audit-log/family           # 家庭组权限变更
GET /api/family-members/{memberId}/audit-log       # 成员权限变更
```

---

## 💡 使用示例

### 示例1: 检查权限冲突
```java
List<String> conflicts = familyMemberService.checkPermissionConflicts(permissionsJson);
if (!conflicts.isEmpty()) {
    throw new BusinessException("权限配置有冲突");
}
```

### 示例2: 验证编辑权限
```java
PermissionValidator.validateEditPermission(
    creatorId, userId, familyMember, "expense"
);
```

### 示例3: 记录权限变更
```java
permissionAuditLogService.logPermissionUpdate(
    familyGroupId, operatorId, memberId, targetUserId,
    oldPermissions, newPermissions
);
```

### 示例4: 获取权限详情
```java
Map<String, Object> details = familyMemberService.getMemberPermissionDetails(memberId);
// 返回解析后的权限信息
```

---

## 📊 权限结构

### 模块权限 (4个模块)
- income: 收入
- expense: 支出
- budget: 预算
- category: 分类

### 操作权限 (4项操作)
- view: 查看
- create: 创建
- edit: 编辑
- delete: 删除

### 成员管理权限 (4项操作)
- view: 查看
- invite: 邀请
- edit_permission: 编辑权限
- remove_member: 移除成员

---

## 🔍 权限模板速查

| 模板 | 收入 | 支出 | 预算 | 分类 | 成员管理 |
|------|------|------|------|------|---------|
| 查看者 | V--- | V--- | V--- | V--- | ------ |
| 记账员 | VCE- | VCE- | V--- | VCE- | V----- |
| 管理员 | VCED | VCED | VCED | VCED | VCECC  |

*V=查看, C=创建, E=编辑, D=删除*

---

## ⚡ 常用操作

### 批量更新权限为"记账员"
```bash
curl -X POST http://localhost:8080/api/family-members/batch/permissions \
  -H "Content-Type: application/json" \
  -d '{
    "memberIds": [2, 3, 4],
    "permissionTemplateName": "记账员"
  }'
```

### 查看家庭组权限变更历史
```bash
curl -X GET http://localhost:8080/api/family-members/audit-log/family
```

### 获取成员权限详情
```bash
curl -X GET http://localhost:8080/api/family-members/2/permission-details
```

---

## 📋 权限检查顺序

```
1. @FamilyPermission注解检查
   ↓
2. FamilyPermissionAspect切面拦截
   ↓
3. PermissionService.hasPermission()
   ↓
4. 业务逻辑执行
   ↓
5. PermissionValidator对象级检查
   ↓
6. 执行操作/拒绝操作
   ↓
7. PermissionAuditLogService记录
```

---

## 🛡️ 权限冲突规则

❌ 不能有的情况:
1. 删除权 without 编辑权
2. 编辑权 without 查看权
3. 创建权 without 查看权

✓ 合法的配置:
1. 仅查看权
2. 查看 + 创建权
3. 查看 + 编辑权
4. 查看 + 编辑 + 删除权

---

## 📊 SQL 查询速查

### 获取用户所有权限变更
```sql
SELECT * FROM permission_audit_log 
WHERE target_user_id = 2 
ORDER BY create_time DESC;
```

### 获取管理员的所有操作
```sql
SELECT * FROM permission_audit_log 
WHERE operator_id = 1 
ORDER BY create_time DESC;
```

### 获取最近7天的权限变更
```sql
SELECT * FROM permission_audit_log 
WHERE create_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) 
ORDER BY create_time DESC;
```

### 统计权限变更次数
```sql
SELECT operation_type, COUNT(*) as count 
FROM permission_audit_log 
GROUP BY operation_type;
```

---

## 🔑 关键类速查

| 类 | 用途 |
|-----|------|
| PermissionValidator | 对象级权限验证 |
| PermissionAuditLog | 审计日志实体 |
| PermissionAuditLogService | 审计日志操作 |
| FamilyMemberService | 成员权限管理 |
| FamilyMemberController | 权限管理API |
| PermissionTemplateInitializer | 权限初始化 |

---

## 🚀 开发检查清单

### 添加新权限检查时:
- [ ] 在@FamilyPermission注解中声明权限
- [ ] 在业务逻辑中调用validateXxxPermission()
- [ ] 操作成功后记录审计日志
- [ ] 在PermissionValidator中添加验证逻辑

### 添加新API端点时:
- [ ] 添加权限检查逻辑
- [ ] 记录操作审计日志
- [ ] 返回标准响应格式
- [ ] 添加错误处理

### 修改权限模板时:
- [ ] 检查权限冲突
- [ ] 验证向后兼容性
- [ ] 记录变更日志
- [ ] 测试权限生效

---

## 📞 常见问题

**Q: 权限何时生效?**
A: 用户下次请求时生效。建议清除权限缓存确保立即生效。

**Q: 能否自定义权限模板?**
A: 目前支持3个标准模板。自定义权限需要修改权限验证逻辑。

**Q: 如何查看权限变更历史?**
A: 通过 `/api/family-members/audit-log/family` 接口查看。

**Q: 权限数据如何备份?**
A: 定期导出permission_audit_log表进行备份。

---

## 📌 重要提示

⚠️ **必读**:
1. 系统启动时会自动初始化权限模板，无需手动操作
2. 权限变更会自动记录审计日志，便于审计
3. 权限冲突会被自动检测并提示
4. 所有权限操作都需要管理员权限

---

**最后更新**: 2026年1月13日  
**版本**: 2.0
