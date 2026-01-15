# 家庭组管理模块 - 完整实现规划

**制定日期**: 2026年1月11日  
**版本**: 1.0  
**状态**: 正式开始开发

---

## 📋 核心功能需求

### 用户需求
- ✅ 每个用户注册后自动创建一个家庭组
- ✅ 家庭组管理员可查看家庭组编号
- ✅ 其他用户通过编号加入家庭组
- ✅ 管理员可管理成员权限和移除成员
- ✅ 所有数据按家庭组隔离查询

### 架构决策
| 项目 | 决策 |
|------|------|
| 用户-家庭组关系 | 1:1 （一个用户只属于一个家庭组） |
| 查询维度 | 按 `family_group_id` 查询（无user_id条件） |
| 权限验证 | 查询前先权限检查（内存操作，避免冗余DB查询） |
| 权限粒度 | 编辑权/删除权分离 |
| 权限模板 | 预置3个：查看者、记账员、管理员 |
| 家庭组编号 | 6位固定编号，不定期更换，支持复制 |
| 邀请机制 | 直接分享编号，无邀请链接 |

---

## 🗄️ 数据库设计

### 新增表

```sql
-- 家庭组表
CREATE TABLE family_group (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭组ID',
    code VARCHAR(20) UNIQUE NOT NULL COMMENT '家庭组编号（6位，如：FA8K3M）',
    name VARCHAR(100) NOT NULL COMMENT '家庭组名称',
    description TEXT COMMENT '家庭介绍',
    creator_id BIGINT NOT NULL COMMENT '创建者ID',
    status TINYINT DEFAULT 1 COMMENT '1正常、0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_creator (creator_id) COMMENT '一个用户只能创建一个家庭组',
    KEY idx_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭组表';

-- 家庭成员表
CREATE TABLE family_member (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '家庭成员ID',
    family_group_id BIGINT NOT NULL COMMENT '家庭组ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID（唯一约束：一个用户只在一个家庭组）',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT 'ADMIN管理员、MEMBER普通成员',
    permissions JSON COMMENT '权限配置JSON',
    join_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    status TINYINT DEFAULT 1 COMMENT '1正常、0禁用',
    PRIMARY KEY (id),
    FOREIGN KEY (family_group_id) REFERENCES family_group(id),
    FOREIGN KEY (user_id) REFERENCES user(id),
    KEY idx_family (family_group_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='家庭成员表';

-- 权限模板表
CREATE TABLE permission_template (
    id BIGINT NOT NULL AUTO_INCREMENT COMMENT '模板ID',
    name VARCHAR(50) NOT NULL UNIQUE COMMENT '模板名称（如：查看者、记账员、管理员）',
    description VARCHAR(200) COMMENT '模板描述',
    permissions JSON NOT NULL COMMENT '权限配置JSON',
    built_in TINYINT DEFAULT 1 COMMENT '1系统内置、0自定义',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限模板表';
```

### 修改现有表

```sql
-- 为各表添加family_group_id字段
ALTER TABLE income ADD COLUMN family_group_id BIGINT NOT NULL AFTER user_id, ADD KEY idx_family_group (family_group_id);
ALTER TABLE expense ADD COLUMN family_group_id BIGINT NOT NULL AFTER user_id, ADD KEY idx_family_group (family_group_id);
ALTER TABLE budget ADD COLUMN family_group_id BIGINT NOT NULL AFTER user_id, ADD KEY idx_family_group (family_group_id);
ALTER TABLE income_category ADD COLUMN family_group_id BIGINT NOT NULL AFTER user_id, ADD KEY idx_family_group (family_group_id);
ALTER TABLE expense_category ADD COLUMN family_group_id BIGINT NOT NULL AFTER user_id, ADD KEY idx_family_group (family_group_id);

-- 重建索引（去掉user_id单字段索引）
ALTER TABLE income DROP KEY idx_user_id;
ALTER TABLE expense DROP KEY idx_user_id;
ALTER TABLE budget DROP KEY idx_user_id;
ALTER TABLE income_category DROP KEY idx_user_id;
ALTER TABLE expense_category DROP KEY idx_user_id;
```

---

## 🔐 权限体系

### 3个标准权限模板

