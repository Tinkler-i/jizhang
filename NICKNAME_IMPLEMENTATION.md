# 用户昵称功能实现总结

## 功能概述
为系统添加用户昵称字段，用于在家庭组管理的成员管理中显示和区分用户，并允许用户在个人设置中修改昵称。

## 实现的需求
- ✅ 用户表添加 nickname 字段
- ✅ 默认昵称与注册时的用户名一致
- ✅ 个人设置中可修改昵称
- ✅ 家庭组成员管理中显示用户昵称
- ✅ 用户修改昵称后，家庭组中其他成员能看到更新后的昵称

## 技术实现详情

### 1. 数据库更改

#### 修改 schema.sql
- **文件**: `src/main/resources/sql/schema.sql`
- **改动**: 在 `user` 表的 `username` 字段后添加 `nickname` 字段
- **字段类型**: `VARCHAR(50)`
- **说明**: 用于家庭组成员管理中显示

#### 创建迁移脚本
- **文件**: `src/main/resources/sql/V4_0__add_nickname_field.sql`
- **功能**:
  - 为现有 user 表添加 nickname 列
  - 自动将所有现有用户的 nickname 设置为其 username
  - 设置 nickname 为非空字段

### 2. 实体类更改

#### User.java
- **文件**: `src/main/java/com/billmanager/jizhang/entity/User.java`
- **改动**: 添加 `private String nickname;` 属性

### 3. 数据库映射层更改

#### UserMapper.xml
- **文件**: `src/main/resources/mapper/UserMapper.xml`
- **改动**:
  - 在 `BaseResultMap` 中添加 nickname 字段映射
  - 在 `insert` 语句中添加 nickname 参数
  - 在 `update` 语句中添加 nickname 的条件更新

### 4. 业务逻辑层更改

#### VerificationCodeServiceImpl.java
- **文件**: `src/main/java/com/billmanager/jizhang/service/impl/VerificationCodeServiceImpl.java`
- **改动**: 在 `registerWithVerificationCode` 方法中，注册新用户时自动设置 `nickname = username`

### 5. 控制器层更改

#### LoginController.java
- **文件**: `src/main/java/com/billmanager/jizhang/controller/LoginController.java`
- **新增方法**: `updateNickname`
  - **请求方法**: `PUT`
  - **路由**: `/api/user/nickname`
  - **请求体**:
    ```json
    {
      "nickname": "新昵称"
    }
    ```
  - **响应**: 返回更新后的用户对象
  - **功能**: 允许用户修改自己的昵称

#### FamilyMemberController.java
- **文件**: `src/main/java/com/billmanager/jizhang/controller/FamilyMemberController.java`
- **引入依赖**: 
  - `UserMapper userMapper` - 用于获取用户详细信息
  - `FamilyMemberDTO` - 用于返回成员和用户信息
- **新增方法**: `getFamilyMembers`
  - **请求方法**: `GET`
  - **路由**: `/api/family-members/list`
  - **响应**: 返回家庭组所有成员的详细信息，包括昵称
  - **返回数据结构**:
    ```json
    {
      "code": 200,
      "data": [
        {
          "id": 1,
          "userId": 1,
          "familyGroupId": 1,
          "username": "user1",
          "nickname": "小明",
          "email": "user1@example.com",
          "phone": "13800138000",
          "role": "ADMIN",
          "status": 1,
          ...
        }
      ],
      "count": 3
    }
    ```

### 6. 数据传输对象（DTO）

#### FamilyMemberDTO.java
- **文件**: `src/main/java/com/billmanager/jizhang/dto/FamilyMemberDTO.java` (新建)
- **字段**:
  - 成员相关: id, familyGroupId, userId, role, permissions, joinTime, status
  - 用户相关: username, nickname, email, phone
  - 时间戳: createTime, updateTime
- **用途**: 在成员列表 API 中返回成员信息和用户昵称

## API 端点汇总

