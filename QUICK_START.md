# 开发快速指南

这个文件提供最快的开始方式。详细内容请参考 `FRONTEND_RECONSTRUCTION.md`。

## ⚡ 快速开始（5分钟）

### 第1步：启动后端
```bash
# 在项目根目录
mvn spring-boot:run
```
后端会运行在 http://localhost:8080

### 第2步：启动前端开发服务器
```bash
# 打开新的终端窗口
cd src/main/resources/static/app
npm install        # 首次需要（2-3分钟）
npm run dev         # 启动开发服务器
```
前端会运行在 http://localhost:5173

### 第3步：开发修改
- 编辑 `src/views/` 下的页面文件
- 编辑 `src/components/` 下的组件
- 编辑 `src/assets/styles/` 下的样式
- 所有修改会**自动刷新**（HMR）

## 📦 构建生产版本

当开发完成后，构建生产版本并放入 Spring Boot 静态资源目录：

```bash
cd src/main/resources/static/app
npm run build
```

然后重新启动后端：
```bash
mvn clean package
java -jar target/jizhang-1.0.0.jar
```

访问 http://localhost:8080/jizhang/

## 📁 关键文件

| 文件 | 用途 |
|------|------|
| `src/router/index.js` | 定义路由 |
| `src/layouts/MainLayout.vue` | 定义导航栏和菜单 |
| `src/api/index.js` | 定义 API 调用 |
| `src/assets/styles/main.scss` | 全局样式和颜色 |

## 🔗 常见操作

### 添加新页面
1. 创建 `src/views/MyPage.vue`
2. 在 `src/router/index.js` 中添加路由
3. 在 `src/layouts/MainLayout.vue` 中添加菜单项

### 修改菜单项
编辑 `src/layouts/MainLayout.vue` 中的 `menuItems`

### 修改主题色
编辑 `src/assets/styles/main.scss` 中的颜色值

### 添加 API 端点
在 `src/api/index.js` 中添加新函数

## 🐛 常见问题

**Q: npm install 失败**
```bash
# 清空缓存后重试
npm cache clean --force
npm install
```

**Q: 端口 5173 已被占用**
```bash
# 使用其他端口
npm run dev -- --port 5174
```

**Q: API 调用失败**
- 确保后端运行在 http://localhost:8080
- 检查 `src/api/index.js` 中的 baseURL 配置

**Q: 样式不生效**
- 确保编辑的是 `.scss` 文件
- 如果修改了 CSS 文件，需要重启开发服务器

## 📚 详细文档

- [Vue 集成指南](./VUE_INTEGRATION_GUIDE.md) - 完整的集成说明
- [前端重构说明](./FRONTEND_RECONSTRUCTION.md) - 项目结构和特性
- [app/README.md](./src/main/resources/static/app/README.md) - Vue 项目文档

## ✅ 需要检查的事项

- [ ] Node.js v16+ 已安装 (`node --version`)
- [ ] npm 已安装 (`npm --version`)
- [ ] 后端能正常运行
- [ ] 能访问 http://localhost:5173

---

**提示**: 使用 VS Code 开发时，推荐安装 "Volar" 扩展以获得 Vue 3 最佳支持。
