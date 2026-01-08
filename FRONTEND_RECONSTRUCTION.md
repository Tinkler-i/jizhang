# Vue 前端重构完成说明

## 项目重构完成 ✅

您的记账管理系统前端已经完全使用 Vue 3 + Vite 重构，实现了现代化的统一 UI 设计。

## 快速开始

### 1. 安装依赖并启动开发服务器

```bash
# 进入 Vue 项目目录
cd src/main/resources/static/app

# 安装 Node.js 依赖（首次只需要做一次）
npm install

# 启动开发服务器
npm run dev
```

然后访问 http://localhost:5173 查看应用。

### 2. 后端运行

保持 Spring Boot 后端运行（默认端口 8080）：

```bash
# 在项目根目录
mvn spring-boot:run
```

前端开发服务器会自动代理所有 `/jizhang/api` 请求到后端。

### 3. 生产构建

当开发完成后，构建生产版本：

```bash
cd src/main/resources/static/app
npm run build
```

构建输出会直接放到 `src/main/resources/static/` 目录，Spring Boot 会直接提供这些文件。

## 项目结构

```
src/main/resources/static/
├── app/                       # Vue 3 + Vite 项目
│   ├── src/
│   │   ├── assets/           # 样式资源
│   │   │   └── styles/       # SCSS 全局样式
│   │   ├── components/       # 可复用组件
│   │   │   ├── Button.vue
│   │   │   ├── Card.vue
│   │   │   ├── Modal.vue
│   │   │   ├── Input.vue
│   │   │   ├── Select.vue
│   │   │   └── Textarea.vue
│   │   ├── views/            # 页面组件
│   │   │   ├── Login.vue
│   │   │   ├── Dashboard.vue
│   │   │   ├── Income.vue
│   │   │   ├── Expense.vue
│   │   │   ├── IncomeCategory.vue
│   │   │   ├── ExpenseCategory.vue
│   │   │   ├── Budget.vue
│   │   │   ├── Report.vue
│   │   │   └── UserProfile.vue
│   │   ├── layouts/          # 布局组件
│   │   │   └── MainLayout.vue
│   │   ├── api/              # API 服务层
│   │   │   └── index.js
│   │   ├── stores/           # Pinia 状态管理
│   │   │   ├── auth.js
│   │   │   └── ui.js
│   │   ├── router/           # 路由配置
│   │   │   └── index.js
│   │   ├── App.vue           # 根组件
│   │   └── main.js           # 应用入口
│   ├── index.html            # 开发 HTML 入口
│   ├── vite.config.js        # Vite 配置
│   ├── package.json          # 依赖管理
│   └── README.md             # 项目文档
├── index.html                # 生产 HTML 入口（自动生成）
├── js/                       # 打包后的 JavaScript（自动生成）
└── css/                      # 打包后的样式（自动生成）
```

## 核心特性

### ✨ UI/UX 改进

