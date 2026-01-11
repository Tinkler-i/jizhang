# 邮箱验证码功能配置指南

## 概述

注册系统已完成邮箱验证码发送功能的实现。本文档说明如何配置邮箱服务以启用验证码发送。

## 支持的邮箱服务商

### 1. QQ 邮箱（推荐）

**步骤 1：开启 SMTP 服务**

1. 登录 [QQ 邮箱](https://mail.qq.com)
2. 进入设置 → 账户
3. 找到 "POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV 服务"
4. 点击 "开启" 按钮

**步骤 2：获取授权码**

1. 开启 SMTP 服务后，QQ 邮箱会弹出授权码
2. 复制这个 16 位的授权码
3. 保存好，后面需要配置

**步骤 3：配置应用**

编辑 `src/main/resources/application.yml`：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: your-email@qq.com         # 替换为你的 QQ 邮箱
    password: your-app-password         # 替换为刚才获取的授权码
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

### 2. 163 邮箱

**步骤 1：开启 SMTP/POP3 服务**

1. 登录 [163 邮箱](https://mail.163.com)
2. 进入设置 → POP3/SMTP/IMAP
3. 开启 POP3/SMTP 服务
4. 验证手机号获取授权密码

**步骤 2：配置应用**

```yaml
spring:
  mail:
    host: smtp.163.com
    port: 587
    username: your-email@163.com
    password: your-app-password
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

### 3. Gmail

**步骤 1：启用应用程序密码**

1. 访问 [Google 账户安全设置](https://myaccount.google.com/security)
2. 启用 2FA（两步验证）
3. 生成应用专用密码

**步骤 2：配置应用**

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
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

## 测试邮箱配置

### 方法 1：使用应用程序

1. **配置 application.yml 文件**
2. **重新编译和启动应用**：
   ```bash
   mvn clean package
   java -jar target/jizhang-1.0.0.jar
   ```
3. **在前端注册页面测试**：
   - 选择邮箱注册
   - 输入你配置的邮箱地址
   - 点击发送验证码
   - 检查邮箱收件箱

### 方法 2：使用 Java 测试类

在项目中创建测试类 `EmailServiceTest.java`：

```java
package com.billmanager.jizhang.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class EmailServiceTest {
    
    @Autowired
    private EmailService emailService;
    
    @Test
    public void testSendVerificationCode() {
        emailService.sendVerificationCode("your-test-email@qq.com", "1234");
        System.out.println("邮件已发送");
    }
}
```

运行测试：
```bash
mvn test -Dtest=EmailServiceTest
```

## 常见问题

### 问题 1：收不到邮件

**原因可能**：
- SMTP 服务未开启
- 授权码错误
- 邮箱被标记为垃圾邮件

**解决方案**：
1. 确认 SMTP 服务已开启
2. 重新检查授权码是否正确
3. 检查垃圾邮件文件夹
4. 查看应用日志获取具体错误信息

### 问题 2：连接超时

**原因可能**：
- 网络连接问题
- SMTP 服务器地址或端口错误
- 防火墙阻止

**解决方案**：
1. 检查网络连接
2. 确认 SMTP 配置正确
3. 检查防火墙设置

### 问题 3：认证失败

**原因可能**：
- 邮箱地址拼写错误
- 授权码拼写错误（注意大小写）
- 账户被锁定

**解决方案**：
1. 重新复制粘贴授权码（避免手动输入）
2. 检查邮箱地址
3. 在邮箱官网重新解锁账户

## 日志调试

启用邮件调试日志，编辑 `application.yml`：

```yaml
logging:
  level:
    org.springframework.mail: debug
    org.springframework.mail.javamail: debug
```

然后查看控制台输出获取详细信息。

## 邮件模板自定义

邮件模板位于 `EmailServiceImpl.java` 的 `buildVerificationCodeEmail()` 方法中。

你可以修改：
- 邮件主题
- HTML 内容
- CSS 样式
- 品牌信息

## 安全建议

1. **不要在代码中硬编码密码**
   - 使用环境变量或配置文件
   - 在生产环境使用加密配置

2. **使用 SMTP 认证**
   - 启用 TLS/SSL 加密
   - 禁用不安全的连接

3. **限制发送频率**
   - 防止滥用
   - 建议每个邮箱 60 秒内只能发送一次

4. **日志安全**
   - 避免在日志中记录敏感信息（如密码）

## 生产环境部署

### 使用环境变量

```bash
# Linux/Mac
export MAIL_USERNAME=your-email@qq.com
export MAIL_PASSWORD=your-app-password
export MAIL_HOST=smtp.qq.com

# Windows
set MAIL_USERNAME=your-email@qq.com
set MAIL_PASSWORD=your-app-password
set MAIL_HOST=smtp.qq.com
```

### Docker 部署

```dockerfile
FROM openjdk:17-slim
ADD target/jizhang-1.0.0.jar app.jar
ENV MAIL_USERNAME=your-email@qq.com
ENV MAIL_PASSWORD=your-app-password
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 云服务部署

在云服务（如阿里云、腾讯云）的配置中心设置邮箱参数。

## 相关代码位置

- **邮箱服务接口**: `src/main/java/com/billmanager/jizhang/service/EmailService.java`
- **邮箱服务实现**: `src/main/java/com/billmanager/jizhang/service/impl/EmailServiceImpl.java`
- **验证码服务**: `src/main/java/com/billmanager/jizhang/service/impl/VerificationCodeServiceImpl.java`
- **应用配置**: `src/main/resources/application.yml`

## 下一步

- [ ] 实现短信验证码发送（使用阿里云 SMS 或腾讯云 SMS）
- [ ] 添加邮件发送频率限制
- [ ] 实现邮件模板引擎（Freemarker/Thymeleaf）
- [ ] 添加邮件发送记录和统计
