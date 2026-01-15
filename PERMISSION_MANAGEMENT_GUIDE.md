# 家庭组权限管理 - 完整实现指南

**更新日期**: 2026年1月13日  
**版本**: 2.0  
**状态**: 完善权限管理功能

---

## 📋 核心改进内容

本文档详细说明家庭组权限管理模块的完善内容，包括细粒度权限控制、权限审计日志、权限冲突检查等功能。

---

## 🔐 权限体系详解

### 权限模板分类（3个标准模板）

#### 1. 查看者 (Viewer)
- **适用场景**: 家庭成员仅需查看账本，不需要修改
- **权限配置**:
  ```json
  {
    "income": { "view": true, "create": false, "edit": false, "delete": false },
    "expense": { "view": true, "create": false, "edit": false, "delete": false },
    "budget": { "view": true, "create": false, "edit": false, "delete": false },
    "category": { "view": true, "create": false, "edit": false, "delete": false },
    "member_management": { "view": false, "invite": false, "edit_permission": false, "remove_member": false }
  }
  ```

#### 2. 记账员 (Recorder)
- **适用场景**: 家庭成员可以记账，管理账目和分类，但不能删除
- **权限配置**:
  ```json
  {
    "income": { "view": true, "create": true, "edit": true, "delete": false },
    "expense": { "view": true, "create": true, "edit": true, "delete": false },
    "budget": { "view": true, "create": false, "edit": false, "delete": false },
    "category": { "view": true, "create": true, "edit": true, "delete": false },
    "member_management": { "view": true, "invite": false, "edit_permission": false, "remove_member": false }
  }
  ```

#### 3. 管理员 (Admin)
- **适用场景**: 家庭组管理员，拥有所有权限
- **权限配置**:
  ```json
  {
    "income": { "view": true, "create": true, "edit": true, "delete": true },
    "expense": { "view": true, "create": true, "edit": true, "delete": true },
    "budget": { "view": true, "create": true, "edit": true, "delete": true },
    "category": { "view": true, "create": true, "edit": true, "delete": true },
    "member_management": { "view": true, "invite": true, "edit_permission": true, "remove_member": true }
  }
  ```

---

## 🛡️ 权限验证流程

### 方法级权限验证 (@FamilyPermission注解)

```
HTTP请求
  ↓
@FamilyPermission("expense_create") 拦截
  ↓
FamilyPermissionAspect切面处理
  ↓
1. 获取当前用户ID
2. 从family_member查询用户权限
3. 解析权限JSON
4. 检查权限标识（expense_create）
  ↓
✅ 权限充足 → 继续执行业务逻辑
❌ 权限不足 → 抛出BusinessException
```

### 对象级权限验证（PermissionValidator工具类）

```
对象操作请求（编辑/删除）
  ↓
PermissionValidator.validateEditPermission()
  ↓
检查规则：
  1. 管理员？ → YES → 允许
  2. 创建者？ → YES → 允许（仅编辑）
  3. 其他用户 → 拒绝
  ↓
返回验证结果
```

---

## 📊 新增功能详解

### 1. 权限验证工具类 (PermissionValidator)

**文件**: `util/PermissionValidator.java`

提供以下验证方法：

#### validateEditPermission() - 编辑权限验证
```java
PermissionValidator.validateEditPermission(
    creatorId,          // 资源创建者ID
    userId,             // 当前用户ID
    familyMember,       // 成员信息（含权限）
    "expense"           // 模块名
);
```

**规则**:
- 管理员可编辑所有资源
- 创建者可编辑自己的资源
- 其他用户无法编辑

#### validateDeletePermission() - 删除权限验证
```java
PermissionValidator.validateDeletePermission(
    creatorId,
    userId,
    familyMember,
    "expense"
);
```

**规则**:
- 仅管理员可删除

#### validatePermissionManagement() - 权限管理验证
```java
PermissionValidator.validatePermissionManagement(userId, familyMember);
```

**规则**:
- 仅管理员可管理权限

#### validateRemoveMember() - 移除成员验证
```java
PermissionValidator.validateRemoveMember(
    userId,          // 操作者
    targetUserId,    // 目标成员
    familyMember
);
```

**规则**:
- 仅管理员可移除成员
- 不能移除自己

#### validateBatchOperation() - 批量操作验证
```java
PermissionValidator.validateBatchOperation(
    userId,
    resourceOwnerIds,  // 所有资源所有者ID列表
    familyMember,
    "edit"             // 操作类型
);
```

**规则**:
- 管理员可批量操作所有资源
- 普通成员仅能批量操作自己的资源

#### checkPermissionConflicts() - 权限冲突检查
```java
String conflicts = PermissionValidator.checkPermissionConflicts(permissionsJson);
```

**检查项**:
- 不能有删除权而无编辑权
- 不能有编辑权而无查看权

---

### 2. 权限审计日志

**表**: `permission_audit_log`

记录所有权限相关操作：