- **统一的导航设计**：固定顶部导航栏 + 可折叠侧边栏
- **现代化视觉效果**：渐变色背景、阴影、过渡动画
- **响应式设计**：完全适配桌面、平板、手机设备
- **一致的配色方案**：紫色渐变主题 (#667eea → #764ba2)

### 🎯 功能页面

1. **仪表盘** - 关键指标卡片 + 图表展示
2. **收入/支出管理** - 完整的 CRUD 操作 + 筛选搜索
3. **分类管理** - 网格卡片布局，易于管理
4. **预算管理** - 进度条显示，视觉化预算消耗
5. **报表分析** - 分类分析 + 预算对比
6. **个人设置** - 基本信息、密码修改、通知设置

### 🔧 技术特点

- **Vue 3 Composition API** - 更好的代码组织和复用
- **Vite** - 极快的开发和构建速度
- **Vue Router 4** - 完整的路由管理
- **Pinia** - 轻量级状态管理
- **Axios** - HTTP 请求客户端
- **SCSS** - 强大的样式预处理器
- **Chart.js** - 专业图表库

## API 集成

所有 API 调用都经过统一的服务层处理：

```javascript
// 示例 API 调用
import { expenseAPI } from './api'

// 获取支出列表
const response = await expenseAPI.getList({ month: '2024-01' })

// 创建支出
await expenseAPI.create({ categoryId: 1, amount: 100, expenseDate: '2024-01-01' })

// 更新支出
await expenseAPI.update(id, { amount: 200 })

// 删除支出
await expenseAPI.delete(id)
```

**支持的 API 模块**：
- `authAPI` - 用户认证
- `incomeAPI` - 收入管理
- `expenseAPI` - 支出管理
- `incomeCategoryAPI` - 收入分类
- `expenseCategoryAPI` - 支出分类
- `budgetAPI` - 预算管理
- `reportAPI` - 报表数据
- `analysisAPI` - 分析数据

## 配置说明

### 后端配置（Spring Boot）

确保 `application.yml` 中有以下配置：

```yaml
server:
  port: 8080
  servlet:
    context-path: /jizhang

# 可选：如果不使用 Thymeleaf 模板
spring:
  thymeleaf:
    enabled: false
```

### 前端配置（Vite）

`vite.config.js` 中已配置代理，开发时自动转发 API 请求到后端：

```javascript
proxy: {
  '/jizhang/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path
  }
}
```

## 开发工作流

### 修改现有组件

```bash
cd src/main/resources/static/app
npm run dev
```

编辑 `src/views/` 或 `src/components/` 中的文件，更改会实时刷新。

### 添加新页面

1. 在 `src/views/` 中创建新的 `.vue` 文件
2. 在 `src/router/index.js` 中添加路由
3. 在 `src/layouts/MainLayout.vue` 中的菜单中添加链接

### 修改样式主题

编辑 `src/assets/styles/main.scss` 中的颜色变量。主要颜色：
- 主色: `#667eea` → `#764ba2`（紫色渐变）
- 成功: `#27ae60`（绿色）
- 危险: `#e74c3c`（红色）
- 背景: `#f5f7fa`（浅灰）

## 生产部署

1. **构建前端**
   ```bash
   cd src/main/resources/static/app
   npm run build
   ```

2. **打包 Spring Boot 应用**
   ```bash
   mvn clean package
   ```

3. **运行**
   ```bash
   java -jar target/jizhang-1.0.0.jar
   ```

4. **访问应用**
   ```
   http://localhost:8080/jizhang/
   ```

## 浏览器支持

- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## 常见问题

### Q: 如何添加新的 API 端点？
A: 在 `src/api/index.js` 中添加新的函数，例如：
```javascript
export const newModuleAPI = {
  getList: (params) => api.get('/new-module', { params }),
  create: (data) => api.post('/new-module', data),
  // ...
}
```

### Q: 如何修改菜单项？
A: 编辑 `src/layouts/MainLayout.vue` 中的 `menuItems` 数组。

### Q: 如何添加全局通知？
A: 使用 `useUIStore()` 中的 `showNotification` 方法。

### Q: 开发服务器无法启动？
A: 确保 Node.js 已安装（推荐 v16+），运行 `npm install` 重新安装依赖。

## 后续改进建议

- [ ] 实现用户认证的 token 刷新机制
- [ ] 添加数据导出功能（Excel/PDF）
- [ ] 实现实时数据同步（WebSocket）
- [ ] 添加离线模式支持
- [ ] 国际化支持（i18n）
- [ ] 更多图表类型
- [ ] 移动应用（使用 Electron 或 React Native）

## 获取帮助

详细信息请查看：
- [Vue 3 官方文档](https://vuejs.org/)
- [Vite 官方文档](https://vitejs.dev/)
- [Pinia 官方文档](https://pinia.vuejs.org/)
- [项目集成指南](./VUE_INTEGRATION_GUIDE.md)

---

**重构完成时间**: 2026年1月7日  
**技术栈**: Vue 3 + Vite + Pinia + Vue Router + Axios  
**状态**: ✅ 生产就绪