#### 1. 查看者（Viewer）
```json
{
  "permissions": {
    "income": { "view": true, "create": false, "edit": false, "delete": false },
    "expense": { "view": true, "create": false, "edit": false, "delete": false },
    "budget": { "view": true, "create": false, "edit": false, "delete": false },
    "category": { "view": true, "create": false, "edit": false, "delete": false },
    "member_management": { "view": false, "invite": false, "edit_permission": false, "remove_member": false }
  }
}
```

#### 2. 记账员（Recorder）
```json
{
  "permissions": {
    "income": { "view": true, "create": true, "edit": true, "delete": false },
    "expense": { "view": true, "create": true, "edit": true, "delete": false },
    "budget": { "view": true, "create": false, "edit": false, "delete": false },
    "category": { "view": true, "create": true, "edit": true, "delete": false },
    "member_management": { "view": true, "invite": false, "edit_permission": false, "remove_member": false }
  }
}
```

#### 3. 管理员（Admin）
```json
{
  "permissions": {
    "income": { "view": true, "create": true, "edit": true, "delete": true },
    "expense": { "view": true, "create": true, "edit": true, "delete": true },
    "budget": { "view": true, "create": true, "edit": true, "delete": true },
    "category": { "view": true, "create": true, "edit": true, "delete": true },
    "member_management": { "view": true, "invite": true, "edit_permission": true, "remove_member": true }
  }
}
```

### 权限验证流程（AOP切面）
```
请求到达
  ↓
@FamilyPermission("expense_create") 注解检查
  ↓
获取当前用户ID
  ↓
获取用户的family_group_id（缓存或单次查询）
  ↓
从family_member表获取用户权限
  ↓
检查是否有此权限
  ↓
✅ 有权限 → 继续执行业务逻辑 → 按family_group_id查询数据
❌ 无权限 → 返回403 Forbidden
```

---

## 📱 API 设计

### 家庭组管理接口（FamilyGroupController）

```
【创建和查询】
POST   /api/family/create           创建家庭组（注册时自动调用，无需前端调用）
GET    /api/family/info             获取当前用户的家庭组信息
PUT    /api/family/info             编辑家庭组信息（仅ADMIN）

【编号查询】
GET    /api/family/code             获取家庭组编号（仅ADMIN可见）
```

### 成员管理接口（FamilyMemberController）

```
【成员管理】
POST   /api/family/join                     通过编号加入家庭组
GET    /api/family/members                  获取家庭组成员列表
PUT    /api/family/members/{memberId}       更新成员权限（仅ADMIN）
DELETE /api/family/members/{memberId}       移除成员（仅ADMIN）

【权限管理】
GET    /api/family/members/{memberId}/permissions      获取成员权限详情
POST   /api/family/members/{memberId}/permissions      批量更新成员权限
```

### 权限模板接口（PermissionTemplateController）

```
【权限模板】
GET    /api/permission-templates            获取所有权限模板列表
GET    /api/permission-templates/{id}       获取单个权限模板详情
```

---

## 📊 业务流程

### 流程1：用户注册（自动创建家庭组）

```
用户填写邮箱/密码并验证码
  ↓
VerificationCodeService.registerWithVerificationCode()
  ↓
【新增逻辑】创建User记录后，自动：
  ├─ 生成family_group_code（6位字母数字）
  ├─ 创建FamilyGroup记录（creator_id = 新用户ID，name默认为"我的家庭"）
  ├─ 创建FamilyMember记录（role = ADMIN，permissions = 从Admin模板复制）
  └─ 返回成功
```

### 流程2：用户加入家庭组

```
用户在JoinFamily页面输入编号（如：FA8K3M）
  ↓
调用 POST /api/family/join { "code": "FA8K3M" }
  ↓
后端验证：
  ├─ 编号是否存在
  ├─ 家庭组是否正常（status=1）
  ├─ 用户是否已属于其他家庭组（检查family_member表）
  └─ 都通过则创建FamilyMember记录（role=MEMBER，permissions=从Recorder模板复制）
  ↓
返回加入成功 或 相应错误提示
```

### 流程3：查询数据

```
用户请求 GET /api/expense/list
  ↓
@FamilyPermission("expense_view") 权限检查
  ↓
检查用户是否有expense_view权限
  ↓
✅ 有权限
  ├─ 获取用户family_group_id
  └─ SELECT * FROM expense WHERE family_group_id = ?
  ↓
❌ 无权限 → 返回403
```

---

## 🎨 前端页面设计

### 新增页面1：FamilyManagement.vue（仅ADMIN可访问）

