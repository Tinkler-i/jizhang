# 创建Session用于保持Cookies
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

# 第一步：登录
Write-Host "正在登录..."
$loginUrl = "http://localhost:8080/jizhang/api/login"
$loginBody = @{
    username = "admin"
    password = "admin123"
} | ConvertTo-Json

try {
    $loginResponse = Invoke-WebRequest -Uri $loginUrl `
        -Method POST `
        -ContentType "application/json" `
        -Body $loginBody `
        -WebSession $session `
        -UseBasicParsing
    
    $loginData = $loginResponse.Content | ConvertFrom-Json
    Write-Host "登录响应: $($loginData.message)"
    Write-Host "用户信息: $($loginData.data.username)"
} catch {
    Write-Host "登录失败: $_"
    exit
}

# 第二步：访问income-category页面
Write-Host "`n正在访问收入分类页面..."
$categoryUrl = "http://localhost:8080/jizhang/income-category"

try {
    $categoryResponse = Invoke-WebRequest -Uri $categoryUrl `
        -WebSession $session `
        -UseBasicParsing
    
    Write-Host "收入分类页面状态码: $($categoryResponse.StatusCode)"
    
    # 检查页面内容是否包含预期的内容
    if ($categoryResponse.Content -match "收入分类管理") {
        Write-Host "✓ 页面加载成功！"
    } else {
        Write-Host "✗ 页面加载失败或内容不匹配"
        Write-Host "页面内容摘录: $($categoryResponse.Content.Substring(0, 500))"
    }
} catch {
    Write-Host "访问失败: $_"
    if ($_ -match "403") {
        Write-Host "错误: 403 Forbidden"
    }
    if ($_ -match "500") {
        Write-Host "错误: 500 Internal Server Error"
    }
}

# 第三步：测试API - 获取分类列表
Write-Host "`n正在获取收入分类列表..."
$apiUrl = "http://localhost:8080/jizhang/api/income-category"

try {
    $apiResponse = Invoke-WebRequest -Uri $apiUrl `
        -WebSession $session `
        -UseBasicParsing
    
    $apiData = $apiResponse.Content | ConvertFrom-Json
    Write-Host "API响应状态: $($apiData.message)"
    Write-Host "数据行数: $($apiData.data.Count)"
} catch {
    Write-Host "API调用失败: $_"
}
