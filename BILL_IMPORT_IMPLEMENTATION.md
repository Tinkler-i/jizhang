# 账单导入功能 - 实现文档

## 📋 项目概述

本文档记录了**账单导入功能**的完整实现，该功能允许用户上传账单截图，通过讯飞大模型AI智能识别交易信息，并确认导入到系统。

---

## ✅ 已实现的功能

### 后端实现

#### 1. **DTO 数据传输对象** ✓
- `BillImportRequest.java` - 账单导入请求
- `BillImportResponse.java` - 识别响应
- `BillRecordDTO.java` - 单条账单记录
- `BillImportConfirmRequest.java` - 确认导入请求
- `BillImportConfirmResponse.java` - 导入结果响应

#### 2. **讯飞API集成** ✓
- `XunfeiApiProperties.java` - API配置读取
- `XunfeiApiService.java` - 服务接口
- `XunfeiApiServiceImpl.java` - API调用实现
- `XunfeiWebSocketClient.java` - WebSocket客户端

**核心功能：**
- 生成HMAC-SHA256授权签名
- WebSocket连接到讯飞API
- 构建标准化的提示词
- 解析JSON格式的识别结果

#### 3. **业务逻辑服务** ✓
- `BillImportService.java` - 服务接口
- `BillImportServiceImpl.java` - 业务实现

**核心功能：**
- `recognize()` - 识别账单图像
- `confirm()` - 批量导入账单
- 数据验证和分类检查
- 事务性导入（支持回滚）

#### 4. **REST API控制器** ✓
- `BillImportController.java` - RESTful接口

**暴露的API：**
```
POST /api/bill-import/recognize
- 请求：{image: "base64编码", accountType: "ALIPAY|WECHAT|BANK|UNKNOWN"}
- 响应：{code: 200, data: {records: [...]}}

POST /api/bill-import/confirm
- 请求：{records: [{type, amount, date, categoryId, description}, ...]}
- 响应：{code: 200, data: {importedIds: [...]}}
```

#### 5. **配置文件** ✓
- `application.yml` - 讯飞API配置
- `pom.xml` - 新增依赖（Jackson, HttpClient, WebSocket, Commons-Codec）

---

### 前端实现

#### 1. **路由配置** ✓
- `router/index.js` - 添加 `/bill-import` 路由

#### 2. **API调用模块** ✓
- `api/index.js` - 添加 `billImportAPI` 模块
  - `recognize(image, accountType)` - 识别接口调用
  - `confirm(records)` - 确认导入接口调用

#### 3. **主页面组件** ✓
- `views/BillImport.vue` - 完整的3步流程页面

**功能特性：**
- **第1步：上传图片**
  - 点击/拖拽上传图片
  - 图片预览
  - 账单类型选择（可选）
  - Base64编码和识别

- **第2步：确认信息**
  - 可编辑的表格显示识别结果
  - 删除错误的行
  - 添加新的手动记录
  - 分类选择（动态加载收入/支出分类）
  - 字段编辑（类型、金额、日期、说明）

- **第3步：导入完成**
  - 成功提示
  - 导入统计
  - 跳转到相应页面

#### 4. **导航菜单更新** ✓
- `layouts/MainLayout.vue` - 添加"账单导入"菜单项

---

## 🔧 技术架构

```
┌─────────────────────────────────────────┐
│  前端 (Vue.js)                          │
│  BillImport.vue (3步流程)                │
└───────────────────┬─────────────────────┘
                    │ HTTP API
                    ▼
┌─────────────────────────────────────────┐
│  Spring Boot 后端                       │
│  ┌──────────────────────────────────┐  │
│  │ BillImportController             │  │
│  │ (POST /api/bill-import/*)        │  │
│  └─────────────┬────────────────────┘  │
│                │                        │
│  ┌─────────────▼────────────────────┐  │
│  │ BillImportService                │  │
│  │ - recognize()                    │  │
│  │ - confirm()                      │  │
│  │ - 数据验证                       │  │
│  └─────────────┬────────────────────┘  │
│                │                        │
│  ┌─────────────▼────────────────────┐  │
│  │ XunfeiApiService                 │  │
│  │ - 讯飞API调用                    │  │
│  │ - WebSocket连接                  │  │
│  │ - JSON解析                       │  │
│  └─────────────┬────────────────────┘  │
│                │                        │
│                ▼                        │
│  ┌──────────────────────────────────┐  │
│  │ Expense/Income Mapper            │  │
│  │ (持久化到数据库)                  │  │
│  └──────────────────────────────────┘  │
└─────────────────────────────────────────┘
                    │
                    ▼
            ┌───────────────┐
            │  MySQL数据库  │
            │(Expense/Income)
            └───────────────┘
```

