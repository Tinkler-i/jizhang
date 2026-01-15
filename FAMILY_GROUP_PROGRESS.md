# 家庭组管理模块 - 开发进度报告

**更新时间**: 2026年1月11日 21:00  
**完成度**: 40% (第1-6阶段完成)

---

## ✅ 已完成的工作

### 第1阶段：数据库设计和迁移脚本 ✅
**文件**: `src/main/resources/sql/V3_0__add_family_group.sql`
- ✅ 创建 `family_group` 表（家庭组表）
- ✅ 创建 `family_member` 表（家庭成员表）
- ✅ 创建 `permission_template` 表（权限模板表）
- ✅ 为所有现有表添加 `family_group_id` 字段
- ✅ 初始化3个权限模板（查看者、记账员、管理员）

### 第2阶段：Entity 类创建 ✅
**文件**: `src/main/java/com/billmanager/jizhang/entity/`
- ✅ `FamilyGroup.java` - 家庭组实体
- ✅ `FamilyMember.java` - 家庭成员实体
- ✅ `PermissionTemplate.java` - 权限模板实体
- ✅ 修改 `Expense.java` - 添加 familyGroupId 字段
- ✅ 修改 `Income.java` - 添加 familyGroupId 字段
- ✅ 修改 `Budget.java` - 添加 familyGroupId 字段
- ✅ 修改 `ExpenseCategory.java` - 添加 familyGroupId 字段
- ✅ 修改 `IncomeCategory.java` - 添加 familyGroupId 字段

### 第3阶段：Mapper 层创建 ✅
**文件**: `src/main/java/com/billmanager/jizhang/mapper/`
- ✅ `FamilyGroupMapper.java` - 包含 insert, selectById, selectByCode, selectByCreatorId, update, updateStatus
- ✅ `FamilyMemberMapper.java` - 包含 insert, selectById, selectByUserId, selectByFamilyGroupId, updatePermissions, updateRole, deleteById, countByFamilyGroupId
- ✅ `PermissionTemplateMapper.java` - 包含 insert, selectById, selectByName, selectAll, selectBuiltInTemplates, update, deleteById

### 第4阶段：业务服务层创建 ✅

#### 权限服务（PermissionService）
**文件**: 
- `src/main/java/com/billmanager/jizhang/service/PermissionService.java` - 接口
- `src/main/java/com/billmanager/jizhang/service/impl/PermissionServiceImpl.java` - 实现

**实现的方法**:
- `getAllTemplates()` - 获取所有权限模板
- `getTemplateByName(String name)` - 按名称查询权限模板
- `getTemplateById(Long id)` - 按ID查询权限模板
- `hasPermission(Long userId, String permission)` - 检查用户权限
- `hasPermission(FamilyMember member, String permission)` - 检查成员权限
- `isAdmin(Long userId)` - 检查是否是管理员
- `checkPermissionFromJson(String json, String permission)` - 从JSON检查权限

#### 家庭组服务（FamilyGroupService）
**文件**:
- `src/main/java/com/billmanager/jizhang/service/FamilyGroupService.java` - 接口
- `src/main/java/com/billmanager/jizhang/service/impl/FamilyGroupServiceImpl.java` - 实现

**实现的方法**:
- `createFamilyGroup(Long userId, String familyName)` - 创建家庭组（注册时调用）
- `getFamilyGroupById(Long id)` - 按ID查询
- `getFamilyGroupByCode(String code)` - 按编号查询
- `getFamilyGroupByCreatorId(Long creatorId)` - 按创建者查询
- `getFamilyGroupByUserId(Long userId)` - 按用户ID查询所属家庭组
- `updateFamilyGroup(FamilyGroup group)` - 更新家庭组信息
- `generateFamilyGroupCode()` - 生成唯一的6位编号

#### 家庭成员服务（FamilyMemberService）
**文件**:
- `src/main/java/com/billmanager/jizhang/service/FamilyMemberService.java` - 接口
- `src/main/java/com/billmanager/jizhang/service/impl/FamilyMemberServiceImpl.java` - 实现

**实现的方法**:
- `joinFamilyGroup(Long userId, String code)` - 通过编号加入家庭组
- `createFamilyMember(...)` - 创建家庭成员（注册时调用）
- `getFamilyMemberById(Long id)` - 按ID查询
- `getFamilyMemberByUserId(Long userId)` - 按用户ID查询
- `getFamilyMembersByGroupId(Long groupId)` - 查询家庭组所有成员
- `countFamilyMembers(Long groupId)` - 统计成员数量
- `updateMemberPermissions(...)` - 更新权限
- `updateMemberPermissionsByTemplate(...)` - 使用模板更新权限
- `updateMemberRole(...)` - 更新角色
- `removeMember(Long memberId)` - 移除成员
- `isUserInAnyFamilyGroup(Long userId)` - 检查用户是否在任何家庭组中

---

## ⏳ 待完成的工作

### 第7阶段：改造用户注册流程 ⏳
**目标文件**: `src/main/java/com/billmanager/jizhang/service/impl/VerificationCodeServiceImpl.java`

**需要修改**:
- 在 `registerWithVerificationCode()` 方法中添加：
  1. 用户创建后，自动创建 FamilyGroup（调用 familyGroupService.createFamilyGroup）
  2. 自动创建 FamilyMember（role=ADMIN，使用Admin权限模板）
  3. 必要的事务处理和异常处理

