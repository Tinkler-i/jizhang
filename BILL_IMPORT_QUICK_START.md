# 账单导入功能 - 快速启动指南

## 🚀 5分钟快速开始

### 第1步：配置讯飞API凭证
```
1. 访问 https://www.xfyun.cn/
2. 注册账号并创建应用
3. 获取：App ID、API Key、API Secret
4. 编辑 src/main/resources/application.yml
```

编辑后的应该像这样：
```yaml
xunfei:
  api:
    app-id: 你的AppID
    api-key: 你的ApiKey
    api-secret: 你的ApiSecret
    image-understanding-url: wss://spark-api.xf-yun.com/v1/private/s9d79457c6/imageunderstanding
    timeout: 30000
```

### 第2步：编译后端
```bash
cd d:\Code\Java\SorceCode\jizhang
mvn clean package
```

### 第3步：启动应用
```bash
# 方式1：使用Maven启动
mvn spring-boot:run

# 方式2：运行JAR包
java -jar target/jizhang-1.0.0.jar
```

### 第4步：访问功能
```
打开浏览器访问：http://localhost:8080/jizhang/
登录后，点击左侧菜单"账单导入"开始使用
```

---

## ✅ 验证安装成功

### 后端检查
```bash
# 1. 检查依赖是否正确导入
mvn dependency:tree | grep -E "commons-codec|jackson|httpclient"

# 2. 检查配置是否正确加载
tail -f target/classes/application.yml | grep xunfei

# 3. 查看启动日志
# 应该看到类似的输出：
# [main] INFO com.billmanager.jizhang.JizhangApplication: Started JizhangApplication in 12.345 seconds
```

### 前端检查
```bash
1. 打开浏览器开发者工具 (F12)
2. 访问 http://localhost:8080/jizhang/#/bill-import
3. 应该能看到"账单导入"页面
4. 检查Network标签，确认API路由正确
```

---

## 📝 完整测试清单

### 功能测试
- [ ] 上传支付宝账单截图
- [ ] 上传微信账单截图
- [ ] 上传银行卡账单截图
- [ ] 上传模糊/低质量图片（应正确处理）
- [ ] 编辑识别结果
- [ ] 添加新的手动记录
- [ ] 删除错误的记录
- [ ] 确认导入
- [ ] 验证数据保存到数据库

### 错误处理测试
- [ ] 不上传图片直接点击识别（应提示错误）
- [ ] 上传非图片文件（应提示错误）
- [ ] 上传超大图片（应提示错误）
- [ ] 网络断开时API调用（应超时提示）
- [ ] 分类未选择时导入（应提示错误）

### 边界值测试
- [ ] 金额为0（应拒绝）
- [ ] 金额为负数（应拒绝）
- [ ] 日期为未来日期（应拒绝）
- [ ] 一张图有多条交易（应全部识别）
- [ ] 金额有小数点（应正确处理）

---

## 🔧 常见问题解决

### Q: 讯飞API返回"无效的AppID"
```
A: 检查以下项目：
   1. 应用是否已开通"图像理解"功能
   2. AppID, ApiKey, ApiSecret是否正确复制
   3. 配置是否正确reload（重启应用）
```

### Q: 图片上传后一直转圈不完成
```
A: 可能原因及解决：
   1. 网络连接问题 → 检查网络
   2. 讯飞API超时 → 增加timeout值
   3. 后端未启动 → 检查Spring Boot是否启动
   4. 查看浏览器F12 Network标签
```

### Q: 导入时提示"分类不存在"
```
A: 解决步骤：
   1. 先创建收入/支出分类
   2. 获取分类的ID
   3. 在导入确认界面选择正确的分类
```

### Q: 数据没有保存到数据库
```
A: 检查以下项目：
   1. 是否看到"导入成功"提示
   2. 检查数据库连接是否正常
   3. 检查后端日志是否有异常
   4. 确认expense/income表结构是否完整
```

---

## 📊 数据流示意

```
用户上传图片
    ↓
[前端] Base64编码
    ↓
POST /api/bill-import/recognize
    ↓
[后端] 解析请求
    ↓
XunfeiApiService 调用讯飞API
    ↓
讯飞Spark 图像理解模型
    ↓
返回JSON格式的识别结果
    ↓
[前端] 展示可编辑表格
    ↓
用户确认、修改
    ↓
POST /api/bill-import/confirm
    ↓
[后端] 验证数据
    ↓
检查分类是否存在
    ↓
批量插入 Expense/Income 表
    ↓
返回导入的ID列表
    ↓
[前端] 显示成功提示
```

---

## 📂 核心代码文件清单

```
后端代码：
✓ src/main/java/com/billmanager/jizhang/dto/
  - BillImportRequest.java
  - BillImportResponse.java
  - BillRecordDTO.java
  - BillImportConfirmRequest.java
  - BillImportConfirmResponse.java

✓ src/main/java/com/billmanager/jizhang/config/
  - XunfeiApiProperties.java

✓ src/main/java/com/billmanager/jizhang/service/
  - XunfeiApiService.java
  - BillImportService.java
  - impl/XunfeiApiServiceImpl.java
  - impl/XunfeiWebSocketClient.java
  - impl/BillImportServiceImpl.java

✓ src/main/java/com/billmanager/jizhang/controller/
  - BillImportController.java

✓ 配置文件：
  - src/main/resources/application.yml

✓ pom.xml (已更新依赖)

前端代码：
✓ src/main/resources/static/app/src/views/
  - BillImport.vue

✓ src/main/resources/static/app/src/router/
  - index.js (已添加路由)

✓ src/main/resources/static/app/src/api/
  - index.js (已添加API)

✓ src/main/resources/static/app/src/layouts/
  - MainLayout.vue (已更新菜单)
```

---

## 🎯 下一步工作

### 立即可做
- [ ] 部署讯飞API配置
- [ ] 编译并启动应用
- [ ] 进行功能验证测试
- [ ] 使用实际账单截图测试

### 后续优化
- [ ] 添加导入历史查询功能
- [ ] 实现分类自动映射
- [ ] 优化识别提示词
- [ ] 添加更多账单格式支持
- [ ] 性能测试和优化

---

## 📞 技术支持

遇到问题？检查以下资源：
- 完整实现文档：`BILL_IMPORT_IMPLEMENTATION.md`
- 讯飞API官方文档：https://www.xfyun.cn/doc/spark/ImageUnderstanding.html
- Spring Boot官方文档：https://spring.io/projects/spring-boot
- Vue.js官方文档：https://vuejs.org/

---

**实现日期:** 2026年1月10日  
**版本:** 1.0.0 MVP  
**状态:** 准备上线
