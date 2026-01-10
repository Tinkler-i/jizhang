@echo off
REM 讯飞AI账单导入 - Windows快速启动脚本

echo.
echo ============================================
echo   讯飞AI账单导入功能 - 快速启动
echo ============================================
echo.

REM 1. 检查Java版本
echo ✓ 检查Java环境...
java -version
if errorlevel 1 (
    echo.
    echo ❌ 未找到Java环境，请安装Java 17+
    exit /b 1
)
echo.

REM 2. 编译项目
echo ✓ 编译项目...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo.
    echo ❌ 编译失败，请检查代码
    exit /b 1
)
echo ✅ 编译成功
echo.

REM 3. 构建JAR
echo ✓ 构建应用包...
call mvn package -DskipTests -DskipAssembly
if errorlevel 1 (
    echo.
    echo ❌ 构建失败
    exit /b 1
)
echo ✅ 构建成功: target\jizhang-1.0.0.jar
echo.

REM 4. 启动应用
echo ✓ 启动应用...
echo.
echo =========================================
echo   应用启动中...
echo =========================================
echo.
echo 访问地址: http://localhost:8080
echo.
java -jar target\jizhang-1.0.0.jar

pause