**伪代码**:
```java
@Transactional
public void registerWithVerificationCode(RegisterRequest request) {
    // 现有逻辑：验证、创建User...
    User newUser = createUser(request);
    
    // 【新增】自动创建家庭组
    PermissionTemplate adminTemplate = permissionService.getTemplateByName("管理员");
    FamilyGroup familyGroup = familyGroupService.createFamilyGroup(newUser.getId(), "我的家庭");
    
    // 【新增】自动创建家庭成员（管理员）
    familyMemberService.createFamilyMember(
        familyGroup.getId(), 
        newUser.getId(), 
        "ADMIN", 
        adminTemplate
    );
}
```

### 第8阶段：创建权限检查AOP切面 ⏳
**目标文件**: `src/main/java/com/billmanager/jizhang/config/` 和 `src/main/java/com/billmanager/jizhang/annotation/`

**需要创建**:
1. `@FamilyPermission` 注解 - 用于标注需要权限检查的方法
2. `FamilyPermissionAspect` - AOP切面类

**实现思路**:
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FamilyPermission {
    String value();  // 权限标识：expense_create, income_view 等
}

@Aspect
@Component
public class FamilyPermissionAspect {
    @Before("@annotation(permission)")
    public void checkPermission(JoinPoint jp, FamilyPermission permission) {
        Long userId = getCurrentUserId();  // 从认证信息获取
        if (!permissionService.hasPermission(userId, permission.value())) {
            throw new PermissionDeniedException("您没有此权限");
        }
    }
}
```

### 第9阶段：修改现有查询接口 ⏳
**目标文件**: 
- `src/main/java/com/billmanager/jizhang/service/impl/ExpenseServiceImpl.java`
- `src/main/java/com/billmanager/jizhang/service/impl/IncomeServiceImpl.java`
- `src/main/java/com/billmanager/jizhang/service/impl/BudgetServiceImpl.java`
- `src/main/java/com/billmanager/jizhang/service/impl/ExpenseCategoryServiceImpl.java`
- `src/main/java/com/billmanager/jizhang/service/impl/IncomeCategoryServiceImpl.java`

**修改模式**（以ExpenseService为例）:
```java
// 旧模式
public List<Expense> getExpenseList(Long userId) {
    return expenseMapper.selectByUserId(userId);
}

// 新模式
@FamilyPermission("expense_view")
public List<Expense> getExpenseList() {
    Long userId = getCurrentUserId();
    Long familyGroupId = familyGroupService.getFamilyGroupByUserId(userId).getId();
    return expenseMapper.selectByFamilyGroupId(familyGroupId);
}
```

**Mapper改造**:
需要为所有Mapper添加 `selectByFamilyGroupId(Long id)` 方法

### 第10阶段：创建FamilyGroupController ⏳
**目标文件**: `src/main/java/com/billmanager/jizhang/controller/FamilyGroupController.java`

**API端点**:
```
POST   /api/family/info           创建家庭组（注册时自动调用）
GET    /api/family/info           获取当前用户的家庭组信息
PUT    /api/family/info           编辑家庭组信息（仅ADMIN）
GET    /api/family/code           获取家庭组编号（仅ADMIN）
```

### 第11阶段：创建FamilyMemberController ⏳
**目标文件**: `src/main/java/com/billmanager/jizhang/controller/FamilyMemberController.java`

**API端点**:
```
POST   /api/family/join           通过编号加入家庭组
GET    /api/family/members        获取家庭组成员列表
PUT    /api/family/members/{id}   更新成员权限（仅ADMIN）
DELETE /api/family/members/{id}   移除成员（仅ADMIN）
```

### 第12-15阶段：前端页面实现 ⏳
- FamilyManagement.vue（3个标签页）
- JoinFamily.vue
- 改造Dashboard.vue
- 改造UserProfile.vue

### 第16阶段：测试和调试 ⏳
- 端到端集成测试
- 权限验证测试
- 性能测试

---

## 📊 关键代码位置一览

| 组件 | 路径 | 状态 |
|------|------|------|
| 数据库迁移 | `sql/V3_0__add_family_group.sql` | ✅ |
| Entity | `entity/FamilyGroup.java` 等 | ✅ |
| Mapper | `mapper/FamilyGroupMapper.java` 等 | ✅ |
| PermissionService | `service/PermissionService.java` | ✅ |
| FamilyGroupService | `service/FamilyGroupService.java` | ✅ |
| FamilyMemberService | `service/FamilyMemberService.java` | ✅ |
| 注册流程改造 | `service/impl/VerificationCodeServiceImpl.java` | ⏳ |
| 权限AOP | `config/FamilyPermissionAspect.java` | ⏳ |
| 控制器 | `controller/FamilyGroupController.java` | ⏳ |
| 前端页面 | `views/FamilyManagement.vue` | ⏳ |

---

## 🚀 下一步计划

**建议优先级**:
1. **立即执行** - 第7阶段：改造用户注册流程（最关键）
2. **随后执行** - 第8阶段：创建权限检查AOP
3. **并行执行** - 第9阶段：修改现有查询接口 + 第10-11阶段：创建Controllers
4. **最后执行** - 第12-15阶段：前端页面实现

---

## 💡 重要提醒

1. **SQL脚本执行**: 在运行Java代码前，务必在MySQL中执行 `V3_0__add_family_group.sql` 脚本
2. **事务处理**: 注册流程的修改需要使用 `@Transactional` 保证原子性
3. **权限JSON格式**: 权限检查依赖JSON格式的准确性，务必确保权限模板中的JSON结构正确
4. **缓存考虑**: 后期可考虑缓存用户的家庭组ID和权限，以优化性能

---

**预计完成时间**: 2026年1月15日  
**工作量估计**: 还需约20小时
