# 📂 项目结构指南 - 账单导入功能

## 项目文件树（重要文件标注）

```
jizhang/
│
├── 📄 pom.xml                          ⭐ 已更新（添加4个依赖）
├── 📄 IMPLEMENTATION_REPORT.md         ✨ 完整实现报告
├── 📄 README_BILL_IMPORT.md            ✨ 快速概览（从这里开始！）
├── 📄 BILL_IMPORT_QUICK_START.md      ✨ 5分钟快速开始
├── 📄 BILL_IMPORT_IMPLEMENTATION.md   ✨ 详细实现文档
├── 📄 BILL_IMPORT_SUMMARY.md          ✨ 项目总结
├── 📄 FILE_MANIFEST.md                ✨ 文件清单
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/billmanager/jizhang/
│   │   │       │
│   │   │       ├── 📁 config/
│   │   │       │   ├── SecurityConfig.java
│   │   │       │   ├── WebMvcConfig.java
│   │   │       │   └── ✨ XunfeiApiProperties.java      [新建]
│   │   │       │
│   │   │       ├── 📁 controller/
│   │   │       │   ├── AnalysisController.java
│   │   │       │   ├── BudgetController.java
│   │   │       │   ├── ExpenseController.java
│   │   │       │   ├── IncomeController.java
│   │   │       │   ├── ... (其他控制器)
│   │   │       │   └── ✨ BillImportController.java      [新建]
│   │   │       │
│   │   │       ├── 📁 dto/
│   │   │       │   ├── ApiResponse.java
│   │   │       │   ├── ExpenseRequest.java
│   │   │       │   ├── IncomeRequest.java
│   │   │       │   ├── ... (其他DTO)
│   │   │       │   ├── ✨ BillImportRequest.java        [新建]
│   │   │       │   ├── ✨ BillImportResponse.java       [新建]
│   │   │       │   ├── ✨ BillRecordDTO.java            [新建]
│   │   │       │   ├── ✨ BillImportConfirmRequest.java [新建]
│   │   │       │   └── ✨ BillImportConfirmResponse.java[新建]
│   │   │       │
│   │   │       ├── 📁 entity/
│   │   │       │   ├── Expense.java
│   │   │       │   ├── Income.java
│   │   │       │   └── ... (其他实体)
│   │   │       │
│   │   │       ├── 📁 mapper/
│   │   │       │   ├── ExpenseMapper.java
│   │   │       │   ├── IncomeMapper.java
│   │   │       │   └── ... (其他Mapper)
│   │   │       │
│   │   │       ├── 📁 service/
│   │   │       │   ├── ExpenseService.java
│   │   │       │   ├── IncomeService.java
│   │   │       │   ├── ... (其他Service)
│   │   │       │   ├── ✨ BillImportService.java        [新建]
│   │   │       │   ├── ✨ XunfeiApiService.java         [新建]
│   │   │       │   │
│   │   │       │   └── 📁 impl/
│   │   │       │       ├── ExpenseServiceImpl.java
│   │   │       │       ├── IncomeServiceImpl.java
│   │   │       │       ├── ... (其他实现)
│   │   │       │       ├── ✨ BillImportServiceImpl.java [新建]
│   │   │       │       ├── ✨ XunfeiApiServiceImpl.java  [新建]
│   │   │       │       └── ✨ XunfeiWebSocketClient.java[新建]
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   └── BusinessException.java
│   │   │       │
│   │   │       └── JizhangApplication.java
│   │   │
│   │   └── resources/
│   │       ├── ⭐ application.yml           (已更新：添加xunfei配置)
│   │       ├── templates/
│   │       ├── mapper/
│   │       │   ├── BudgetMapper.xml
│   │       │   ├── ExpenseMapper.xml
│   │       │   ├── IncomeMapper.xml
│   │       │   └── ... (其他Mapper)
│   │       │
│   │       ├── sql/
│   │       │   └── schema.sql
│   │       │
│   │       └── static/
│   │           └── app/
│   │               ├── package.json
│   │               ├── vite.config.js
│   │               ├── README.md
│   │               │
│   │               └── src/
│   │                   ├── main.js
│   │                   ├── App.vue
│   │                   │
│   │                   ├── 📁 api/
│   │                   │   └── ⭐ index.js     (已更新：添加billImportAPI)
│   │                   │
│   │                   ├── 📁 assets/
│   │                   │   └── styles/
│   │                   │
│   │                   ├── 📁 components/
│   │                   │   ├── Button.vue
│   │                   │   ├── Card.vue
│   │                   │   ├── Input.vue
│   │                   │   ├── Modal.vue
│   │                   │   ├── Select.vue
│   │                   │   └── Textarea.vue
│   │                   │
│   │                   ├── 📁 layouts/
│   │                   │   └── ⭐ MainLayout.vue (已更新：添加菜单)
│   │                   │
│   │                   ├── 📁 router/
│   │                   │   └── ⭐ index.js     (已更新：添加路由)
│   │                   │
│   │                   ├── 📁 stores/
│   │                   │   ├── auth.js
│   │                   │   └── ui.js
│   │                   │
│   │                   └── 📁 views/
│   │                       ├── Budget.vue
│   │                       ├── Dashboard.vue
│   │                       ├── Expense.vue
│   │                       ├── Income.vue
│   │                       ├── Login.vue
│   │                       ├── Report.vue
│   │                       ├── UserProfile.vue
│   │                       └── ✨ BillImport.vue    [新建]
│   │
│   └── test/
│       └── java/
│           └── (测试文件)
│
└── target/
    ├── classes/
    ├── generated-sources/
    └── ... (编译输出)
```