---

## 📝 使用流程

### 用户操作流程
```
1. 用户进入"账单导入"页面
   ↓
2. 上传账单截图 (JPEG/PNG)
   ↓
3. 点击"识别账单"
   ↓
4. 系统调用讯飞AI识别 (后端WebSocket)
   ↓
5. 展示识别结果 (可编辑表格)
   ↓
6. 用户修改、确认信息
   ↓
7. 点击"确认导入"
   ↓
8. 数据校验 → 分类检查 → 批量插入
   ↓
9. 导入成功提示 + 记录统计
   ↓
10. 跳转到相应管理页面查看
```

---

## ⚠️ 重要配置说明

### 1. **讯飞API凭证配置** (必需)
编辑 `src/main/resources/application.yml`：
```yaml
xunfei:
  api:
    app-id: YOUR_APP_ID           # 从讯飞申请
    api-key: YOUR_API_KEY         # 从讯飞申请
    api-secret: YOUR_API_SECRET   # 从讯飞申请
    image-understanding-url: wss://spark-api.xf-yun.com/v1/private/s9d79457c6/imageunderstanding
    timeout: 30000
```

**获取方式：**
1. 访问 https://www.xfyun.cn/
2. 注册账号，创建应用
3. 选择"讯飞Spark"→"图像理解"
4. 获取 App ID、API Key、API Secret

### 2. **依赖库**
已在 `pom.xml` 中添加：
- `jackson-databind` - JSON处理
- `httpclient5` - HTTP客户端
- `spring-boot-starter-websocket` - WebSocket支持
- `commons-codec` - Base64和加密

### 3. **数据库**
使用现有的 `expense` 和 `income` 表，无需新建表。

---

## 🧪 测试建议

### 后端测试

```bash
# 1. 单元测试 - 数据验证
POST http://localhost:8080/jizhang/api/bill-import/recognize
Content-Type: application/json

{
  "image": "base64_encoded_image_here",
  "accountType": "ALIPAY"
}

响应示例：
{
  "code": 200,
  "data": {
    "records": [
      {
        "type": "EXPENSE",
        "amount": 120.50,
        "transactionDate": "2026-01-10",
        "categoryName": "餐饮",
        "description": "午餐"
      }
    ]
  }
}

# 2. 确认导入测试
POST http://localhost:8080/jizhang/api/bill-import/confirm
Content-Type: application/json

{
  "records": [
    {
      "type": "EXPENSE",
      "amount": 120.50,
      "transactionDate": "2026-01-10",
      "categoryId": 1,
      "description": "午餐"
    }
  ]
}

响应示例：
{
  "code": 200,
  "data": {
    "importedIds": [123]
  }
}
```

### 前端测试
1. 访问 http://localhost:8080/jizhang/#/bill-import
2. 上传实际的账单截图
3. 查看识别结果
4. 修改、确认、导入
5. 验证数据是否保存到数据库

---

## 📦 提示词设计

当前集成的提示词：
```
你是一个专业的账单数据提取助手。你的任务是从账单截图中准确提取交易信息。

请从图像中提取以下信息:
- type: 交易类型，必须是 'INCOME'(收入) 或 'EXPENSE'(支出)
- amount: 交易金额，必须是数字，例如 1000.00
- transactionDate: 交易日期，格式必须是 YYYY-MM-DD
- categoryName: 交易分类，例如 '工资', '餐饮', '购物', '转账', '红包'等
- description: 交易说明或备注（可选）

重要要求:
1. 如果图像中有多条交易记录，请全部提取并返回
2. 金额必须是正数
3. 日期格式必须严格按照 YYYY-MM-DD
4. 必须返回标准的JSON数组格式，每条记录一个对象
5. 不要返回任何其他文本，只返回JSON数组

返回格式示例:
[
  {"type": "INCOME", "amount": 5000.00, "transactionDate": "2026-01-10", "categoryName": "工资", "description": "一月工资"},
  {"type": "EXPENSE", "amount": 120.50, "transactionDate": "2026-01-09", "categoryName": "餐饮", "description": "午餐"}
]
```