| 字段 | 说明 | 例值 |
|------|------|------|
| operation_type | 操作类型 | PERMISSION_UPDATE, ROLE_UPDATE, MEMBER_REMOVAL |
| operator_id | 操作者ID | 管理员ID |
| target_user_id | 目标用户ID | 被修改权限的用户ID |
| old_value | 旧值 | 旧权限JSON或旧角色 |
| new_value | 新值 | 新权限JSON或新角色 |
| status | 操作状态 | 1成功、0失败 |
| create_time | 操作时间 | 2026-01-13 10:30:45 |

**查询审计日志接口**:

```
GET /api/family-members/audit-log/family
获取家庭组的所有权限变更历史（管理员可见）

GET /api/family-members/{memberId}/audit-log
获取特定成员的权限变更历史（管理员可见）
```

---

### 3. 权限冲突检查

**场景**: 管理员编辑成员权限时

**检查规则**:
1. ✓ 不能在没有查看权的情况下拥有编辑权
2. ✓ 不能在没有查看权的情况下拥有创建权
3. ✓ 不能在没有编辑权的情况下拥有删除权

**检查接口**:

```
POST /api/family-members/check-permission-conflicts

请求体:
{
  "permissions": "{\"income\": {\"view\": true, \"create\": true, \"edit\": true, \"delete\": false}, ...}"
}

响应:
{
  "code": 200,
  "hasConflicts": true,
  "conflicts": [
    "expense模块：不能在没有编辑权的情况下拥有删除权"
  ]
}
```

---

### 4. 批量权限更新

**接口**:

```
POST /api/family-members/batch/permissions

请求体:
{
  "memberIds": [1, 2, 3],
  "permissionTemplateName": "记账员"
}

响应:
{
  "code": 200,
  "message": "已批量更新 3 个成员的权限",
  "updatedCount": 3
}
```

**功能**:
- 批量应用权限模板给多个成员
- 自动记录审计日志
- 需要管理员权限

---

### 5. 权限详情查询

**接口**:

```
GET /api/family-members/{memberId}/permission-details

响应:
{
  "code": 200,
  "data": {
    "memberId": 1,
    "userId": 1,
    "role": "MEMBER",
    "joinTime": "2026-01-11T10:30:45",
    "permissions": {
      "income": { "view": true, "create": true, "edit": true, "delete": false },
      "expense": { "view": true, "create": true, "edit": true, "delete": false },
      ...
    }
  }
}
```

**功能**:
- 获取成员权限的详细信息
- 权限JSON自动解析为可读格式
- 便于前端展示权限编辑界面

---

### 6. 权限初始化配置

**类**: `config/PermissionTemplateInitializer`

**功能**:
- 应用启动时自动初始化3个权限模板
- 检查模板是否已存在，避免重复创建
- 详细的初始化日志

**初始化流程**:
```
应用启动
  ↓
CommandLineRunner执行
  ↓
检查"查看者"模板是否存在 → 不存在则创建
检查"记账员"模板是否存在 → 不存在则创建
检查"管理员"模板是否存在 → 不存在则创建
  ↓
权限模板初始化完成
```

---

## 🚀 API 端点总结

### 权限模板相关

| 方法 | 端点 | 说明 | 权限要求 |
|------|------|------|---------|
| GET | /api/family-members/permission-templates | 获取所有权限模板 | 用户认证 |

### 成员权限管理

| 方法 | 端点 | 说明 | 权限要求 |
|------|------|------|---------|
| GET | /api/family-members/{memberId}/permission-details | 获取成员权限详情 | 用户认证 |
| PUT | /api/family-members/permissions | 更新单个成员权限 | 管理员 |
| POST | /api/family-members/batch/permissions | 批量更新成员权限 | 管理员 |
| POST | /api/family-members/check-permission-conflicts | 检查权限冲突 | 用户认证 |

### 权限审计日志

| 方法 | 端点 | 说明 | 权限要求 |
|------|------|------|---------|
| GET | /api/family-members/audit-log/family | 查看家庭组权限变更历史 | 管理员 |
| GET | /api/family-members/{memberId}/audit-log | 查看成员权限变更历史 | 管理员 |

---

## 🔄 权限检查流程（完整示例）

### 示例1: 用户编辑支出

```
用户请求: PUT /api/expense/{expenseId}
  ↓
@FamilyPermission("expense_edit")切面检查
  ↓
1. 获取当前用户ID (userId=123)
2. 查询family_member，获取权限JSON
3. 检查权限 "expense_edit" → 存在且为true → 通过
  ↓
业务逻辑处理
  ↓
1. 查询支出对象 (creatorId=123, userId=123)
2. 调用PermissionValidator.validateEditPermission()
3. 检查：creatorId==userId → YES → 允许编辑
  ↓
更新支出对象
  ↓
返回成功
```

### 示例2: 管理员删除用户支出

