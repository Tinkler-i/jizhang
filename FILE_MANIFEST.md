# 📁 账单导入功能 - 文件清单

## 完整的文件修改清单

### 新建文件 (14个)

#### 后端 DTO 类 (5个)
```
1. src/main/java/com/billmanager/jizhang/dto/BillImportRequest.java
   - 账单导入请求对象
   - 包含：image(Base64), accountType

2. src/main/java/com/billmanager/jizhang/dto/BillImportResponse.java
   - 识别响应对象
   - 包含：records 列表

3. src/main/java/com/billmanager/jizhang/dto/BillRecordDTO.java
   - 单条账单记录
   - 包含：type, amount, date, categoryName, description

4. src/main/java/com/billmanager/jizhang/dto/BillImportConfirmRequest.java
   - 确认导入请求
   - 包含：records 列表（用户确认后的数据）

5. src/main/java/com/billmanager/jizhang/dto/BillImportConfirmResponse.java
   - 导入结果响应
   - 包含：importedIds 列表
```

#### 后端配置 (1个)
```
6. src/main/java/com/billmanager/jizhang/config/XunfeiApiProperties.java
   - 讯飞API配置属性读取类
   - 从 application.yml 读取配置
```

#### 后端服务接口 (1个)
```
7. src/main/java/com/billmanager/jizhang/service/BillImportService.java
   - 账单导入服务接口
   - 定义 recognize() 和 confirm() 方法
```

#### 后端服务实现 (3个)
```
8. src/main/java/com/billmanager/jizhang/service/XunfeiApiService.java
   - 讯飞API调用服务接口

9. src/main/java/com/billmanager/jizhang/service/impl/XunfeiApiServiceImpl.java
   - 讯飞API实现
   - 包含：HMAC签名生成, 授权URL生成, API调用, JSON解析

10. src/main/java/com/billmanager/jizhang/service/impl/XunfeiWebSocketClient.java
    - WebSocket客户端实现
    - 处理HTTP长连接到讯飞API

11. src/main/java/com/billmanager/jizhang/service/impl/BillImportServiceImpl.java
    - 业务逻辑实现
    - 包含：识别、确认、数据验证、分类检查、事务管理
```

#### 后端控制器 (1个)
```
12. src/main/java/com/billmanager/jizhang/controller/BillImportController.java
    - REST API控制器
    - 暴露 POST /api/bill-import/recognize 和 confirm 接口
```

#### 前端页面 (1个)
```
13. src/main/resources/static/app/src/views/BillImport.vue
    - 完整的账单导入页面组件
    - 3步流程：上传 → 确认 → 完成
    - 包含所有UI、交互逻辑、API调用
```

#### 文档 (3个)
```
14. BILL_IMPORT_IMPLEMENTATION.md
    - 详细的实现文档
    - 包含：功能说明、API文档、配置说明、测试建议

15. BILL_IMPORT_QUICK_START.md
    - 快速启动指南
    - 包含：5分钟快速开始、测试清单、常见问题

16. BILL_IMPORT_SUMMARY.md
    - 完整实现总结
    - 包含：交付物清单、架构概览、性能指标、后续建议
```

---

### 修改的文件 (5个)

#### 配置文件
```
1. src/main/resources/application.yml
   修改内容：
   - 添加 xunfei.api 配置段
   - 包含：app-id, api-key, api-secret, url, timeout

2. pom.xml
   修改内容：
   - 添加 jackson-databind 依赖
   - 添加 httpclient5 依赖
   - 添加 spring-boot-starter-websocket 依赖
   - 添加 commons-codec 依赖
```

#### 前端代码
```
3. src/main/resources/static/app/src/router/index.js
   修改内容：
   - 添加 import BillImport from '../views/BillImport.vue'
   - 添加路由配置：
     {
       path: '/bill-import',
       name: 'BillImport',
       component: BillImport,
       meta: { requiresAuth: true, title: '账单导入' }
     }

4. src/main/resources/static/app/src/api/index.js
   修改内容：
   - 添加 billImportAPI 对象
   - recognize(image, accountType) 方法
   - confirm(records) 方法

5. src/main/resources/static/app/src/layouts/MainLayout.vue
   修改内容：
   - 在 menuItems 数组中添加账单导入菜单项
   {
     path: '/bill-import',
     label: '账单导入',
     icon: '📸'
   }
```

---

## 文件大小统计

| 文件类型 | 数量 | 总行数 | 备注 |
|---------|------|--------|------|
| DTO类 | 5 | ~245 | 包含注释 |
| 配置类 | 1 | ~40 | 属性读取 |
| Service接口 | 1 | ~20 | 方法定义 |
| Service实现 | 3 | ~700 | 核心逻辑 |
| Controller | 1 | ~120 | REST接口 |
| WebSocket客户端 | 1 | ~200 | HTTP长连接 |
| Vue组件 | 1 | ~570 | 前端页面 |
| **后端代码合计** | **13** | **~1,895** | **包含所有代码** |
| **文档** | **3** | **~1,500** | **完整的文档** |
| **总计** | **19** | **~3,395** | **包括文档** |

---

## 关键代码片段位置

