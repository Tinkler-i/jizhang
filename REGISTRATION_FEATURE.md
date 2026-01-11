# 注册功能实现文档

## 概述
本文档说明了新增的用户注册功能的设计和实现细节。

## 功能特性

### 1. 双通道注册支持
- **邮箱注册**：用户可以使用邮箱地址注册账户
- **短信注册**：用户可以使用手机号码注册账户

### 2. 验证码机制
- **生成规则**：4 位随机数字验证码（0000-9999）
- **有效期**：5 分钟（300 秒）
- **存储位置**：数据库 `verification_code` 表
- **过期处理**：存储 `created_time` + `ttl` 字段，验证时检查

### 3. 人机验证
- 使用开源库 `vue-monoplasty-slide-verify` 进行滑动验证码验证
- 防止验证码发送接口被滥用
- 在发送验证码前必须通过人机验证

### 4. 已注册检查
- 在发送验证码前后检查邮箱/手机是否已注册
- 如已注册，提示用户已注册，并给出去登录或修改密码的选项
- 新申请验证码时，会覆盖旧验证码并刷新有效期

### 5. 安全特性
- 密码采用 BCrypt 加密存储
- 用户名唯一性验证
- 邮箱/手机号唯一性验证
- 验证码使用后自动标记为已使用
- 防止过期验证码的使用

## 数据库表结构

