# 快速参考指南

## 🎯 注册功能快速上手

### 快速启动

```bash
# 1. 构建项目
mvn clean package

# 2. 安装前端依赖
cd src/main/resources/static/app && npm install && cd -

# 3. 运行应用
java -jar target/jizhang-1.0.0.jar

# 4. 访问注册页面
# http://localhost:8080/jizhang/register
```

---

## 📋 文件速查表

### 要修改的文件

| 功能 | 文件位置 | 修改内容 |
|------|---------|--------|
| 邮件服务 | `service/impl/MailServiceImpl.java` | 实现 sendEmailCode() 方法 |
| 短信服务 | `service/impl/SmsServiceImpl.java` | 实现 sendSmsCode() 方法 |
| 验证码验证 | `controller/CaptchaController.java` | 实现 captchaToken 验证 |
| 邮件配置 | `application.yml` | 添加邮件 SMTP 配置 |
| 短信配置 | `application.yml` | 添加短信 API 配置 |

### 要查看的文件

| 功能 | 文件位置 |
|------|---------|
| 注册逻辑 | `service/impl/VerificationCodeServiceImpl.java` |
| API 接口 | `controller/RegisterController.java` |
| 前端页面 | `views/Register.vue` |
| 数据模型 | `entity/VerificationCode.java` |
| API 调用 | `api/index.js` |
| 路由配置 | `router/index.js` |

---

## 🔑 关键代码片段

### 1. 生成验证码
```java
public String generateCode() {
    int code = random.nextInt(10000);
    return String.format("%04d", code);
}
```

### 2. 检查验证码有效性
```java
public boolean isValidCode(VerificationCode verificationCode) {
    if (verificationCode == null || verificationCode.getIsUsed() == 1) {
        return false;
    }
    LocalDateTime createdTime = verificationCode.getCreatedTime();
    long secondsDiff = ChronoUnit.SECONDS.between(createdTime, LocalDateTime.now());
    return secondsDiff <= verificationCode.getTtl();
}
```

### 3. 发送验证码（邮件/短信预留接口）
```java
private void sendEmailCode(String email, String code) {
    // TODO: 调用邮件服务
    System.out.println("发送邮箱验证码: " + email + ", 验证码: " + code);
}

private void sendSmsCode(String phone, String code) {
    // TODO: 调用短信服务
    System.out.println("发送短信验证码: " + phone + ", 验证码: " + code);
}
```

### 4. 前端倒计时
```javascript
const startCodeCountdown = () => {
  codeCountdown.value = 60
  const interval = setInterval(() => {
    codeCountdown.value--
    if (codeCountdown.value <= 0) {
      clearInterval(interval)
    }
  }, 1000)
}
```

---

## 🛠️ 常见任务

### 任务 1：集成邮件服务

**所需时间：2-3 小时**

1. **添加依赖** (pom.xml)
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-mail</artifactId>
</dependency>
```

2. **配置** (application.yml)
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_password
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

3. **实现** (service/MailService.java)
```java
@Service
public class MailService {
    @Autowired
    private JavaMailSender mailSender;
    
    public void sendVerificationCodeEmail(String to, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("注册验证码");
        message.setText("您的验证码是: " + code + "\n有效期为5分钟");
        mailSender.send(message);
    }
}
```

4. **调用** (在 VerificationCodeServiceImpl 中)
```java
private void sendEmailCode(String email, String code) {
    mailService.sendVerificationCodeEmail(email, code);
}
```

### 任务 2：集成短信服务（以阿里云为例）

**所需时间：2-3 小时**

1. **添加依赖** (pom.xml)
```xml
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-core</artifactId>
    <version>4.6.1</version>
</dependency>
<dependency>
    <groupId>com.aliyun</groupId>
    <artifactId>aliyun-java-sdk-dysmsapi</artifactId>
    <version>2.2.1</version>
</dependency>
```

2. **配置** (application.yml)
```yaml
aliyun:
  sms:
    accessKeyId: your_access_key_id
    accessKeySecret: your_access_key_secret
    signName: 你的签名
    templateCode: SMS_1234567
```

3. **实现** (service/SmsService.java)
```java
@Service
public class SmsService {
    @Value("${aliyun.sms.accessKeyId}")
    private String accessKeyId;
    
    public void sendVerificationCodeSms(String phoneNumber, String code) {
        // 调用阿里云短信 API
        // ...
    }
}
```

4. **调用** (在 VerificationCodeServiceImpl 中)
```java
private void sendSmsCode(String phone, String code) {
    smsService.sendVerificationCodeSms(phone, code);
}
```

### 任务 3：实现滑动验证码验证

**所需时间：1-2 小时**

1. **创建验证码服务** (service/CaptchaService.java)
```java
@Service
public class CaptchaService {
    private Map<String, CaptchaSession> sessions = new ConcurrentHashMap<>();
    
    public String createCaptcha() {
        String token = UUID.randomUUID().toString();
        // 生成验证码图片和相关信息
        sessions.put(token, new CaptchaSession());
        return token;
    }
    
    public boolean verifyCaptcha(String token, String x) {
        CaptchaSession session = sessions.get(token);
        if (session == null) return false;
        // 验证滑动距离
        boolean result = session.verify(x);
        sessions.remove(token);
        return result;
    }
}
```

2. **添加验证码 API** (controller/CaptchaController.java)
```java
@RestController
@RequestMapping("/api/captcha")
public class CaptchaController {
    @PostMapping("/get")
    public ApiResponse<String> getCaptcha() {
        String token = captchaService.createCaptcha();
        return ApiResponse.success(token);
    }
    