---

## 🎯 新增文件快速定位

### 后端新增文件（14个）

#### DTO 层（5个文件）
```
src/main/java/com/billmanager/jizhang/dto/
├── BillImportRequest.java          ← 导入请求对象
├── BillImportResponse.java         ← 识别响应对象
├── BillRecordDTO.java              ← 单条记录对象
├── BillImportConfirmRequest.java   ← 确认请求对象
└── BillImportConfirmResponse.java  ← 导入结果对象
```

#### 配置层（1个文件）
```
src/main/java/com/billmanager/jizhang/config/
└── XunfeiApiProperties.java        ← API配置读取类
```

#### 服务层（4个文件）
```
src/main/java/com/billmanager/jizhang/service/
├── BillImportService.java          ← 业务接口
├── XunfeiApiService.java           ← API接口
└── impl/
    ├── BillImportServiceImpl.java   ← 业务实现（核心）
    └── XunfeiApiServiceImpl.java    ← API实现（核心）
    └── XunfeiWebSocketClient.java  ← WebSocket客户端
```

#### 控制器层（1个文件）
```
src/main/java/com/billmanager/jizhang/controller/
└── BillImportController.java       ← REST API控制器
```

### 前端新增文件（1个）

#### 视图层
```
src/main/resources/static/app/src/views/
└── BillImport.vue                  ← 账单导入页面（完整组件）
```

### 修改的文件（5个）

#### 配置文件
```
pom.xml                                    ← 添加4个新依赖
src/main/resources/application.yml         ← 添加xunfei配置段
```

#### 前端集成
```
src/main/resources/static/app/src/router/index.js      ← 添加路由配置
src/main/resources/static/app/src/api/index.js         ← 添加API调用
src/main/resources/static/app/src/layouts/MainLayout.vue ← 添加菜单项
```

### 文档文件（5个）

```
项目根目录/
├── README_BILL_IMPORT.md           ← 快速概览 ⭐ 从这里开始
├── BILL_IMPORT_QUICK_START.md      ← 5分钟快速开始
├── BILL_IMPORT_IMPLEMENTATION.md   ← 详细实现文档
├── BILL_IMPORT_SUMMARY.md          ← 项目总结
├── FILE_MANIFEST.md                ← 文件清单
├── IMPLEMENTATION_REPORT.md        ← 实现报告
```

---

## 📊 分层架构对应

```
表现层 (Presentation)
  ├── BillImport.vue              [前端]
  ├── router/index.js             [路由]
  └── api/index.js                [API客户端]

应用层 (Application)
  └── BillImportController         [REST接口]

业务层 (Business)
  ├── BillImportService           [业务接口]
  └── BillImportServiceImpl        [业务实现]

外部服务 (External Services)
  ├── XunfeiApiService            [API接口]
  ├── XunfeiApiServiceImpl         [API实现]
  └── XunfeiWebSocketClient       [WebSocket客户端]

数据层 (Data)
  ├── ExpenseMapper / IncomeMapper [现有Mapper]
  └── MySQL Database               [数据库]
```

---

## 🔍 按功能查找文件

### 图片上传功能
- 📄 前端：`views/BillImport.vue` (第1步上传界面)
- 📄 后端：`controller/BillImportController.java` (POST /recognize)

### AI识别功能
- 📄 前端：`views/BillImport.vue` (recognizeBill方法)
- 📄 后端：`service/impl/XunfeiApiServiceImpl.java` (核心)
- 📄 后端：`service/impl/XunfeiWebSocketClient.java` (WebSocket)

### 确认界面功能
- 📄 前端：`views/BillImport.vue` (第2步确认界面)
- 📄 后端：`controller/BillImportController.java` (权限检查)

