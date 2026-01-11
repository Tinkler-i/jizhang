# 邮箱验证码功能 - 快速启动

## 已完成的工作

✅ 后端邮箱验证码发送功能已实现
✅ Maven 依赖已添加 (spring-boot-starter-mail)
✅ 邮件服务类已创建 (EmailService)
✅ 验证码生成和保存到数据库
✅ HTML 邮件模板已设计
✅ 代码已成功编译

## 快速配置步骤

### 第 1 步：选择邮箱服务商

推荐使用 **QQ 邮箱**（最简单）

### 第 2 步：获取 SMTP 凭证

对于 QQ 邮箱：
1. 登录 https://mail.qq.com
2. 设置 → 账户 → 找到 POP3/SMTP/IMAP 服务
3. 点击"开启"
4. 获取授权码（16位）

### 第 3 步：配置应用

编辑文件：`src/main/resources/application.yml`

找到邮箱配置部分，修改：

```yaml
spring:
  mail:
    host: smtp.qq.com              # QQ邮箱 SMTP 服务器
    port: 587                       # 端口
    username: your-email@qq.com     # 替换为你的 QQ 邮箱
    password: your-app-password     # 替换为授权码（16位）
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
```

### 第 4 步：重新编译

```bash
# 删除 target 目录
Remove-Item -Path "target" -Recurse -Force

# 编译和打包
mvn clean package -DskipTests

# 或者只编译
mvn compile -DskipTests
```

### 第 5 步：启动应用

```bash
# 方式 1：直接运行 JAR
java -jar target/jizhang-1.0.0.jar

# 方式 2：用 Maven 运行
mvn spring-boot:run
```

### 第 6 步：测试功能

1. 打开浏览器访问：`http://localhost:5173/jizhang/register`
2. 在注册页面：
   - 选择"邮箱注册"
   - 输入你配置的邮箱地址
   - 点击人机验证
   - 点击"发送验证码"
3. 检查你的邮箱收件箱
4. 如果没有收到，检查垃圾邮件文件夹

## 预期的邮件样式

你会收到一封格式化的 HTML 邮件，包含：
- 账户验证标题
- 4位数字验证码（大号显示）
- 验证码有效期（5分钟）
- 安全提示

## 常见邮箱配置参考

| 邮箱服务商 | SMTP 服务器 | 端口 | 获取授权码方式 |
|-----------|-----------|-----|-------------|
| QQ 邮箱   | smtp.qq.com | 587 | 设置 → POP3/SMTP |
| 163 邮箱  | smtp.163.com | 587 | 设置 → POP3/SMTP |
| Gmail     | smtp.gmail.com | 587 | 应用密码 |
| 企业邮箱   | 按公司配置  | 按公司配置 | 按公司配置 |

## 验证邮箱配置是否正确

查看应用启动日志，如果看到类似信息：
```
Sending verification code email to: xxx@qq.com
```

说明邮件服务已成功初始化。

如果看到错误信息，根据错误类型查看 `EMAIL_CONFIG_GUIDE.md` 中的故障排查部分。

## 代码位置

### 前端代码
- 注册页面：`src/main/resources/static/app/src/views/Register.vue`
- 注册 API：`src/main/resources/static/app/src/api/index.js`
- 路由配置：`src/main/resources/static/app/src/router/index.js`

### 后端代码
- 邮箱服务：`src/main/java/com/billmanager/jizhang/service/EmailService.java`
- 邮箱实现：`src/main/java/com/billmanager/jizhang/service/impl/EmailServiceImpl.java`
- 验证码服务：`src/main/java/com/billmanager/jizhang/service/impl/VerificationCodeServiceImpl.java`
- 注册控制器：`src/main/java/com/billmanager/jizhang/controller/RegisterController.java`

### 配置文件
- 应用配置：`src/main/resources/application.yml`
- 邮件配置：`src/main/resources/application.yml` (spring.mail 部分)

## 下一步（可选）

### 短信验证码功能
参考 `SMS_CONFIG_GUIDE.md`（后续编写）

### 邮件模板自定义
编辑 `EmailServiceImpl.java` 中的 `buildVerificationCodeEmail()` 方法

### 发送频率限制
在 `VerificationCodeServiceImpl.java` 中添加频率限制逻辑

## 获取帮助

- 查看详细配置指南：`EMAIL_CONFIG_GUIDE.md`
- 查看应用日志了解具体错误
- 启用 debug 日志：在 `application.yml` 添加
  ```yaml
  logging:
    level:
      org.springframework.mail: debug
  ```

## 安全提示

⚠️ **生产环境注意**：
- 不要在代码中硬编码邮箱密码
- 使用环境变量或加密配置
- 启用 TLS/SSL 加密
- 定期更新授权码
