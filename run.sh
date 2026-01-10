#!/bin/bash
# 讯飞AI账单导入 - 快速启动脚本

echo "============================================"
echo "  讯飞AI账单导入功能 - 快速启动"
echo "============================================"
echo ""

# 1. 检查Java版本
echo "✓ 检查Java环境..."
java -version
echo ""

# 2. 编译项目
echo "✓ 编译项目..."
mvn clean compile -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 编译失败，请检查代码"
    exit 1
fi
echo "✅ 编译成功"
echo ""

# 3. 构建JAR
echo "✓ 构建应用包..."
mvn package -DskipTests -DskipAssembly
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi
echo "✅ 构建成功: target/jizhang-1.0.0.jar"
echo ""

# 4. 启动应用
echo "✓ 启动应用..."
echo ""
echo "========================================="
echo "  应用启动中..."
echo "========================================="
echo ""
java -jar target/jizhang-1.0.0.jar

# 如果需要后台运行，使用:
# nohup java -jar target/jizhang-1.0.0.jar > logs/app.log 2>&1 &
