/**
 * 报表与分析模块 - 完整实现指南
 * 
 * 本模块提供了一套完整的财务报表、分析和仪表盘功能，
 * 帮助用户全面了解和管理自己的财务状况。
 */

## 模块结构

### 后端服务

#### 1. DTO (数据传输对象)
- FinancialReport.java - 财务报表数据
- BudgetReport.java - 预算执行情况报表
- CashFlowAnalysis.java - 现金流分析
- TrendAnalysis.java - 收支趋势预测
- DashboardData.java - 仪表盘综合数据

#### 2. Service 服务层
- ReportService.java / ReportServiceImpl.java
  - generateFinancialReport() - 生成财务报表
  - generateBudgetReport() - 生成预算报表
  - generateCashFlowAnalysis() - 生成现金流分析
  - generateTrendAnalysis() - 生成趋势预测

- AnalysisService.java / AnalysisServiceImpl.java
  - getDashboardData() - 获取仪表盘数据
  - calculateFinancialHealthScore() - 计算财务健康分数
  - getFinancialRecommendation() - 获取财务建议

#### 3. Controller 控制层
- ReportController.java
  - GET /api/reports/financial - 财务报表
  - GET /api/reports/budget - 预算报表
  - GET /api/reports/cashflow - 现金流分析
  - GET /api/reports/trend - 趋势预测

- AnalysisController.java
  - GET /api/analysis/dashboard - 仪表盘数据
  - GET /api/analysis/health-score - 财务健康分数
  - GET /api/analysis/recommendation - 财务建议

### 前端界面

#### 1. 仪表盘页面 (dashboard.html)
- 关键指标卡片区 - 显示本月收入、支出、结余、预算使用率
- 时间范围汇总 - 最近30天、年累计数据
- 图表展示区 - 收支趋势、分类分析、预算执行情况
- 财务建议区 - AI生成的个性化建议
- 报表模态框 - 查看详细报表

#### 2. 样式文件 (dashboard.css)
- 响应式设计
- 现代化卡片风格
- 图表友好的颜色搭配

#### 3. 脚本文件 (dashboard.js)
- 数据加载和更新
- 图表绘制 (使用Chart.js)
- 用户交互处理

## 功能特性

### 1. 财务报表
- 统计选定时间范围内的总收入和总支出
- 计算净利润和储蓄率
- 按分类统计收入和支出
- 显示每个分类的金额、笔数和占比

### 2. 预算执行情况报表
- 展示月度预算与实际支出对比
- 计算每个分类的预算使用率
- 识别超支分类并高亮提示
- 显示整体预算执行情况

### 3. 现金流分析
- 按日期统计现金流入和流出
- 计算累计余额变化
- 分析现金流波动度
- 评估现金流健康度（优秀/良好/一般/差）

### 4. 收支趋势预测
- 基于历史数据分析收支增长趋势
- 预测未来3个月的收支情况
- 显示趋势方向（上升/下降/稳定）
- 提供置信度评估

### 5. 仪表盘综合视图
- 实时显示本月、最近30天、年累计数据
- 多种图表类型：折线图、饼图、柱状图
- 财务健康度评分（0-100分）
- 智能财务建议系统

## API 接口使用示例

### 获取仪表盘数据
```javascript
fetch('/api/analysis/dashboard')
  .then(response => response.json())
  .then(data => {
    const dashboard = data.data;
    console.log('本月收入:', dashboard.thisMonthIncome);
    console.log('本月支出:', dashboard.thisMonthExpense);
  });
```

### 获取财务报表
```javascript
const startDate = '2026-01-01';
const endDate = '2026-01-31';
fetch(`/api/reports/financial?startDate=${startDate}&endDate=${endDate}`)
  .then(response => response.json())
  .then(data => {
    const report = data.data;
    console.log('总收入:', report.totalIncome);
    console.log('总支出:', report.totalExpense);
  });
```