**三个标签页：**

#### 标签页1：家庭信息
```
【家庭信息】
┌─────────────────────────┐
│ 家庭组名称：王家         │
│ 创建时间：2026-01-11    │
│ 成员数：3人              │
│ ┌─────────────────────┐ │
│ │ 🔑 家庭组编号        │ │
│ │ ┌─────────────────┐ │ │
│ │ │   FA8K3M       │ │ │ ← 大字显示，可复制
│ │ └─────────────────┘ │ │
│ │ 分享此编号给其他人，    │ │
│ │ 让他们加入你的家庭  │ │
│ │ ┌──────────────────┐ │
│ │ │ [📋 复制编号]    │ │
│ │ └──────────────────┘ │
│ └─────────────────────┘ │
│ ┌─────────────────────┐ │
│ │ [✏️ 编辑信息]       │ │
│ └─────────────────────┘ │
└─────────────────────────┘
```

#### 标签页2：成员列表
```
【成员管理】
┌──────────────────────────────────┐
│ 成员总数：3                        │
├──────────────────────────────────┤
│ 👤 张三                           │
│   角色：管理员 | 加入：2026-01-11 │
│   [编辑权限] [移除]               │
├──────────────────────────────────┤
│ 👤 李四                           │
│   角色：记账员 | 加入：2026-01-12 │
│   [编辑权限] [移除]               │
├──────────────────────────────────┤
│ 👤 王五                           │
│   角色：查看者 | 加入：2026-01-13 │
│   [编辑权限] [移除]               │
└──────────────────────────────────┘

【权限编辑弹窗】
┌──────────────────────────────────┐
│ 编辑权限：李四                     │
├──────────────────────────────────┤
│ 📊 收入                           │
│   ☑ 查看  ☑ 新增  ☑ 编辑  ☐ 删除 │
│                                  │
│ 💰 支出                           │
│   ☑ 查看  ☑ 新增  ☑ 编辑  ☐ 删除 │
│                                  │
│ 📈 预算                           │
│   ☑ 查看  ☐ 新增  ☐ 编辑  ☐ 删除 │
│                                  │
│ 🏷️  分类                          │
│   ☑ 查看  ☑ 新增  ☑ 编辑  ☐ 删除 │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ [使用模板] ▼ (Recorder)      │ │
│ └──────────────────────────────┘ │
│ [保存] [取消]                     │
└──────────────────────────────────┘
```

#### 标签页3：邀请成员
```
【邀请成员】
┌──────────────────────────┐
│ 邀请成员加入你的家庭      │
├──────────────────────────┤
│ 1. 复制编号：FA8K3M      │
│    [📋 复制]              │
│                          │
│ 2. 分享给其他人          │
│                          │
│ 3. 其他人输入编号后自动加入│
└──────────────────────────┘
```

---

### 新增页面2：JoinFamily.vue

```
【加入家庭组】
┌──────────────────────────────────┐
│ 🏠 加入家庭                        │
├──────────────────────────────────┤
│ 请输入家庭组编号：                 │
│ ┌──────────────────────────────┐ │
│ │ [输入6位编号，如：FA8K3M]     │ │
│ └──────────────────────────────┘ │
│                                  │
│ ┌──────────────────────────────┐ │
│ │ [🚀 加入]                    │ │
│ └──────────────────────────────┘ │
│                                  │
│ 💡 提示：如果你已有账户，请让家庭 │
│    管理员分享编号给你               │
└──────────────────────────────────┘
```

---

### 修改现有页面：Dashboard.vue

```
【首页 - 添加家庭组信息卡片】
┌─────────────────────────────────────┐
│ 🏠 家庭组：王家                      │
│ 成员：张三（管理员）, 李四, 王五    │
│ [👨‍💼 管理家庭]（仅ADMIN显示）      │
└─────────────────────────────────────┘

... 其他内容保持不变 ...
```

---

### 修改现有页面：UserProfile.vue

```
【个人资料 - 添加家庭组信息】
┌─────────────────────────────────┐
│ 用户名：张三                      │
│ 邮箱：zhangsan@example.com       │
│ 手机：13800000000                │
│ ────────────────────────────────│
│ 家庭组：王家 (管理员)              │
│ 编号：FA8K3M（仅ADMIN可见）        │
│ ────────────────────────────────│
│ [🔐 修改密码]                     │
└─────────────────────────────────┘
```

---

