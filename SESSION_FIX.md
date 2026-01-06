# 会话持久化问题解决方案

## 问题分析

在之前的测试中，用户成功登录到系统后，访问 `/income-category` 和其他受保护页面时收到 403 Forbidden 错误。根本原因是会话中保存的用户对象在后续请求中不可用。

## 根本原因

1. **会话不持久化**：虽然在 `LoginController.login()` 中将用户对象存储在 `session.setAttribute("user", user)` 中，但这个会话属性在后续请求中不可用。

2. **SecurityContext 可能未正确还原**：虽然我们显式保存了 `SecurityContext` 到会话，但在后续请求中可能没有被正确还原。

3. **会话管理政策问题**：原始的 `SessionCreationPolicy.IF_REQUIRED` 可能导致会话创建不一致。

## 实施的解决方案

### 1. 更新 SecurityConfig.java

**主要改变：**
- 将 `SessionCreationPolicy` 从 `IF_REQUIRED` 改为 `ALWAYS`：确保每个请求都创建和维护会话
- 配置 `SecurityContextRepository` 为 `HttpSessionSecurityContextRepository`：启用会话中 SecurityContext 的持久化
- 添加会话超时配置到 `application.yml`

```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
)
```

### 2. 更新 application.yml

**新增配置：**
```yaml
spring:
  session:
    store-type: memory
    timeout: 30m

server:
  servlet:
    session:
      cookie:
        secure: false
        http-only: true
        max-age: 1800
```

### 3. 改进所有 Controllers 的用户获取方式

所有 Controller（IncomeCategoryController, IncomeController, LoginController）现在都实现了相同的 `getCurrentUser()` 辅助方法：

```java
private User getCurrentUser(HttpSession session) {
    // 首先尝试从 Session 中获取
    User user = (User) session.getAttribute("user");
    if (user != null) {
        return user;
    }
    
    // 如果 Session 中没有，从 SecurityContext 获取用户名，然后从数据库查询
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth != null && auth.isAuthenticated() && !auth.getPrincipal().equals("anonymousUser")) {
        String username = auth.getName();
        user = userMapper.findByUsername(username);
        if (user != null) {
            // 缓存到 Session 供后续使用
            session.setAttribute("user", user);
            return user;
        }
    }
    
    return null;
}
```

**这种双重机制的好处：**
1. **快速路径**：如果用户已在会话中，直接返回
2. **容错路径**：如果会话丢失但 SecurityContext 仍然有效，从数据库重新加载用户信息
3. **自愈能力**：在从 SecurityContext 恢复用户后，将其重新缓存到会话中

### 4. SecurityConfig 中的权限配置

```java
.requestMatchers("/login", "/api/login", "/logout", "/css/**", "/js/**", "/images/**", "/test.html").permitAll()
.requestMatchers("/dashboard", "/income", "/income-category", "/expense", "/expense-category", 
                "/budget", "/report", "/api/income/**", "/api/income-category/**", 
                "/api/expense/**", "/api/expense-category/**", "/api/budget/**", "/api/report/**").authenticated()
```

## 关键改进

| 问题 | 原因 | 解决方案 |
|------|------|----------|
| 会话在请求间丢失 | SessionCreationPolicy.IF_REQUIRED 不够可靠 | 更改为 ALWAYS |
| 用户对象为 null | 会话未被恢复 | 实现双重获取机制 |
| SecurityContext 未持久化 | 未配置会话保存 | 配置 HttpSessionSecurityContextRepository |
| Cookie 问题 | 未明确配置 | 在 application.yml 中配置会话 Cookie |

## 验证方法

使用提供的 `test.html` 工具进行测试：

1. 打开 http://localhost:8080/jizhang/test.html
2. 点击"发送登录请求"按钮
3. 成功登录后，点击"访问受保护页面"中的按钮测试各个功能

## 预期结果

- ✅ 登录成功返回 200 状态码
- ✅ 后续访问 /income-category, /income, /dashboard 返回 200
- ✅ API 调用 GET /api/income-category 返回带有数据的正确响应
- ✅ JSESSIONID Cookie 在整个会话期间保持

## 技术细节

### 会话流程

1. **登录时 (POST /api/login)**
   - 用户输入凭证
   - 验证成功后创建 User 对象
   - 保存到 HttpSession: `session.setAttribute("user", user)`
   - 创建 UsernamePasswordAuthenticationToken
   - 设置到 SecurityContextHolder
   - 保存 SecurityContext 到会话

2. **后续请求时**
   - Spring Security 过滤器链首先尝试从 HttpSession 恢复 SecurityContext
   - Controller 调用 getCurrentUser() 获取用户
   - 如果会话中有用户，直接返回
   - 如果没有但 SecurityContext 有效，从数据库加载并缓存

### 关键过滤器

从日志可以看到应用的安全过滤器链包含：
- `ForceEagerSessionCreationFilter` - 确保总是创建会话
- `SecurityContextHolderFilter` - 保存和恢复 SecurityContext
- `SessionManagementFilter` - 管理会话生命周期
- `AuthorizationFilter` - 检查权限

## 未来可能的改进

1. **Redis 会话存储**：在生产环境中使用 Redis 替代内存会话存储
   ```yaml
   spring:
     session:
       store-type: redis
   ```

2. **使用 @AuthenticationPrincipal**：在 Spring Security 集成更好时，可以使用注解直接注入用户
   ```java
   public String categoryPage(@AuthenticationPrincipal UserDetails principal, Model model)
   ```

3. **JWT Token**：对于 REST API，考虑使用 JWT 而不是会话

## 测试清单

- [x] 修改 SecurityConfig 会话政策
- [x] 更新 application.yml 会话配置
- [x] 为所有 Controllers 添加 getCurrentUser() 方法
- [x] 测试 LoginController.dashboard() 使用新方法
- [x] 编译项目无错误
- [x] 应用启动成功
- [x] 提供测试工具 (test.html)

## 下一步

用户应该：
1. 在浏览器中打开 http://localhost:8080/jizhang/test.html
2. 点击测试按钮验证各个功能
3. 如果所有测试通过，系统就可以正常使用了
