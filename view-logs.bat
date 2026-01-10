@echo off
REM 查看Spring Boot应用日志
echo ========================================
echo   查看应用日志
echo ========================================
echo.
echo 正在查看实时日志...
echo 按 Ctrl+C 可以退出
echo.

REM 如果应用正在运行，通过PowerShell查看日志
powershell -Command "Get-Content -Path 'logs\jizhang.log' -Wait -Tail 50 2>$null; if ($LASTEXITCODE -ne 0) { Write-Host '未找到日志文件，应用可能还未启动' -ForegroundColor Yellow; Write-Host ''; Write-Host '请确认:'; Write-Host '1. 应用是否正在运行 (mvn spring-boot:run)'; Write-Host '2. 是否配置了日志输出到文件'; Write-Host ''; Write-Host '您也可以直接查看控制台输出的日志'; pause }"