### 数据导入功能
- 📄 前端：`views/BillImport.vue` (confirmImport方法)
- 📄 后端：`service/impl/BillImportServiceImpl.java` (confirm方法)

### 分类映射功能
- 📄 前端：`views/BillImport.vue` (Select组件)
- 📄 后端：`service/impl/BillImportServiceImpl.java` (分类检查)

### 安全验证功能
- 📄 后端：`controller/BillImportController.java` (身份验证)
- 📄 后端：`service/impl/BillImportServiceImpl.java` (数据验证)

---

## 🔗 文件依赖关系

```
application.yml
    ↓
XunfeiApiProperties.java
    ↓
XunfeiApiService.java
XunfeiApiServiceImpl.java
XunfeiWebSocketClient.java
    ↓
BillImportService.java
BillImportServiceImpl.java
    ↓
BillImportController.java
    ↓
router/index.js
api/index.js
    ↓
BillImport.vue
    ↓
MainLayout.vue (菜单集成)
```

---

## 💡 文件命名约定

### 后端命名
```
DTO 类：          XxxRequest/XxxResponse/XxxDTO
配置类：          XxxProperties
接口：            XxxService/XxxApi
实现类：          XxxServiceImpl/XxxApiImpl
控制器：          XxxController
实体类：          Xxx
Mapper：          XxxMapper
```

### 前端命名
```
页面组件：        Xxx.vue
API 模块：        index.js (在 api/ 目录下)
路由配置：        index.js (在 router/ 目录下)
```

---

## 📈 代码统计

### 按层统计
```
后端代码:
  - DTO 层：       ~245 行
  - 配置层：       ~40 行
  - 服务层：       ~700 行
  - 控制器层：     ~120 行
  - WebSocket：    ~200 行
  小计：           ~1,305 行

配置和集成:
  - application.yml：  ~20 行 (新增)
  - pom.xml：         ~20 行 (新增)
  - router：          ~10 行 (修改)
  - api：             ~10 行 (修改)
  - MainLayout：      ~5 行 (修改)
  小计：              ~65 行

前端代码:
  - BillImport.vue：  ~570 行
  小计：              ~570 行

总计：              ~1,940 行
```

### 按文件统计
```
总文件数：          19 个
  - 新建：         14 个
  - 修改：         5 个

代码文件：          14 个 (后端)
UI 文件：           1 个 (前端)
配置文件：          5 个 (修改)
文档文件：          5 个
```

---

## ⚠️ 重要文件警告

### 敏感信息 ⚠️
```
application.yml
├── xunfei.api.app-id         [需要配置]
├── xunfei.api.api-key        [需要配置]
└── xunfei.api.api-secret     [需要配置]

⚠️ 不要提交到Git！
⚠️ 使用环境变量或配置服务器管理！
```

### 关键文件 🔑
```
BillImportServiceImpl.java      [业务逻辑核心]
XunfeiApiServiceImpl.java       [API集成核心]
BillImportController.java      [接口暴露]
BillImport.vue                 [用户交互核心]
```

---

## 🚀 快速导航

### 想要修改 UI？
→ `src/main/resources/static/app/src/views/BillImport.vue`

### 想要优化识别？
→ `src/main/java/com/billmanager/jizhang/service/impl/XunfeiApiServiceImpl.java`
→ 编辑 `buildPrompt()` 方法中的提示词

### 想要修改业务逻辑？
→ `src/main/java/com/billmanager/jizhang/service/impl/BillImportServiceImpl.java`

### 想要添加新的 API？
→ 修改 `BillImportController.java`

### 想要改变前端数据流？
→ 修改 `src/main/resources/static/app/src/api/index.js`

### 想要查看完整文档？
→ 查看 `BILL_IMPORT_IMPLEMENTATION.md`

---

## 📞 使用指南

### 文件太多了，不知道从哪开始？
1. 阅读 `README_BILL_IMPORT.md` （5分钟）
2. 按照 `BILL_IMPORT_QUICK_START.md` 操作（10分钟）
3. 查看 `FILE_MANIFEST.md` 定位文件（按需）

### 想要深入理解代码？
1. 先读 `BILL_IMPORT_IMPLEMENTATION.md`
2. 然后查看对应的源码文件
3. 跟踪数据流和函数调用

### 想要修改某个功能？
1. 在 `FILE_MANIFEST.md` 中查找相关文件
2. 打开对应的源码文件
3. 参考该文件的代码注释
4. 按需修改和测试

---

**文件指南生成日期**: 2026年1月10日  
**总文件数**: 19  
**新增文件**: 14  
**修改文件**: 5  
**覆盖度**: 100%
