# Vue 3 前端项目

这是使用 Vue 3 + Vite 重构的现代化前端项目。

## 项目结构

```
app/
├── src/
│   ├── assets/           # 静态资源和全局样式
│   │   └── styles/       # SCSS 样式文件
│   ├── components/       # 可复用 Vue 组件
│   ├── views/           # 页面组件
│   ├── api/             # API 服务层
│   ├── stores/          # Pinia 状态管理
│   ├── router/          # Vue Router 路由配置
│   ├── layouts/         # 布局组件
│   ├── App.vue          # 根组件
│   └── main.js          # 应用入口
├── index.html           # HTML 入口文件
├── vite.config.js       # Vite 配置
└── package.json         # 项目依赖
```

## 开发

### 安装依赖

```bash
cd src/main/resources/static/app
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问 http://localhost:5173 进行开发

### 构建生产版本

```bash
npm run build
```

构建输出会被放到 `src/main/resources/static/` 目录下，与 Spring Boot 的静态资源路径相同。

## 功能特点

- ✅ 现代化的 Vue 3 组件式架构
- ✅ 统一的顶部导航栏和侧边栏菜单
- ✅ 响应式设计，支持移动端
- ✅ 完整的表单验证
- ✅ API 请求拦截和错误处理
- ✅ Pinia 状态管理
- ✅ Chart.js 图表支持
- ✅ 模态框、卡片等可复用组件

## 页面列表

- 📊 仪表盘 (Dashboard)
- 💵 收入管理 (Income)
- 📝 支出管理 (Expense)
- 🏷️ 收入分类 (IncomeCategory)
- 🏷️ 支出分类 (ExpenseCategory)
- 💹 预算管理 (Budget)
- 📈 报表分析 (Report)
- ⚙️ 个人设置 (UserProfile)
- 🔓 登录页面 (Login)

## API 集成

所有 API 调用都通过 `src/api/index.js` 统一管理：

- 认证相关: `authAPI`
- 收入管理: `incomeAPI`
- 支出管理: `expenseAPI`
- 分类管理: `incomeCategoryAPI`, `expenseCategoryAPI`
- 预算管理: `budgetAPI`
- 报表分析: `reportAPI`, `analysisAPI`

## 样式系统

使用 SCSS 编写样式，分层管理：

- `main.scss` - 全局样式和工具类
- `button.scss` - 按钮组件样式
- `card.scss` - 卡片组件样式
- `form.scss` - 表单元素样式
- `modal.scss` - 模态框样式
- `table.scss` - 表格样式

## 浏览器支持

- Chrome (最新)
- Firefox (最新)
- Safari (最新)
- Edge (最新)
