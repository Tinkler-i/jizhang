# 家庭组权限管理系统修复说明

## 修复概述

本次修复主要解决了家庭组权限管理系统的以下核心问题：

1. **权限检查只验证操作类型但不限制数据范围**
2. **家庭成员能看到所有家庭组数据，无法按权限过滤**
3. **权限切换逻辑混乱**

## 修改的文件

### 新增文件
- `src/main/java/com/billmanager/jizhang/constant/PermissionConstants.java` - 权限常量类
- `src/main/java/com/billmanager/jizhang/exception/FamilyPermissionException.java` - 权限异常类

### 修改文件
- `src/main/java/com/billmanager/jizhang/service/PermissionService.java` - 新增数据访问权限检查方法
- `src/main/java/com/billmanager/jizhang/service/impl/PermissionServiceImpl.java` - 实现数据访问权限检查
- `src/main/java/com/billmanager/jizhang/service/impl/IncomeServiceImpl.java` - 添加权限过滤逻辑
- `src/main/java/com/billmanager/jizhang/service/impl/ExpenseServiceImpl.java` - 添加权限过滤逻辑
- `src/main/java/com/billmanager/jizhang/controller/IncomeController.java` - 移除冗余的权限注解
- `src/main/java/com/billmanager/jizhang/controller/ExpenseController.java` - 移除冗余的权限注解
- `src/main/java/com/billmanager/jizhang/aspect/FamilyPermissionAspect.java` - 改用新的权限异常
- `src/main/java/com/billmanager/jizhang/exception/GlobalExceptionHandler.java` - 添加权限异常处理

## 权限控制逻辑

### 1. 操作权限（Operation Permission）
检查用户是否有执行某种操作的权限，如：`income_view`、`expense_create` 等。

- **管理员（ADMIN）**：拥有所有操作权限
- **普通成员（MEMBER）**：按 `permissions` JSON 字段配置的权限

### 2. 数据访问权限（Data Access Permission）

#### 查看数据（View）
```java
// 检查用户是否能访问特定用户的数据
boolean canAccessUserData(Long currentUserId, Long targetUserId, String module)
```
- 自己的数据：总是可以访问
- 同家庭组其他成员的数据：需要有对应模块的 `view` 权限

#### 编辑数据（Edit）
```java
// 检查用户是否能编辑特定用户的数据
boolean canEditUserData(Long currentUserId, Long targetUserId, String module)
```
- 自己的数据：需要有 `edit` 权限
- 其他成员的数据：需要是管理员

#### 删除数据（Delete）
```java
// 检查用户是否能删除特定用户的数据
boolean canDeleteUserData(Long currentUserId, Long targetUserId, String module)
```
- 自己的数据：需要有 `delete` 权限
- 其他成员的数据：需要是管理员

### 3. 数据范围控制

```java
// 获取用户可访问的用户ID列表
Set<Long> getAccessibleUserIds(Long userId, String module)
```
- 有 `view` 权限：返回家庭组所有成员的ID
- 无 `view` 权限：只返回自己的ID
- 不在家庭组：只返回自己的ID

## 权限JSON结构

```json
{
  "income": {
    "view": true,
    "create": true,
    "edit": true,
    "delete": false
  },
  "expense": {
    "view": true,
    "create": true,
    "edit": false,
    "delete": false
  },
  "budget": {
    "view": true,
    "create": false,
    "edit": false,
    "delete": false
  },
  "category": {
    "view": true,
    "create": true,
    "edit": true,
    "delete": false
  },
  "member_management": {
    "view": true,
    "invite": false,
    "edit_permission": false,
    "remove_member": false
  }
}
```

## 使用示例

### Service层权限检查

```java
// 1. 检查操作权限并抛出异常
permissionService.checkPermissionOrThrow(userId, PermissionConstants.INCOME_CREATE);

// 2. 检查是否能编辑特定数据
if (!permissionService.canEditUserData(userId, income.getUserId(), PermissionConstants.MODULE_INCOME)) {
    throw FamilyPermissionException.dataAccessDenied(userId, "income", incomeId);
}

// 3. 获取用户可访问的数据范围
Set<Long> accessibleUserIds = permissionService.getAccessibleUserIds(userId, PermissionConstants.MODULE_INCOME);
// 然后用这个集合过滤查询结果
```

### Controller层

Controller层不再需要 `@FamilyPermission` 注解，权限检查已移至Service层。Controller只负责获取当前用户并调用Service方法。

## 异常处理

新增的 `FamilyPermissionException` 会被 `GlobalExceptionHandler` 捕获，返回 HTTP 403 状态码和错误信息：

```json
{
  "code": 403,
  "message": "用户 1 无权访问 income 资源 (ID: 5)",
  "data": null,
  "success": false
}
```

## 日志输出

权限检查会输出详细的日志，方便调试：

```
【权限】用户 2 有 income_view 权限，可访问家庭组 1 的所有成员数据: [1, 2, 3]
【收入】用户 2 查询结果: 总数 10, 过滤后 10
【权限】用户 2 编辑用户 1 的 income 数据: isAdmin=false
【支出】用户 2 无权编辑用户 1 的支出记录 5
```

## 待完善的部分

1. **BudgetService** - 需要添加类似的权限过滤逻辑
2. **CategoryService** - 需要添加类似的权限过滤逻辑
3. **前端适配** - 前端需要根据权限控制UI元素的显示/隐藏

## 测试建议

1. 创建两个用户，一个是管理员，一个是普通成员
2. 给普通成员配置部分权限（如只有 `income_view`，没有 `income_edit`）
3. 验证：
   - 普通成员只能看到自己的数据（如果没有view权限）
   - 普通成员不能编辑他人的数据（即使有edit权限）
   - 管理员可以看到和编辑所有数据