### 讯飞API签名
```java
// XunfeiApiServiceImpl.java 中的 generateAuthUrl() 方法
HmacUtils.hmacSha256Hex(secret, authString)
```

### 提示词定义
```java
// XunfeiWebSocketClient.java 中的 buildPrompt() 方法
约30行的精心设计的提示词
```

### 数据验证逻辑
```java
// BillImportServiceImpl.java 中的 validateRecord() 方法
检查金额、日期、分类等
```

### 事务管理
```java
// BillImportServiceImpl.java 的 confirm() 方法
@Transactional(rollbackFor = Exception.class)
```

### 前端API调用
```javascript
// BillImport.vue 中的 recognizeBill() 和 confirmImport() 方法
异步调用后端API，处理响应
```

### 前端表格编辑
```vue
// BillImport.vue 中的确认界面
v-for 遍历 recognizedRecords，支持编辑和删除
```

---

## 依赖关系图

```
application.yml
    ↓
XunfeiApiProperties (读取配置)
    ↓
XunfeiApiServiceImpl (实现API调用)
    ↓
XunfeiWebSocketClient (WebSocket连接)
    ↓
BillImportServiceImpl (业务逻辑)
    ↓
BillImportController (REST接口)
    ↓
前端 API 模块
    ↓
BillImport.vue (UI组件)
```

---

## 验证清单

### 确保所有文件都存在
- [ ] 5个DTO类在 dto/ 目录
- [ ] XunfeiApiProperties 在 config/ 目录
- [ ] Service 接口在 service/ 目录
- [ ] Service 实现在 service/impl/ 目录
- [ ] BillImportController 在 controller/ 目录
- [ ] BillImport.vue 在 views/ 目录
- [ ] application.yml 已更新
- [ ] pom.xml 已更新
- [ ] 路由配置已更新
- [ ] API 模块已更新
- [ ] 菜单已更新
- [ ] 3个文档文件已创建

### 确保没有遗漏
- [ ] 所有类都在正确的包路径下
- [ ] 所有依赖都已在 pom.xml 声明
- [ ] 所有配置都已在 application.yml 中
- [ ] 所有路由都已在 router 中注册
- [ ] 所有API都已在 api 模块中定义
- [ ] 菜单项已在 MainLayout 中添加

---

## 导入这些文件到IDE

### 对于IntelliJ IDEA
1. 打开项目
2. 所有Java文件会自动识别
3. pom.xml 会自动加载依赖
4. Vue文件会显示在resources下

### 对于VS Code
1. 安装 Extension Pack for Java
2. 安装 Vetur 或 Vue - Official
3. 所有文件应该被正确识别

### 确保无编译错误
```bash
# 编译后端
mvn clean compile

# 应该没有错误信息
# 如有错误，检查：
# 1. 依赖是否正确导入
# 2. Java 版本是否为 17+
# 3. 包路径是否正确
```

---

## 快速查找

### 按功能查找文件

**图片上传**
- 前端：BillImport.vue 的 `<upload-area>` 部分

**AI识别**
- 后端：XunfeiApiServiceImpl.java
- 前端：BillImport.vue 的 `recognizeBill()` 方法

**结果确认**
- 前端：BillImport.vue 的第2步（确认界面）

**数据导入**
- 后端：BillImportServiceImpl.java 的 `confirm()` 方法
- 前端：BillImport.vue 的 `confirmImport()` 方法

**数据验证**
- 后端：BillImportServiceImpl.java 的 `validateRecord()` 方法
- 前端：BillImport.vue 的表单验证代码

**错误处理**
- 后端：各类中的 try-catch 和异常处理
- 前端：各API调用的 catch 块

### 按文件查找功能

**BillImportRequest.java**
- 请求数据结构

**BillImportResponse.java**
- 识别结果数据结构

**BillRecordDTO.java**
- 单条记录数据结构

**XunfeiApiProperties.java**
- 配置读取

**XunfeiApiServiceImpl.java**
- API调用核心逻辑
- 签名生成
- JSON解析

**XunfeiWebSocketClient.java**
- WebSocket连接
- 请求构建
- 提示词定义

**BillImportServiceImpl.java**
- 业务逻辑
- 数据验证
- 数据库操作

**BillImportController.java**
- REST接口定义
- 权限检查
- 请求处理

**BillImport.vue**
- 完整的UI界面
- 用户交互逻辑
- API调用

---

## 版本控制建议

### Git 提交信息建议
```
feat: 实现账单导入功能

- 添加讯飞AI图像识别集成
- 实现3步导入流程（上传-确认-完成）
- 后端：14个新文件，共1895行代码
- 前端：1个新组件，570行代码
- 包含完整的API接口、业务逻辑和UI实现

关联文件：
- 14个新建文件
- 5个修改文件
- 3个文档文件

参考：BILL_IMPORT_SUMMARY.md
```

### .gitignore 建议
```
# 讯飞API凭证（不要提交）
application-prod.yml
.env.local
```

---

**文件清单生成日期**: 2026年1月10日  
**总文件数**: 19  
**总代码行数**: ~3,395  
**状态**: ✅ 完成