## 🔧 技术实现细节

### 1. 家庭组编号生成
```java
// 生成6位编号（字母+数字，避免歧义字母如I、O、L等）
String code = UUID.randomUUID()
    .toString()
    .replace("-", "")
    .substring(0, 6)
    .toUpperCase()
    .replaceAll("[IO]", String.valueOf(new Random().nextInt(10)));
```

### 2. 权限检查AOP
```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FamilyPermission {
    String value();  // 如："expense_create", "member_edit"
}

@Aspect
public class FamilyPermissionAspect {
    @Before("@annotation(permission)")
    public void checkPermission(JoinPoint jp, FamilyPermission permission) {
        Long userId = getCurrentUserId();
        FamilyMember member = familyMemberService.getMemberByUserId(userId);
        if (!member.hasPermission(permission.value())) {
            throw new PermissionDeniedException();
        }
    }
}
```

### 3. 数据查询改造示例
```java
// 改造前
List<Expense> getExpenseList(Long userId) {
    return expenseMapper.selectByUserId(userId);
}

// 改造后
@FamilyPermission("expense_view")
List<Expense> getExpenseList() {
    Long userId = getCurrentUserId();
    Long familyGroupId = getUserFamilyGroupId(userId);  // 单次查询或缓存
    return expenseMapper.selectByFamilyGroupId(familyGroupId);
}
```

### 4. 通知提醒接口预留（TODO）
```java
// 当新成员加入家庭组时调用（暂未实现）
private void notifyAdminNewMemberJoined(Long familyGroupId, Long newMemberId) {
    // TODO: 实现通知逻辑
    // - 发送邮件给管理员
    // - 推送应用通知
    // - 记录系统日志
}
```

---

## 📈 开发计划（16个任务）

| 序号 | 任务 | 预计时间 | 负责人 | 状态 |
|------|------|---------|--------|------|
| 1 | 创建数据库表和字段 | 2h | Agent | ⏳ |
| 2 | 创建Entity类 | 1h | Agent | ⏳ |
| 3 | 创建Mapper接口和XML | 1.5h | Agent | ⏳ |
| 4 | 创建权限模板和权限服务 | 2h | Agent | ⏳ |
| 5 | 创建FamilyGroupService | 2h | Agent | ⏳ |
| 6 | 创建FamilyMemberService | 2h | Agent | ⏳ |
| 7 | 改造用户注册流程 | 2h | Agent | ⏳ |
| 8 | 创建权限检查AOP | 2h | Agent | ⏳ |
| 9 | 修改查询接口 | 4h | Agent | ⏳ |
| 10 | 创建FamilyGroupController | 2h | Agent | ⏳ |
| 11 | 创建FamilyMemberController | 2h | Agent | ⏳ |
| 12 | 创建FamilyManagement.vue | 5h | Agent | ⏳ |
| 13 | 创建JoinFamily.vue | 2h | Agent | ⏳ |
| 14 | 改造Dashboard.vue | 1h | Agent | ⏳ |
| 15 | 改造UserProfile.vue | 1h | Agent | ⏳ |
| 16 | 测试和调试 | 4h | Agent | ⏳ |

**总工期：33.5小时 ≈ 4-5天**

---

## ✅ 检查清单

- [ ] 所有数据库表和字段已创建
- [ ] 所有Entity类已实现
- [ ] 所有Mapper已实现
- [ ] FamilyGroupService已实现所有业务逻辑
- [ ] FamilyMemberService已实现所有业务逻辑
- [ ] 权限检查AOP已实现和测试
- [ ] 所有API接口已实现
- [ ] 所有现有查询接口已改造为family_group_id查询
- [ ] 前端所有页面已实现
- [ ] 完整的端到端测试已通过
- [ ] 权限验证测试已通过
- [ ] 性能测试已通过

---

## 📝 下一步

**第一阶段（今天）：**
1. 创建数据库表
2. 创建Entity和Mapper
3. 创建权限服务

**第二阶段（明天）：**
4. 创建业务Service
5. 改造注册流程
6. 创建权限检查AOP

**第三阶段（后天）：**
7. 实现所有Controller
8. 修改查询接口
9. 实现前端页面

**第四阶段（后天）：**
10. 完整测试
11. 性能调优
12. 项目总结

---

**开始日期**: 2026-01-11  
**预计完成日期**: 2026-01-15  
**最后更新**: 2026-01-11 20:58
