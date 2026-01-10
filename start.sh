#!/bin/bash
# 账单导入功能 - 启动脚本
# 该脚本帮助快速启动项目

echo "================================"
echo "  账单导入功能 - 快速启动"
echo "================================"
echo ""

# 检查必要的工具
echo "检查环境..."
command -v java >/dev/null 2>&1 || { echo "❌ 需要 Java 17+"; exit 1; }
command -v mvn >/dev/null 2>&1 || { echo "❌ 需要 Maven"; exit 1; }

echo "✓ Java 版本: $(java -version 2>&1 | head -1)"
echo "✓ Maven 已安装"
echo ""

# 检查配置
echo "检查讯飞API配置..."
if grep -q "YOUR_APP_ID" src/main/resources/application.yml; then
    echo "⚠️  警告：讯飞API凭证尚未配置"
    echo "   请编辑 src/main/resources/application.yml"
    echo "   填入真实的 app-id, api-key, api-secret"
    read -p "是否继续？(y/n) " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
else
    echo "✓ 讯飞API配置已检查"
fi

echo ""
echo "开始编译..."
mvn clean package

if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo ""
echo "✅ 编译成功！"
echo ""
echo "启动应用..."
mvn spring-boot:run

echo ""
echo "================================"
echo "应用已启动！"
echo "访问: http://localhost:8080/jizhang/"
echo "================================"