---

## 🔍 关键代码位置

| 文件 | 位置 | 说明 |
|------|------|------|
| 后端DTO | `src/main/java/com/billmanager/jizhang/dto/` | 5个新DTO类 |
| 配置类 | `src/main/java/com/billmanager/jizhang/config/XunfeiApiProperties.java` | API配置读取 |
| 讯飞服务 | `src/main/java/com/billmanager/jizhang/service/` | XunfeiApiService* |
| 业务服务 | `src/main/java/com/billmanager/jizhang/service/` | BillImportService* |
| 控制器 | `src/main/java/com/billmanager/jizhang/controller/BillImportController.java` | REST接口 |
| 前端路由 | `src/main/resources/static/app/src/router/index.js` | 路由配置 |
| 前端API | `src/main/resources/static/app/src/api/index.js` | billImportAPI |
| 前端页面 | `src/main/resources/static/app/src/views/BillImport.vue` | 主页面 |
| 菜单 | `src/main/resources/static/app/src/layouts/MainLayout.vue` | 导航菜单 |

---

## 🚀 后续优化方向

### 短期优化
- [ ] 添加图片压缩（减小文件大小，加快传输）
- [ ] 实现分类关键词库自动映射
- [ ] 添加识别结果缓存（防止重复识别同一图片）
- [ ] 优化错误处理和用户提示信息

### 中期优化
- [ ] 支持批量导入（一次上传多张图片）
- [ ] 添加导入历史记录查询功能
- [ ] 实现AI反馈学习（改进识别准确率）
- [ ] 添加冲重识别检测（防止导入重复记录）

### 长期优化
- [ ] 支持多种账单格式（支付宝、微信、银行卡等）
- [ ] 实现自动分类映射优化（ML训练）
- [ ] 添加导入审计日志（追踪每条导入记录来源）
- [ ] 实现与其他记账应用的数据迁移功能

---

## 🔐 安全考虑

1. **隐私保护**
   - ✓ 图像不保存到本地数据库
   - ✓ 识别完成后清理临时数据
   - ✓ API调用使用HTTPS/WSS加密

2. **数据验证**
   - ✓ 前端数据校验
   - ✓ 后端二次校验
   - ✓ 分类ID合法性检查
   - ✓ 金额、日期格式验证

3. **API安全**
   - ✓ 不在前端暴露讯飞API凭证
   - ✓ 后端中转API调用
   - ✓ 使用配置文件管理凭证
   - ✓ 请求超时控制（30秒）

4. **事务安全**
   - ✓ 批量导入使用事务管理
   - ✓ 字段级别的业务规则验证

---

## 📞 故障排查

### 问题1：讯飞API连接失败
```
错误信息：Connection refused / API调用超时
排查步骤：
1. 检查application.yml中的app-id, api-key, api-secret是否正确
2. 检查网络连接是否正常
3. 检查讯飞API是否开通了图像理解功能
4. 查看后端日志：src/main/logs/
```

### 问题2：识别结果为空
```
排查步骤：
1. 确认上传的图片质量清晰
2. 尝试更换图片格式（支持JPG/PNG）
3. 检查图片尺寸是否过小
4. 查看讯飞API的返回信息
```

### 问题3：导入失败
```
排查步骤：
1. 检查用户是否登录
2. 检查分类ID是否存在
3. 确认用户有权限导入
4. 检查数据库连接状态
```

---

## 📚 参考资源

- [讯飞Spark API文档](https://www.xfyun.cn/doc/spark/ImageUnderstanding.html)
- [Spring Boot WebSocket](https://spring.io/guides/gs/messaging-stomp-websocket/)
- [Vue.js官方文档](https://vuejs.org/)
- [项目现有代码结构](./project-structure.md)

---

**最后更新:** 2026年1月10日  
**实现状态:** MVP完成，可上线  
**预计工作量:** 前端20h + 后端15h + 测试10h = 45小时
