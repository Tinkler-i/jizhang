# 个人记账系统

基于Spring Boot 3.2.0和MySQL 8.0开发的个人记账管理系统。

## 项目特性

- 用户管理：账号登录、密码加密存储、手机验证码登录（待实现）
- 收入管理：分类管理、数据分析、统计功能
- 支出管理：分类管理、数据分析、统计功能
- 资金计划：预算添加删除、统计分析
- 税务记录：自动计税功能
- 报表分析：年度/月度报告、数据可视化

## 技术栈

- Spring Boot 3.2.0
- MySQL 8.0
- MyBatis 3.0.3
- Spring Security
- Thymeleaf
- Redis
- Lombok

## 项目结构

```
jizhang/
├── src/
│   ├── main/
│   │   ├── java/com/billmanager/jizhang/
│   │   │   ├── config/          # 配置类
│   │   │   ├── controller/      # 控制器
│   │   │   ├── dto/             # 数据传输对象
│   │   │   ├── entity/          # 实体类
│   │   │   ├── exception/       # 异常处理
│   │   │   ├── mapper/          # MyBatis Mapper
│   │   │   ├── service/         # 服务层
│   │   │   └── JizhangApplication.java
│   │   └── resources/
│   │       ├── mapper/          # MyBatis XML映射文件
│   │       ├── sql/             # SQL脚本
│   │       ├── static/          # 静态资源
│   │       ├── templates/       # Thymeleaf模板
│   │       └── application.yml  # 配置文件
└── pom.xml
```

## 快速开始

### 1. 环境要求

- JDK 17+
- Maven 3.6+
- MySQL 8.0+

### 2. 数据库配置

1. 创建MySQL数据库：
```bash
mysql -u root -p
```

2. 执行SQL脚本：
```bash
mysql -u root -p bill_manager < src/main/resources/sql/schema.sql
```

3. 修改数据库配置：
编辑 `src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/bill_manager?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: your_password
```

### 3. 运行项目

1. 使用Maven编译项目：
```bash
mvn clean install
```

2. 运行应用：
```bash
mvn spring-boot:run
```

3. 访问应用：
- 登录页面：http://localhost:8080/jizhang/login
- 默认账号：admin
- 默认密码：admin123

## 当前功能

### 已实现

- ✅ 项目基础架构搭建
- ✅ MySQL数据库设计与连接配置
- ✅ 用户实体类和Mapper
- ✅ 登录业务逻辑（BCrypt密码加密）
- ✅ 登录Controller接口
- ✅ 登录页面UI（HTML/CSS/JS）
- ✅ 登录表单验证
- ✅ 异常处理机制
- ✅ Session管理
- ✅ 首页Dashboard

### 待实现

- ⏳ 手机验证码登录
- ⏳ 密码找回功能
- ⏳ 收入管理模块
- ⏳ 支出管理模块
- ⏳ 预算管理模块
- ⏳ 税务记录模块
- ⏳ 报表分析模块

## 开发说明

### 密码加密

系统使用BCrypt算法对用户密码进行加密存储，确保密码安全。

### 登录流程

1. 用户输入用户名和密码
2. 前端进行表单验证
3. 提交到后端API
4. 后端验证用户名和密码
5. 验证成功后将用户信息存入Session
6. 跳转到Dashboard页面

### 异常处理

系统使用全局异常处理器统一处理异常，包括：
- 业务异常
- 参数验证异常
- 系统异常

## 许可证

MIT License