```
管理员请求: DELETE /api/expense/{expenseId}
  ↓
@FamilyPermission("expense_delete")切面检查
  ↓
1. 获取当前用户ID (userId=1，管理员)
2. 查询family_member，获取权限JSON
3. 检查权限 "expense_delete" → 存在且为true → 通过
  ↓
业务逻辑处理
  ↓
1. 查询支出对象 (creatorId=123)
2. 调用PermissionValidator.validateDeletePermission()
3. 检查：role=="ADMIN" → YES → 允许删除
  ↓
删除支出对象，记录审计日志
  ↓
返回成功
```

### 示例3: 更新成员权限

```
管理员请求: PUT /api/family-members/permissions
  ↓
在controller中:
1. 获取当前用户ID (userId=1)
2. 检查isAdmin(userId) → true → 继续
3. 调用FamilyMemberService.updateMemberPermissions()
  ↓
在service中:
1. 检查新权限JSON是否有冲突
2. 更新family_member.permissions
3. 记录审计日志（通过PermissionAuditLogService）
  ↓
返回成功，包含审计日志ID
```

---

## 📝 最佳实践

### 1. 权限检查位置

| 检查位置 | 用途 | 例子 |
|---------|------|------|
| Controller | 验证用户基本权限 | @FamilyPermission注解 |
| Business Logic | 验证操作对象的权限 | PermissionValidator.validateEditPermission() |
| Database Query | 通过family_group_id隔离数据 | WHERE family_group_id = ? |

### 2. 权限日志记录

```java
// 操作成功时记录
permissionAuditLogService.logPermissionUpdate(
    familyGroupId,
    operatorId,
    memberId,
    targetUserId,
    oldPermissions,
    newPermissions
);

// 操作失败时记录
permissionAuditLogService.logPermissionCheckFailure(
    familyGroupId,
    userId,
    "expense_delete",
    "用户权限不足"
);
```

### 3. 权限冲突避免

```java
// 保存权限前检查冲突
List<String> conflicts = familyMemberService.checkPermissionConflicts(permissionsJson);
if (!conflicts.isEmpty()) {
    throw new BusinessException("权限配置有冲突: " + String.join(", ", conflicts));
}
```

### 4. 批量操作的权限验证

```java
// 验证批量操作权限
PermissionValidator.validateBatchOperation(
    userId,
    resourceOwnerIds,
    familyMember,
    "delete"
);
```

---

## 🧪 测试场景

### 场景1: 查看者权限测试

```
用户登录为"查看者"角色
  ↓
✓ 能查看账目列表
✗ 不能创建新账目
✗ 不能编辑账目
✗ 不能删除账目
✓ 能查看成员列表
✗ 不能编辑成员权限
```

### 场景2: 记账员权限测试

```
用户登录为"记账员"角色
  ↓
✓ 能创建支出账目
✓ 能编辑自己的支出账目
✗ 不能删除支出账目
✓ 能创建支出分类
✗ 不能删除分类
✓ 能查看成员列表
✗ 不能编辑成员权限
```

### 场景3: 权限冲突检查

```
尝试设置权限：
{
  "expense": { "view": false, "create": false, "edit": false, "delete": true }
}
  ↓
检查结果：
冲突 → "不能在没有编辑权的情况下拥有删除权"
  ↓
拒绝保存
```

---

## 📊 权限变更审计示例

```
操作时间          操作者     目标用户    操作类型          详情
2026-01-13 10:30  管理员(1)  李四(2)    PERMISSION_UPDATE  权限已更新为"记账员"
2026-01-13 10:31  管理员(1)  王五(3)    ROLE_UPDATE        角色已更新：MEMBER → ADMIN
2026-01-13 10:32  李四(2)    -          PERMISSION_DENIED   无权删除支出(ID:123)
```

---

## 🔒 安全建议

1. **定期审计日志**: 定期检查权限变更日志，监控异常操作
2. **权限模板不可删除**: 系统内置的3个权限模板不应该删除
3. **管理员转移**: 转移管理员身份时需要特殊流程
4. **权限最小化**: 为新用户分配最少必需的权限（最小权限原则）
5. **日志保留**: 保留至少3个月的审计日志用于审计

---

## 📌 常见问题

**Q: 如何添加自定义权限？**
A: 目前系统支持3个标准权限模板。如需自定义权限，需要修改权限JSON结构并更新PermissionValidator验证逻辑。

**Q: 能否转移管理员身份？**
A: 可以通过updateMemberRole()将另一个成员设置为ADMIN，但不能移除自己的管理员身份。

**Q: 删除成员会发生什么？**
A: 删除成员会将family_member.status设置为0，保留审计日志，但不真正删除数据。

**Q: 权限变更是否立即生效？**
A: 是的。权限变更立即保存到数据库，下次用户请求时即可生效。

---

## 版本更新历史

| 版本 | 日期 | 主要改进 |
|------|------|---------|
| 1.0 | 2026-01-11 | 初始权限体系设计 |
| 2.0 | 2026-01-13 | 完善权限管理功能，添加权限验证工具、审计日志、批量操作、权限冲突检查 |
