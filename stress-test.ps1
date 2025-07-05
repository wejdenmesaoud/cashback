# Cashback Service Advanced Stress Test
# PowerShell version with JWT token handling and concurrent requests

param(
    [string]$BaseUrl = "http://localhost:8080",
    [int]$UsersToCreate = 50,
    [int]$LoginAttempts = 200,
    [int]$ApiCalls = 100,
    [int]$ConcurrentCalls = 10,
    [int]$DurationMinutes = 20
)

Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "    CASHBACK SERVICE ADVANCED STRESS TEST" -ForegroundColor Yellow
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host "This script will generate realistic load patterns" -ForegroundColor Green
Write-Host "for beautiful Grafana visualizations" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan

# Configuration display
Write-Host "`nConfiguration:" -ForegroundColor Yellow
Write-Host "Base URL: $BaseUrl"
Write-Host "Users to create: $UsersToCreate"
Write-Host "Login attempts: $LoginAttempts"
Write-Host "API calls: $ApiCalls"
Write-Host "Concurrent calls: $ConcurrentCalls"
Write-Host "Duration: $DurationMinutes minutes"
Write-Host ""

# Function to make HTTP requests with error handling
function Invoke-SafeRequest {
    param($Uri, $Method = "GET", $Body = $null, $Headers = @{})
    try {
        if ($Body) {
            $Headers["Content-Type"] = "application/json"
            return Invoke-RestMethod -Uri $Uri -Method $Method -Body $Body -Headers $Headers -ErrorAction Stop
        } else {
            return Invoke-RestMethod -Uri $Uri -Method $Method -Headers $Headers -ErrorAction Stop
        }
    } catch {
        Write-Host "Request failed: $($_.Exception.Message)" -ForegroundColor Red
        return $null
    }
}

# Function to extract JWT token from response
function Get-JwtToken {
    param($Response)
    if ($Response -and $Response.accessToken) {
        return $Response.accessToken
    }
    return $null
}

Write-Host "[1/7] Creating test users..." -ForegroundColor Green
$CreatedUsers = @()
for ($i = 1; $i -le $UsersToCreate; $i++) {
    Write-Progress -Activity "Creating users" -Status "User $i of $UsersToCreate" -PercentComplete (($i / $UsersToCreate) * 100)
    
    $userData = @{
        username = "stressuser$i"
        email = "stressuser$i@example.com"
        password = "StressTest123!"
    } | ConvertTo-Json
    
    $response = Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signup" -Method POST -Body $userData
    if ($response) {
        $CreatedUsers += "stressuser$i"
    }
    
    Start-Sleep -Milliseconds 200
}
Write-Host "Created $($CreatedUsers.Count) users successfully" -ForegroundColor Green

Write-Host "`n[2/7] Testing successful logins and collecting tokens..." -ForegroundColor Green
$ValidTokens = @()
foreach ($username in $CreatedUsers[0..9]) {  # Get tokens for first 10 users
    $loginData = @{
        username = $username
        password = "StressTest123!"
    } | ConvertTo-Json
    
    $response = Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signin" -Method POST -Body $loginData
    $token = Get-JwtToken $response
    if ($token) {
        $ValidTokens += $token
        Write-Host "Got token for $username" -ForegroundColor Gray
    }
    Start-Sleep -Milliseconds 500
}

Write-Host "`n[3/7] Generating failed login attempts..." -ForegroundColor Green
for ($i = 1; $i -le 50; $i++) {
    Write-Progress -Activity "Failed logins" -Status "Attempt $i of 50" -PercentComplete (($i / 50) * 100)
    
    $failData = @{
        username = "stressuser$i"
        password = "WrongPassword123!"
    } | ConvertTo-Json
    
    Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signin" -Method POST -Body $failData | Out-Null
    Start-Sleep -Milliseconds 300
}

Write-Host "`n[4/7] Making authenticated API calls..." -ForegroundColor Green
$endpoints = @("/api/test/all", "/api/test/user", "/api/test/mod", "/api/test/admin")

for ($i = 1; $i -le $ApiCalls; $i++) {
    Write-Progress -Activity "API calls" -Status "Call $i of $ApiCalls" -PercentComplete (($i / $ApiCalls) * 100)
    
    $endpoint = $endpoints[$i % $endpoints.Length]
    
    # Use random token if available
    if ($ValidTokens.Count -gt 0) {
        $token = $ValidTokens[$i % $ValidTokens.Count]
        $headers = @{ "Authorization" = "Bearer $token" }
        Invoke-SafeRequest -Uri "$BaseUrl$endpoint" -Headers $headers | Out-Null
    } else {
        # Make unauthenticated call (will generate 401 errors)
        Invoke-SafeRequest -Uri "$BaseUrl$endpoint" | Out-Null
    }
    
    # Random delay between 100ms and 1s
    Start-Sleep -Milliseconds (Get-Random -Minimum 100 -Maximum 1000)
}

Write-Host "`n[5/7] Generating concurrent burst traffic..." -ForegroundColor Green
for ($burst = 1; $burst -le 5; $burst++) {
    Write-Host "Burst $burst/5: Launching $ConcurrentCalls concurrent requests..." -ForegroundColor Yellow
    
    # Launch concurrent jobs
    $jobs = @()
    for ($j = 1; $j -le $ConcurrentCalls; $j++) {
        $job = Start-Job -ScriptBlock {
            param($url, $token)
            try {
                if ($token) {
                    $headers = @{ "Authorization" = "Bearer $token" }
                    Invoke-RestMethod -Uri "$url/api/test/all" -Headers $headers -TimeoutSec 10
                } else {
                    Invoke-RestMethod -Uri "$url/api/test/all" -TimeoutSec 10
                }
            } catch {
                # Ignore errors for stress testing
            }
        } -ArgumentList $BaseUrl, ($ValidTokens | Get-Random)
        $jobs += $job
    }
    
    # Wait for jobs to complete (max 15 seconds)
    $jobs | Wait-Job -Timeout 15 | Out-Null
    $jobs | Remove-Job -Force
    
    # Add some successful logins between bursts
    for ($k = 1; $k -le 3; $k++) {
        $user = $CreatedUsers | Get-Random
        $loginData = @{
            username = $user
            password = "StressTest123!"
        } | ConvertTo-Json
        Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signin" -Method POST -Body $loginData | Out-Null
    }
    
    Start-Sleep -Seconds 3
}