    @PostMapping("/verify")
    public ApiResponse<Boolean> verifyCaptcha(
        @RequestParam String token,
        @RequestParam String x) {
        boolean result = captchaService.verifyCaptcha(token, x);
        return result ? ApiResponse.success(true) : ApiResponse.error("验证失败");
    }
}
```

3. **在注册时验证**
```java
// 在 RegisterController.sendVerificationCode() 中
@PostMapping("/send-verification-code")
public ApiResponse<Void> sendVerificationCode(
    @RequestBody SendVerificationCodeRequest request) {
    // 验证 captchaToken
    if (!captchaService.isValidToken(request.getCaptchaToken())) {
        return ApiResponse.error("人机验证失败");
    }
    // 继续发送验证码...
}
```

---

## 📱 前端 API 调用示例

### 发送验证码
```javascript
import { authAPI } from '@/api'

const response = await authAPI.sendVerificationCode({
  type: 'EMAIL',
  email: 'user@example.com',
  captchaToken: 'xxx'
})

if (response.code === 0) {
  console.log('验证码已发送')
}
```

### 验证验证码
```javascript
const response = await authAPI.verifyCode({
  type: 'EMAIL',
  email: 'user@example.com',
  code: '1234'
})

if (response.data.success) {
  if (response.data.status === 'CAN_REGISTER') {
    // 可以注册
  } else if (response.data.status === 'REGISTERED') {
    // 已注册
  }
}
```

### 注册用户
```javascript
const response = await authAPI.register({
  username: 'newuser',
  password: 'password123',
  confirmPassword: 'password123',
  type: 'EMAIL',
  email: 'user@example.com',
  code: '1234',
  captchaToken: 'xxx'
})

if (response.code === 0) {
  // 注册成功
  router.push('/login')
}
```

---

## 🧪 测试命令

### 使用 curl 测试

```bash
# 发送验证码
curl -X POST http://localhost:8080/jizhang/api/auth/send-verification-code \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "email": "test@example.com",
    "captchaToken": "xxx"
  }'

# 验证验证码
curl -X POST http://localhost:8080/jizhang/api/auth/verify-code \
  -H "Content-Type: application/json" \
  -d '{
    "type": "EMAIL",
    "email": "test@example.com",
    "code": "1234"
  }'

# 注册
curl -X POST http://localhost:8080/jizhang/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "confirmPassword": "password123",
    "type": "EMAIL",
    "email": "test@example.com",
    "code": "1234",
    "captchaToken": "xxx"
  }'
```

---

## 🔍 数据库查询

### 查看验证码表
```sql
SELECT * FROM verification_code ORDER BY created_time DESC;
```

### 查看用户表
```sql
SELECT id, username, email, phone, status, create_time FROM user;
```

### 查看过期的验证码
```sql
SELECT * FROM verification_code 
WHERE TIMESTAMPDIFF(SECOND, created_time, NOW()) > ttl;
```

### 清理过期验证码
```sql
DELETE FROM verification_code 
WHERE TIMESTAMPDIFF(SECOND, created_time, NOW()) > ttl;
```

---

## 📊 监控和调试

### 启用 SQL 日志
```yaml
logging:
  level:
    com.billmanager.jizhang: DEBUG
    org.mybatis.spring.boot.autoconfigure: DEBUG
```

### 启用 HTTP 日志
```yaml
logging:
  level:
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
```

### 查看日志
```bash
tail -f logs/jizhang.log
```

---

## ⚠️ 常见错误

| 错误 | 原因 | 解决方案 |
|------|------|--------|
| `Invalid email address` | 邮箱格式错误 | 检查邮箱格式验证 |
| `Verification code expired` | 验证码已过期 | 重新申请验证码 |
| `User already exists` | 用户已存在 | 使用不同的用户名 |
| `Invalid captcha token` | 人机验证失败 | 重新完成滑动验证 |
| `Table doesn't exist` | 数据库迁移未执行 | 运行 SQL 迁移脚本 |

---

## 📚 相关文档索引

```
REGISTRATION_SUMMARY.md          ← 本项目总体总结
REGISTRATION_FEATURE.md          ← 详细功能文档
REGISTRATION_IMPLEMENTATION.md   ← 实施报告
REGISTRATION_CHECKLIST.md        ← 实施清单
REGISTRATION_QUICK_GUIDE.md      ← 本文件
```

---

## 💬 快速问答

**Q: 验证码为什么是 4 位？**
A: 用户体验好，输入快速。可改为 6 位获得更高安全性。

**Q: 为什么用 TTL 而不是过期时间？**
A: 更灵活，支持动态调整过期时间。

**Q: 为什么需要滑动验证码？**
A: 防止暴力破解和接口滥用。

**Q: 为什么 5 分钟就过期？**
A: 平衡安全性和用户体验。可调整为 10 或 15 分钟。

**Q: 支持第三方登录吗？**
A: 暂不支持，可后期扩展。

---

**最后更新：2026-01-11**

**建议：先完成邮件/短信服务集成，再进行完整的功能测试** ✅