### 获取预算报表
```javascript
const budgetMonth = '2026-01';
fetch(`/api/reports/budget?budgetMonth=${budgetMonth}`)
  .then(response => response.json())
  .then(data => {
    const budgetReport = data.data;
    console.log('预算使用率:', budgetReport.budgetUtilizationRate);
  });
```

### 获取现金流分析
```javascript
const startDate = '2025-12-08';
const endDate = '2026-01-07';
fetch(`/api/reports/cashflow?startDate=${startDate}&endDate=${endDate}`)
  .then(response => response.json())
  .then(data => {
    const analysis = data.data;
    console.log('健康度:', analysis.healthScore);
    console.log('健康状态:', analysis.healthStatus);
  });
```

### 获取趋势分析
```javascript
fetch('/api/reports/trend?monthCount=6')
  .then(response => response.json())
  .then(data => {
    const trend = data.data;
    console.log('收入趋势:', trend.incomeTrend);
    console.log('预测数据:', trend.forecastData);
  });
```

## 数据计算方法

### 财务健康分数计算
- 基础分：60分
- 收入 > 支出：+20分
- 支出率 < 60%：+15分
- 预算执行正常：+5分
- 最近30天有收入：+5分
- 总分上限：100分

### 现金流健康度评分
- 以50分为基础
- 净现金流为正：+20分
- 波动度低：+15分
- 收入大于支出：+15分
- 总分上限：100分

### 收支趋势预测
- 基于最近6个月历史数据
- 使用简单线性增长模型
- 预测置信度：85%
- 可根据需要调整历史数据范围

## 文件清单

### Java 源代码
- src/main/java/com/billmanager/jizhang/dto/FinancialReport.java
- src/main/java/com/billmanager/jizhang/dto/BudgetReport.java
- src/main/java/com/billmanager/jizhang/dto/CashFlowAnalysis.java
- src/main/java/com/billmanager/jizhang/dto/TrendAnalysis.java
- src/main/java/com/billmanager/jizhang/dto/DashboardData.java
- src/main/java/com/billmanager/jizhang/service/ReportService.java
- src/main/java/com/billmanager/jizhang/service/impl/ReportServiceImpl.java
- src/main/java/com/billmanager/jizhang/service/AnalysisService.java
- src/main/java/com/billmanager/jizhang/service/impl/AnalysisServiceImpl.java
- src/main/java/com/billmanager/jizhang/controller/ReportController.java
- src/main/java/com/billmanager/jizhang/controller/AnalysisController.java

### 前端资源
- src/main/resources/templates/dashboard.html
- src/main/resources/static/js/dashboard.js
- src/main/resources/static/css/dashboard.css

## 功能扩展建议

### 1. 高级分析
- 支持自定义时间范围的灵活查询
- 增加更复杂的预测算法（如ARIMA、Prophet）
- 支持多种对比维度（YoY、MoM、百分比等）

### 2. 数据导出
- 支持导出PDF格式报表
- 支持导出Excel文件，包含详细数据
- 支持定时生成和发送报表

### 3. 告警机制
- 支出超预算提醒
- 异常消费模式告警
- 收入异常波动预警

### 4. 移动端适配
- 响应式设计优化
- 移动端专用界面
- 触摸友好的交互

### 5. 多用户与对比
- 支持家庭成员数据汇总
- 与历史同期对比
- 与目标值对比

## 部署说明

### 编译和打包
```bash
cd jizhang
mvn clean package
```

### 运行应用
```bash
java -jar target/jizhang-1.0.0.jar
```

### 访问仪表盘
http://localhost:8080/dashboard

## 注意事项

1. 确保数据库中有足够的历史数据，以获得准确的分析结果
2. 图表使用Chart.js库，需要网络连接加载
3. 仪表盘数据会自动刷新，建议每次登录时查看最新数据
4. 财务建议基于当前数据智能生成，仅供参考
5. 预测数据准确度取决于历史数据的质量和完整性

## 技术栈

- 后端：Spring Boot 3.2.0，MyBatis 3.0.3
- 前端：HTML5，CSS3，JavaScript (ES6+)
- 图表库：Chart.js 4.4.0
- 数据库：MySQL 8.0+