Write-Host "`n[6/7] Simulating realistic user patterns..." -ForegroundColor Green
for ($cycle = 1; $cycle -le 20; $cycle++) {
    Write-Progress -Activity "User simulation" -Status "Cycle $cycle of 20" -PercentComplete (($cycle / 20) * 100)
    
    # Simulate a user session
    $user = $CreatedUsers | Get-Random
    
    # 1. Login
    $loginData = @{
        username = $user
        password = "StressTest123!"
    } | ConvertTo-Json
    $response = Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signin" -Method POST -Body $loginData
    $token = Get-JwtToken $response
    
    if ($token) {
        # 2. Make several authenticated calls (simulating user activity)
        $headers = @{ "Authorization" = "Bearer $token" }
        
        for ($activity = 1; $activity -le (Get-Random -Minimum 3 -Maximum 8); $activity++) {
            $endpoint = $endpoints | Get-Random
            Invoke-SafeRequest -Uri "$BaseUrl$endpoint" -Headers $headers | Out-Null
            Start-Sleep -Milliseconds (Get-Random -Minimum 500 -Maximum 2000)
        }
    }
    
    # Random session break
    Start-Sleep -Milliseconds (Get-Random -Minimum 1000 -Maximum 3000)
}

Write-Host "`n[7/7] Continuous load simulation..." -ForegroundColor Green
$endTime = (Get-Date).AddMinutes($DurationMinutes)
$counter = 0

Write-Host "Running continuous load for $DurationMinutes minutes..." -ForegroundColor Yellow
Write-Host "Press Ctrl+C to stop early" -ForegroundColor Gray

while ((Get-Date) -lt $endTime) {
    $counter++
    
    # Mix of operations
    switch ($counter % 4) {
        0 {
            # New user registration
            $newUserNum = (Get-Random -Minimum 1000 -Maximum 9999)
            $userData = @{
                username = "autouser$newUserNum"
                email = "autouser$newUserNum@example.com"
                password = "AutoPass123!"
            } | ConvertTo-Json
            Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signup" -Method POST -Body $userData | Out-Null
        }
        1 {
            # Successful login
            $user = $CreatedUsers | Get-Random
            $loginData = @{
                username = $user
                password = "StressTest123!"
            } | ConvertTo-Json
            Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signin" -Method POST -Body $loginData | Out-Null
        }
        2 {
            # Failed login
            $failData = @{
                username = ($CreatedUsers | Get-Random)
                password = "WrongPassword"
            } | ConvertTo-Json
            Invoke-SafeRequest -Uri "$BaseUrl/api/auth/signin" -Method POST -Body $failData | Out-Null
        }
        3 {
            # API call
            $endpoint = $endpoints | Get-Random
            if ($ValidTokens.Count -gt 0 -and (Get-Random -Maximum 2)) {
                $headers = @{ "Authorization" = "Bearer $($ValidTokens | Get-Random)" }
                Invoke-SafeRequest -Uri "$BaseUrl$endpoint" -Headers $headers | Out-Null
            } else {
                Invoke-SafeRequest -Uri "$BaseUrl$endpoint" | Out-Null
            }
        }
    }
    
    # Show progress
    $remaining = $endTime - (Get-Date)
    Write-Progress -Activity "Continuous load" -Status "Time remaining: $($remaining.ToString('mm\:ss'))" -PercentComplete ((($DurationMinutes * 60 - $remaining.TotalSeconds) / ($DurationMinutes * 60)) * 100)
    
    # Variable delay to create realistic patterns
    Start-Sleep -Milliseconds (Get-Random -Minimum 200 -Maximum 1500)
}

Write-Host "`n=============================================" -ForegroundColor Cyan
Write-Host "         STRESS TEST COMPLETED!" -ForegroundColor Green
Write-Host "=============================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Check your dashboards:" -ForegroundColor Yellow
Write-Host "   Grafana: http://localhost:3000" -ForegroundColor White
Write-Host "   Prometheus: http://localhost:9090" -ForegroundColor White
Write-Host ""
Write-Host "📈 Generated metrics:" -ForegroundColor Yellow
Write-Host "   • $($CreatedUsers.Count) user registrations" -ForegroundColor White
Write-Host "   • $($ValidTokens.Count) successful authentications" -ForegroundColor White
Write-Host "   • ~$($counter) total API interactions" -ForegroundColor White
Write-Host "   • Burst traffic patterns" -ForegroundColor White
Write-Host "   • Realistic user session simulations" -ForegroundColor White
Write-Host "   • Mixed success/failure scenarios" -ForegroundColor White
Write-Host ""
Write-Host "🎯 Your Grafana dashboard should now show:" -ForegroundColor Yellow
Write-Host "   • Beautiful HTTP request rate patterns" -ForegroundColor White
Write-Host "   • Authentication success/failure trends" -ForegroundColor White
Write-Host "   • Response time variations" -ForegroundColor White
Write-Host "   • JVM memory usage under load" -ForegroundColor White
Write-Host "   • Active user count changes" -ForegroundColor White
Write-Host ""