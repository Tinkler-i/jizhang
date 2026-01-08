# Vue 前端重构集成指南

## 概述

本项目已使用 Vue 3 + Vite 完全重构前端 UI，实现了现代化的统一设计和响应式布局。

## 新架构特点

### 1. 统一的布局设计
- **顶部导航栏**: 固定在页面顶部，包含应用名称、菜单和用户操作
- **侧边栏菜单**: 可折叠的侧边栏，包含所有主要功能菜单
- **响应式设计**: 在移动设备上自适应隐藏/显示菜单
- **现代化样式**: 采用渐变色背景和阴影效果，视觉效果更佳

### 2. 组件化架构
所有页面都由可复用的 Vue 组件组成：

**基础组件** (`src/components/`)
- Button - 按钮组件（支持多种类型和状态）
- Card - 卡片组件
- Modal - 模态框
- Input - 输入框
- Select - 下拉选择框
- Textarea - 文本区域

**页面组件** (`src/views/`)
- Dashboard - 仪表盘
- Income/Expense - 收支管理
- IncomeCategory/ExpenseCategory - 分类管理
- Budget - 预算管理
- Report - 报表分析
- UserProfile - 个人设置
- Login - 登录页面

### 3. 状态管理
使用 Pinia 管理应用全局状态：
- `authStore` - 用户认证信息
- `uiStore` - UI 状态（通知、加载等）

### 4. API 层
统一的 API 服务层提供：
- 自动请求/响应拦截
- 统一的错误处理
- Token 管理
- 自动重定向到登录页面

## 开发流程

### 第一步：安装依赖

```bash
cd src/main/resources/static/app
npm install
```

### 第二步：开发模式运行

```bash
npm run dev
```

Vite 开发服务器会在 http://localhost:5173 启动，并自动代理 `/jizhang/api` 请求到后端。

### 第三步：修改样式和组件

在 `src/assets/styles/` 下修改全局样式，在 `src/components/` 下修改组件。

所有修改会实时热更新（HMR）。

### 第四步：构建生产版本

```bash
npm run build
```

构建输出会直接放到 `../` 目录（即 `src/main/resources/static/`），Spring Boot 会直接读取。

## 文件结构详解

```
src/main/resources/static/
├── app/                          # Vue 项目目录
│   ├── src/
│   │   ├── assets/
│   │   │   └── styles/          # SCSS 样式文件
│   │   ├── components/          # 可复用 Vue 组件
│   │   ├── views/               # 页面组件
│   │   ├── api/                 # API 服务
│   │   ├── stores/              # Pinia 状态管理
│   │   ├── router/              # 路由配置
│   │   ├── layouts/             # 布局组件
│   │   ├── App.vue              # 根组件
│   │   └── main.js              # 入口文件
│   ├── package.json             # 项目依赖
│   ├── vite.config.js           # Vite 配置
│   └── index.html               # 开发入口
├── index.html                   # 生产入口
├── js/
│   └── app.js                   # 打包后的应用入口（自动生成）
└── css/
    └── *.css                    # 打包后的样式（自动生成）
```

## 路由配置

路由在 `src/router/index.js` 中定义：

```javascript
// 示例
{
  path: '/expense',
  name: 'Expense',
  component: Expense,
  meta: { requiresAuth: true, title: '支出管理' }
}
```

所有需要认证的页面都添加了 `requiresAuth: true` 元数据，路由守卫会自动检查。

## API 调用示例

```javascript
// src/api/index.js 中定义的 API

// 调用获取收入列表
const response = await incomeAPI.getList({ month: '2024-01' })

// 创建新的收入记录
await incomeAPI.create({ categoryId: 1, amount: 100, incomeDate: '2024-01-01' })

// 更新收入记录
await incomeAPI.update(id, { amount: 200 })

// 删除收入记录
await incomeAPI.delete(id)
```

## 样式定制

全局样式在 `src/assets/styles/main.scss` 中定义，包括：

- 颜色主题（渐变色 #667eea → #764ba2）
- 间距系统
- 排版
- 响应式断点

要修改主题色，编辑以下文件中的颜色值：
- `src/assets/styles/main.scss`
- `src/layouts/MainLayout.vue`
- 各个页面组件

## 集成后端

确保 Spring Boot 应用的配置正确：

```yaml
# application.yml
server:
  servlet:
    context-path: /jizhang

# 关闭 Thymeleaf 模板引擎（如果使用）
spring:
  thymeleaf:
    enabled: false
```

Vue 应用会从 http://localhost:8080/jizhang/ 加载。

## 常见任务

### 添加新页面

1. 在 `src/views/` 中创建 `NewPage.vue`
2. 在 `src/router/index.js` 中添加路由
3. 在 `src/layouts/MainLayout.vue` 中的菜单中添加链接

### 修改导航菜单

编辑 `src/layouts/MainLayout.vue` 中的 `menuItems` 数组

### 修改 API 端点

编辑 `src/api/index.js` 中对应的 API 函数

### 添加新的全局状态

在 `src/stores/` 中创建新的 store，然后在需要的地方导入使用

## 浏览器开发工具

推荐安装 Vue.js DevTools 浏览器扩展，用于调试 Vue 应用和 Pinia 状态。

## 性能优化

- Vite 提供极快的开发和构建速度
- Vue 3 Composition API 提供更好的代码复用
- 代码分割自动进行，大型应用会自动分割代码块
- 样式使用 SCSS，支持变量和混合

## 部署

1. 构建生产版本: `npm run build`
2. Spring Boot 应用会自动提供 `/jizhang/` 路径下的静态文件
3. 所有非文件请求都会被重定向到 `index.html`（SPA 路由）

## 常见问题

### Q: 如何修改 API 基础路径？
A: 编辑 `src/api/index.js` 中的 `baseURL`

### Q: 如何添加新的请求头？
A: 在 `src/api/index.js` 的请求拦截器中添加

### Q: 如何自定义加载动画？
A: 编辑 `src/stores/ui.js` 中的 loading 状态

### Q: 如何实现国际化（i18n）？
A: 安装 `vue-i18n`，在 `main.js` 中配置

## 后续改进建议

1. 添加 i18n 国际化支持
2. 添加更多图表类型（报表页面可以更丰富）
3. 添加数据导出功能
4. 添加打印预览功能
5. 实现实时数据同步（WebSocket）
6. 添加离线模式支持
7. 优化移动端体验

## 技术栈

- Vue 3 - 渐进式 JavaScript 框架
- Vite - 现代化前端构建工具
- Vue Router 4 - 路由管理
- Pinia - 状态管理
- Axios - HTTP 客户端
- SCSS - 样式预处理
- Chart.js - 图表库
