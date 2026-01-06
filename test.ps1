$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

Write-Host "Step 1: Login..."
$loginUrl = "http://localhost:8080/jizhang/api/login"
$loginBody = @{ username = "admin"; password = "admin123" } | ConvertTo-Json

$loginResponse = Invoke-WebRequest -Uri $loginUrl -Method POST -ContentType "application/json" -Body $loginBody -WebSession $session -UseBasicParsing
$loginData = $loginResponse.Content | ConvertFrom-Json
Write-Host "Login: $($loginData.message)"

Write-Host "`nStep 2: Access income-category page..."
$categoryUrl = "http://localhost:8080/jizhang/income-category"

$categoryResponse = Invoke-WebRequest -Uri $categoryUrl -WebSession $session -UseBasicParsing
Write-Host "Status: $($categoryResponse.StatusCode)"

if ($categoryResponse.Content -match "income-category") {
    Write-Host "Success!"
} else {
    Write-Host "Failed - content:"
    Write-Host $categoryResponse.Content.Substring(0, 300)
}
