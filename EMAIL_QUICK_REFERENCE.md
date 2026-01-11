# 邮箱配置 - 快速参考

## 5分钟快速设置

### 1️⃣ 获取授权码（以QQ邮箱为例）

```
登录 mail.qq.com 
→ 设置 → 账户 
→ POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV 服务
→ 开启 
→ 复制授权码（16位）
```

### 2️⃣ 配置文件

编辑 `src/main/resources/application.yml`，替换这两行：

```yaml
username: your-email@qq.com         # 替换为你的邮箱
password: xxxx-xxxx-xxxx-xxxx       # 替换为授权码
```

### 3️⃣ 编译运行

```bash
mvn clean package -DskipTests
java -jar target/jizhang-1.0.0.jar
```

### 4️⃣ 测试

访问：`http://localhost:5173/jizhang/register`
- 选择邮箱注册
- 输入你的邮箱
- 点击发送验证码
- 查收邮件

---

## 主流邮箱一览

| 邮箱 | 服务器 | 端口 | 开启方式 |
|------|--------|------|--------|
| QQ | smtp.qq.com | 587 | 设置 → POP3/SMTP |
| 163 | smtp.163.com | 587 | 设置 → POP3/SMTP |
| Gmail | smtp.gmail.com | 587 | 应用密码 |

---

## 日志调试

```yaml
logging:
  level:
    org.springframework.mail: debug
```

---

## 常见错误

| 错误 | 原因 | 解决 |
|-----|------|------|
| 连接超时 | SMTP 配置错 | 检查服务器地址和端口 |
| 认证失败 | 密码或邮箱错 | 重新复制授权码 |
| 收不到邮件 | 服务未开启 | 确保在邮箱设置中开启 SMTP |

---

## 文件位置速查

| 文件 | 路径 |
|-----|-----|
| 邮箱配置 | `src/main/resources/application.yml` |
| 邮箱服务 | `src/main/java/.../service/EmailService.java` |
| 邮箱实现 | `src/main/java/.../service/impl/EmailServiceImpl.java` |
| 验证码服务 | `src/main/java/.../service/impl/VerificationCodeServiceImpl.java` |

---

## 更多帮助

- 详细指南：`EMAIL_CONFIG_GUIDE.md`
- 实现总结：`EMAIL_IMPLEMENTATION_SUMMARY.md`
- 快速启动：`EMAIL_QUICK_START.md`