### verification_code 表
```sql
CREATE TABLE `verification_code` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '验证码ID',
    `email` VARCHAR(100) COMMENT '邮箱',
    `phone` VARCHAR(20) COMMENT '手机号',
    `code` VARCHAR(10) NOT NULL COMMENT '验证码（4位数字）',
    `type` VARCHAR(20) NOT NULL COMMENT '验证方式：EMAIL、SMS',
    `created_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `ttl` INT DEFAULT 300 COMMENT '生存时间（秒），默认5分钟',
    `is_used` TINYINT DEFAULT 0 COMMENT '是否已使用：0-未使用，1-已使用',
    PRIMARY KEY (`id`),
    KEY `idx_email` (`email`),
    KEY `idx_phone` (`phone`),
    KEY `idx_created_time` (`created_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='验证码表';
```

## 后端 API 接口

### 1. 发送验证码
**请求**
```
POST /api/auth/send-verification-code
Content-Type: application/json

{
  "type": "EMAIL",           // 验证类型：EMAIL或SMS
  "email": "user@example.com", // 邮箱（type为EMAIL时必填）
  "phone": "",               // 手机号（type为SMS时必填）
  "captchaToken": "xxx"      // 人机验证token
}
```

**响应**
```json
{
  "code": 0,
  "message": "验证码已发送",
  "data": null
}
```

### 2. 验证验证码和检查注册状态
**请求**
```
POST /api/auth/verify-code
Content-Type: application/json

{
  "type": "EMAIL",           // 验证类型：EMAIL或SMS
  "email": "user@example.com", // 邮箱
  "phone": "",               // 手机号
  "code": "1234"             // 验证码（可选，仅在验证时使用）
}
```

**响应（邮箱/手机未注册，可以注册）**
```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "success": true,
    "status": "CAN_REGISTER",
    "message": "可以注册"
  }
}
```

**响应（邮箱/手机已注册）**
```json
{
  "code": 0,
  "message": "成功",
  "data": {
    "success": false,
    "status": "REGISTERED",
    "message": "该邮箱已注册"
  }
}
```

### 3. 用户注册
**请求**
```
POST /api/auth/register
Content-Type: application/json

{
  "username": "newuser",
  "password": "password123",
  "confirmPassword": "password123",
  "type": "EMAIL",           // 验证类型：EMAIL或SMS
  "email": "user@example.com", // 邮箱
  "phone": "",               // 手机号
  "code": "1234",            // 验证码
  "captchaToken": "xxx"      // 人机验证token
}
```

**响应**
```json
{
  "code": 0,
  "message": "注册成功，请登录",
  "data": null
}
```

## 后端代码结构

### 新增文件

1. **Entity**
   - `VerificationCode.java` - 验证码实体类

2. **DTO**
   - `RegisterRequest.java` - 注册请求
   - `SendVerificationCodeRequest.java` - 发送验证码请求
   - `VerifyCodeResponse.java` - 验证码验证响应

3. **Mapper**
   - `VerificationCodeMapper.java` - 验证码 MyBatis 映射

4. **Service**
   - `VerificationCodeService.java` - 验证码服务接口
   - `VerificationCodeServiceImpl.java` - 验证码服务实现

5. **Controller**
   - `RegisterController.java` - 注册相关 API 控制器

6. **Database**
   - `V3_0__add_verification_code_table.sql` - 数据库迁移脚本

### 主要类说明

#### VerificationCodeServiceImpl
```java
public class VerificationCodeServiceImpl implements VerificationCodeService {
    
    // 发送验证码（邮件/短信）
    void sendVerificationCode(SendVerificationCodeRequest request)
    
    // 验证验证码和检查注册状态
    VerifyCodeResponse verifyCode(SendVerificationCodeRequest request)
    
    // 完成注册
    void registerWithVerificationCode(RegisterRequest request)
    
    // 检查验证码是否有效
    boolean isValidCode(VerificationCode verificationCode)
    
    // 生成4位验证码
    String generateCode()
    
    // 预留邮件发送接口
    private void sendEmailCode(String email, String code)
    
    // 预留短信发送接口
    private void sendSmsCode(String phone, String code)
}
```

## 前端页面

### Register.vue 组件

**路径**：`/register`

**功能模块**

1. **标签页选择**
   - 邮箱注册 / 短信注册切换

2. **邮箱/手机输入**
   - 实时检查是否已注册
   - 显示注册状态

3. **人机验证**
   - 集成滑动验证码组件
   - 验证成功后才能发送验证码

4. **验证码输入**
   - 4 位数字输入框
   - 倒计时显示（发送后 60 秒内不能重新发送）

5. **注册表单**
   - 用户名输入
   - 密码输入
   - 确认密码
   - 注册按钮

6. **已注册提示**
   - 如果邮箱/手机已注册，显示提示框
   - 提供"去登录"和"修改密码"选项

### API 集成

在 `api/index.js` 中添加的方法：
```javascript
export const authAPI = {
  sendVerificationCode: (data) => api.post('/auth/send-verification-code', data),
  verifyCode: (data) => api.post('/auth/verify-code', data),
  register: (data) => api.post('/auth/register', data)
}
```

### 路由配置

在 `router/index.js` 中添加注册路由：
```javascript
{
  path: '/register',
  name: 'Register',
  component: Register,
  meta: { requiresAuth: false }
}
```

### 登录页面优化

在 `Login.vue` 中更新注册链接指向注册页面：
```vue
<router-link to="/register" class="register-link">立即注册</router-link>
```

## 注册流程

```
用户访问注册页面
    ↓
选择注册方式（邮箱/短信）
    ↓
输入邮箱/手机号
    ↓
检查是否已注册
    ├─ 已注册 → 显示提示和选项
    └─ 未注册 → 继续
    ↓
完成人机验证（滑动验证码）
    ↓
点击"发送验证码"
    ↓
接收验证码（邮件/短信 - 待实现）
    ↓
输入验证码
    ↓
输入用户名
    ↓
输入密码和确认密码
    ↓
点击"立即注册"
    ↓
验证所有信息无误
    ↓
在用户表中创建新用户
    ↓
标记验证码为已使用
    ↓
注册成功，跳转到登录页
```

## 待实现功能

### 1. 邮件发送服务
在 `VerificationCodeServiceImpl.sendEmailCode()` 中实现：
- 集成邮件服务（如 JavaMail、SendGrid 等）
- 发送包含验证码的邮件
- 记录发送状态

### 2. 短信发送服务
在 `VerificationCodeServiceImpl.sendSmsCode()` 中实现：
- 集成短信服务（如 阿里云短信、腾讯云短信 等）
- 发送包含验证码的短信
- 记录发送状态

### 3. 滑动验证码服务
- 实现验证码图片生成和验证逻辑
- 与前端滑动验证码库集成
- 生成和验证 captchaToken

### 4. 密码重置功能
- 已注册提示框中的"修改密码"功能
- 通过邮件/短信重置密码

### 5. 防暴力破解增强
- 可选：限制单个 IP 的请求频率
- 可选：限制单个邮箱/手机的尝试次数
- 可选：增加验证码位数或使用图形验证码

## 配置要求

### NPM 依赖
```json
{
  "dependencies": {
    "vue-monoplasty-slide-verify": "^1.3.0"
  }
}
```

需要运行：
```bash
npm install
```

## 测试建议

### 后端测试
1. 测试发送验证码接口
   - 测试邮箱/手机验证码发送
   - 测试验证码是否正确生成

2. 测试验证接口
   - 测试邮箱已注册检查
   - 测试手机号已注册检查
   - 测试验证码验证

3. 测试注册接口
   - 测试用户名重复
   - 测试邮箱重复
   - 测试手机号重复
   - 测试密码不一致
   - 测试验证码过期
   - 测试验证码错误

### 前端测试
1. 测试表单输入验证
2. 测试邮箱/手机格式验证
3. 测试倒计时功能
4. 测试错误消息显示
5. 测试已注册状态显示
6. 测试跳转到登录页

## 安全建议

1. ✅ HTTPS 传输所有验证码和密码
2. ✅ 验证码存储前加密（可选）
3. ✅ 限制验证码有效期（已实现 5 分钟）
4. ✅ 标记已使用验证码（已实现）
5. ✅ 密码采用 BCrypt 加密（已实现）
6. ⚠️ 建议添加：IP 级别的频率限制
7. ⚠️ 建议添加：邮箱/手机级别的尝试次数限制
8. ⚠️ 建议添加：定期清理过期验证码

## 常见问题

**Q: 验证码 5 分钟后自动失效吗？**
A: 是的，系统会检查 `created_time + ttl` 来判断验证码是否过期。

**Q: 同一邮箱/手机重复申请验证码会怎样？**
A: 新的验证码会覆盖旧验证码，并刷新有效期为 5 分钟。

**Q: 验证码使用后还能再用吗？**
A: 不能，验证码使用后会被标记为 `is_used = 1`，无法再次使用。

**Q: 注册后邮箱需要激活吗？**
A: 不需要，注册成功后可直接使用账号登录。

**Q: 如何实现邮件和短信发送？**
A: 在 `VerificationCodeServiceImpl` 中的 `sendEmailCode()` 和 `sendSmsCode()` 方法实现具体的发送逻辑。