### 1. 修改用户昵称
```
PUT /api/user/nickname
Content-Type: application/json

Request:
{
  "nickname": "新昵称"
}

Response:
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "user1",
    "nickname": "新昵称",
    "email": "user1@example.com",
    ...
  },
  "message": "昵称已更新"
}
```

### 2. 获取家庭组成员列表（包含昵称）
```
GET /api/family-members/list

Response:
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "userId": 1,
      "familyGroupId": 1,
      "username": "user1",
      "nickname": "小明",
      "email": "user1@example.com",
      "phone": "13800138000",
      "role": "ADMIN",
      "status": 1,
      ...
    }
  ],
  "count": 3
}
```

### 3. 获取个人信息（已包含昵称）
```
GET /api/user/profile
或
GET /api/auth/profile

Response:
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "user1",
    "nickname": "小明",
    "email": "user1@example.com",
    ...
  }
}
```

## 修改的文件清单

### 核心业务文件（源码）
1. `src/main/java/com/billmanager/jizhang/entity/User.java` - 添加 nickname 属性
2. `src/main/java/com/billmanager/jizhang/service/impl/VerificationCodeServiceImpl.java` - 注册时设置 nickname
3. `src/main/java/com/billmanager/jizhang/controller/LoginController.java` - 添加修改昵称 API
4. `src/main/java/com/billmanager/jizhang/controller/FamilyMemberController.java` - 添加获取成员列表 API
5. `src/main/java/com/billmanager/jizhang/dto/FamilyMemberDTO.java` - 新建 DTO 类

### 数据库配置文件
1. `src/main/resources/sql/schema.sql` - 更新表结构
2. `src/main/resources/sql/V4_0__add_nickname_field.sql` - 数据库迁移脚本
3. `src/main/resources/mapper/UserMapper.xml` - 更新 MyBatis 映射

## 使用流程

### 场景 1: 新用户注册
1. 用户注册时，系统自动设置 `nickname = username`
2. 用户可以在个人设置中修改昵称
3. 修改后，家庭组中所有成员都能看到更新后的昵称

### 场景 2: 查看家庭组成员
1. 调用 `/api/family-members/list` 端点
2. 系统返回所有成员的信息，包括各自的昵称
3. 前端可以使用 nickname 字段在界面中显示成员昵称

### 场景 3: 用户修改昵称
1. 用户在个人设置页面输入新昵称
2. 调用 `PUT /api/user/nickname` 端点
3. 系统更新用户的昵称
4. Session 中的用户信息也被同步更新

## 数据库注意事项

### Flyway 迁移
系统使用 Flyway 管理数据库版本。新的迁移脚本 `V4_0__add_nickname_field.sql` 会在应用启动时自动执行。

### 现有数据处理
- 现有用户的 nickname 会自动设置为其 username
- 如果已有 nickname 字段，迁移脚本会跳过列添加步骤（因为已存在）

## 前端集成建议

### 1. 修改昵称表单
```javascript
// 更新昵称
async function updateNickname(nickname) {
  const response = await fetch('/api/user/nickname', {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ nickname })
  });
  return response.json();
}
```

### 2. 获取并显示成员昵称
```javascript
// 获取家庭组成员列表
async function getFamilyMembers() {
  const response = await fetch('/api/family-members/list');
  const data = await response.json();
  
  // data.data 是成员列表，每个成员都包含 nickname 字段
  data.data.forEach(member => {
    console.log(`${member.username} (${member.nickname})`);
  });
}
```

## 向后兼容性

- ✅ 现有的用户信息查询 API 自动包含 nickname 字段
- ✅ UserMapper 中的更新逻辑支持选择性更新
- ✅ 现有代码无需修改即可适配新字段

## 后续可能的增强

1. 添加昵称长度验证
2. 添加昵称唯一性验证（可选）
3. 添加昵称修改历史记录
4. 在成员列表中显示昵称而非用户名
5. 支持昵称搜索功能
