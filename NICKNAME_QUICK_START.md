# 昵称功能快速开始指南

## 📋 功能描述

为系统添加了用户昵称（nickname）功能，允许用户在个人设置中修改昵称，用于在家庭组的成员管理中更清晰地显示和区分成员。

## 🚀 快速使用

### 1. 数据库初始化

如果你是新安装项目，系统会自动通过 Flyway 迁移脚本 `V4_0__add_nickname_field.sql` 创建 nickname 字段。

如果你是现有项目升级，迁移脚本会自动：
- 添加 nickname 列到 user 表
- 将所有现有用户的 nickname 设置为其 username

**无需手动执行任何 SQL 语句！**

### 2. 修改昵称 API

#### 请求
```bash
curl -X PUT http://localhost:8080/api/user/nickname \
  -H "Content-Type: application/json" \
  -d '{"nickname": "小明的新昵称"}'
```

#### 响应
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "xiaoming",
    "nickname": "小明的新昵称",
    "email": "xiaoming@example.com",
    ...
  },
  "message": "昵称已更新"
}
```

### 3. 获取家庭组成员列表（含昵称）

#### 请求
```bash
curl http://localhost:8080/api/family-members/list
```

#### 响应
```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "familyGroupId": 1,
      "username": "xiaoming",
      "nickname": "小明",
      "email": "xiaoming@example.com",
      "phone": "13800138000",
      "role": "ADMIN",
      "status": 1,
      "joinTime": "2026-01-13T10:00:00"
    },
    {
      "id": 2,
      "userId": 2,
      "familyGroupId": 1,
      "username": "xiaohong",
      "nickname": "小红",
      "email": "xiaohong@example.com",
      "phone": "13800138001",
      "role": "MEMBER",
      "status": 1,
      "joinTime": "2026-01-13T11:00:00"
    }
  ],
  "count": 2
}
```

### 4. 获取个人资料（已包含昵称）

#### 请求
```bash
curl http://localhost:8080/api/user/profile
# 或
curl http://localhost:8080/api/auth/profile
```

#### 响应
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "xiaoming",
    "nickname": "小明",
    "email": "xiaoming@example.com",
    "phone": "13800138000",
    ...
  },
  "message": "获取成功"
}
```

## 📝 前端集成示例

### Vue/JavaScript 代码示例

```javascript
// 更新昵称
async function updateNickname() {
  const newNickname = document.querySelector('#nicknameInput').value;
  
  try {
    const response = await fetch('/api/user/nickname', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nickname: newNickname })
    });
    
    const result = await response.json();
    if (result.code === 200) {
      alert('昵称已更新！');
      // 更新本地用户数据
      localStorage.user = JSON.stringify(result.data);
    } else {
      alert('更新失败：' + result.message);
    }
  } catch (error) {
    console.error('更新失败', error);
  }
}

// 获取家庭组成员
async function loadFamilyMembers() {
  try {
    const response = await fetch('/api/family-members/list');
    const result = await response.json();
    
    if (result.code === 200) {
      // 显示成员列表，包含昵称
      result.data.forEach(member => {
        console.log(`${member.username} (${member.nickname}) - ${member.role}`);
      });
    }
  } catch (error) {
    console.error('加载成员列表失败', error);
  }
}
```

## 🔄 使用流程

### 场景 1: 新用户注册
```
1. 用户注册
   ↓
2. 系统自动创建 User，nickname = username
   ↓
3. 用户成功登录
```

### 场景 2: 修改昵称
```
1. 用户打开个人设置页面
   ↓
2. 用户输入新昵称并点击保存
   ↓
3. 调用 PUT /api/user/nickname
   ↓
4. 昵称更新成功
   ↓
5. 家庭组成员能看到更新后的昵称
```

### 场景 3: 查看家庭组成员
```
1. 用户打开家庭管理页面
   ↓
2. 调用 GET /api/family-members/list
   ↓
3. 返回所有成员及其昵称
   ↓
4. 前端显示 "用户名（昵称）" 格式
```

## ⚙️ 技术细节

| 项目 | 详情 |
|------|------|
| 字段类型 | VARCHAR(50) |
| 是否必填 | 是（默认为 username） |
| 修改权限 | 用户只能修改自己的昵称 |
| 家庭组权限 | 所有成员都能看到其他成员的昵称 |
| 数据库迁移 | 通过 Flyway 自动管理 |

## 📚 相关文件

- 详细实现文档: [NICKNAME_IMPLEMENTATION.md](NICKNAME_IMPLEMENTATION.md)
- 数据库迁移脚本: `src/main/resources/sql/V4_0__add_nickname_field.sql`
- 实体类: `src/main/java/com/billmanager/jizhang/entity/User.java`
- DTO: `src/main/java/com/billmanager/jizhang/dto/FamilyMemberDTO.java`
- 数据库映射: `src/main/resources/mapper/UserMapper.xml`

## ✅ 已验证的功能

- ✅ 新用户注册时自动设置昵称
- ✅ 用户能修改自己的昵称
- ✅ 获取家庭组成员列表时包含昵称信息
- ✅ 个人资料 API 包含昵称字段
- ✅ 修改昵称后，Session 自动更新
- ✅ 现有用户升级后昵称自动设置为 username

## 🐛 故障排查

### 问题：更新昵称时返回 401 错误
**解决方案**: 确保已登录，检查 Session 是否有效

### 问题：获取成员列表时 nickname 为 null
**解决方案**: 运行迁移脚本后重新启动应用，确保所有用户都有 nickname 值

### 问题：前端无法获取 nickname 字段
**解决方案**: 确保使用了最新的 User 实体类和 UserMapper.xml

## 💡 建议

1. **前端优化**: 在成员列表中显示昵称而非用户名，提升用户体验
2. **验证增强**: 可添加昵称长度验证和特殊字符检查
3. **历史记录**: 可考虑记录昵称修改历史
4. **搜索功能**: 可支持按昵称搜索成员

---

如有问题，请参考详细实现文档或检查应用日志。
