# 登录过期Bug修复说明

## 问题描述
- 特定情况下登录过期但用户仍能进入管理页面
- 管理页面数据无法获取（401/500错误）
- 前端没有提示用户，用户必须手动退出重新登录
- 控制台显示：路由守卫验证token成功，但后端返回"未登录或会话已过期"

## 根本原因
1. **后端问题**：`LoginController.getProfile()` 返回200状态码+错误消息，而不是401状态码
2. **前端问题**：响应拦截器只检查401状态码，未检查响应体中的错误信息
3. **路由守卫问题**：没有正确处理响应数据中的错误，没有给用户提示

## 修复方案

### 后端修改 (LoginController.java)
✅ 在会话过期时返回401状态码而不是200+错误信息：
```java
// 修改前
public ApiResponse<User> getProfile(HttpSession session) {
    User user = getCurrentUser(session);
    if (user == null) {
        return ApiResponse.error("未登录或会话已过期");  // 返回200状态码
    }
}

// 修改后
public ApiResponse<User> getProfile(HttpSession session, HttpServletResponse response) {
    User user = getCurrentUser(session);
    if (user == null) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);  // 返回401状态码
        return ApiResponse.error("未登录或会话已过期");
    }
}
```

### 前端修改 1 - API拦截器 (api/index.js)
✅ 改进响应拦截器，检查多个认证失败的标志：
- 检查HTTP状态码 401
- 检查响应体中的错误消息（"未登录"、"会话已过期"）
- 在任何认证相关错误时清除令牌并重定向到登录页

### 前端修改 2 - 路由守卫 (router/index.js)
✅ 增强路由守卫验证逻辑：
- 检查响应码 (response.code)
- 检查响应体中的错误消息
- 在验证失败时显示用户提示信息（通过UI Store）
- 正确处理401错误
- 完善的错误日志输出

### 前端修改 3 - UI 存储
✅ 利用现有的 `showNotification()` 方法显示会话过期警告

## 工作流程

### 正常流程
1. 用户有效token → 路由守卫验证 → `getProfile()` 返回200+用户信息 → 继续导航
2. API请求 → 返回200+正常数据 → 继续

### 会话过期流程 (修复后)
1. 用户token过期 → 路由守卫调用 `getProfile()`
2. 后端返回 **401状态码** + "未登录或会话已过期"
3. 前端响应拦截器捕获401 → 清除token → 重定向到登录页 ✅
4. **或者** 路由守卫捕获401错误 → 显示提示信息 → 清除token → 重定向到登录页 ✅
5. 用户看到提示信息："登录已过期，请重新登录"

### 页面加载时过期流程 (修复后)
1. 用户在管理页面 → 发起API请求（收入、支出等）
2. 后端session已过期 → 返回**401状态码** 或 200+认证错误信息
3. 响应拦截器捕获 → 清除token → 重定向到登录页 ✅
4. 用户自动回到登录页，看到提示："登录已过期，请重新登录"

## 测试方法

### 测试场景1：路由守卫验证
```
1. 登录系统
2. 在浏览器控制台执行：localStorage.removeItem('token')
3. 刷新页面或导航到其他路由
4. 预期结果：
   - 自动重定向到登录页
   - 显示提示："登录已过期，请重新登录"
```

### 测试场景2：API请求时过期
```
1. 登录系统进入管理页面
2. 在服务器上清除或使session过期
3. 在管理页面点击加载数据按钮（收入、支出等）
4. 预期结果：
   - 显示提示："登录已过期，请重新登录"
   - 自动重定向到登录页
```

### 测试场景3：检查控制台日志
```
正常的日志输出：
【路由守卫】验证 token 有效性...
【路由守卫】验证成功，用户: {id: xxx, username: "xxx", ...}

过期的日志输出：
【路由守卫】验证 token 有效性...
【路由守卫】会话已过期，用户信息: {code: 401, message: "未登录或会话已过期", ...}
【路由守卫】收到 401 错误，会话已过期
```

## 文件变更清单

### 后端文件
- ✅ `src/main/java/com/billmanager/jizhang/controller/LoginController.java`
  - 添加 `HttpServletResponse` 参数
  - `getProfile()` 和 `getAuthProfile()` 返回401状态码

### 前端文件
- ✅ `src/main/resources/static/app/src/api/index.js`
  - 改进响应拦截器错误处理
  - 检查多个认证失败标志

- ✅ `src/main/resources/static/app/src/router/index.js`
  - 增强路由守卫验证逻辑
  - 添加错误处理和用户提示
  - 改进控制台日志

## 需要编译和部署
```bash
# 后端编译
mvn clean package

# 前端（在 app 目录）
npm run build

# 启动应用
java -jar target/jizhang-xxx.jar
```

## 验证清单
- [ ] 后端修改已编译
- [ ] 前端文件已保存
- [ ] 在开发环境测试场景1（路由守卫验证）
- [ ] 在开发环境测试场景2（API请求时过期）
- [ ] 检查控制台日志输出是否正确
- [ ] 用户收到会话过期提示信息
- [ ] 自动重定向到登录页成功
- [ ] 重新登录后可正常使用系统
