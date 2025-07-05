@echo off
setlocal enabledelayedexpansion

echo =====================================================
echo         CASHBACK SERVICE STRESS TEST
echo =====================================================
echo This script will generate load on your service
echo to create beautiful data visualizations in Grafana
echo =====================================================

REM Configuration
set BASE_URL=http://localhost:8080
set USERS_TO_CREATE=50
set LOGIN_ATTEMPTS=200
set API_CALLS=100
set CONCURRENT_CALLS=5

echo Starting stress test...
echo Base URL: %BASE_URL%
echo Users to create: %USERS_TO_CREATE%
echo Login attempts: %LOGIN_ATTEMPTS%
echo API calls per user: %API_CALLS%
echo.

REM Create a temporary directory for storing tokens
if not exist temp mkdir temp

echo [1/6] Creating test users...
for /L %%i in (1,1,%USERS_TO_CREATE%) do (
    echo Creating user testuser%%i...
    curl -s -X POST %BASE_URL%/api/auth/signup ^
        -H "Content-Type: application/json" ^
        -d "{\"username\": \"testuser%%i\", \"email\": \"testuser%%i@example.com\", \"password\": \"password123\"}" > temp\signup_%%i.json
    
    REM Small delay to avoid overwhelming the server
    timeout /t 1 /nobreak > nul
)

echo.
echo [2/6] Testing successful logins...
for /L %%i in (1,1,%USERS_TO_CREATE%) do (
    echo Logging in user testuser%%i...
    curl -s -X POST %BASE_URL%/api/auth/signin ^
        -H "Content-Type: application/json" ^
        -d "{\"username\": \"testuser%%i\", \"password\": \"password123\"}" > temp\login_%%i.json
    
    REM Extract token for later use (simplified - in real scenario you'd parse JSON)
    timeout /t 1 /nobreak > nul
)

echo.
echo [3/6] Testing failed logins to generate failure metrics...
for /L %%i in (1,1,30) do (
    echo Attempting failed login %%i/30...
    curl -s -X POST %BASE_URL%/api/auth/signin ^
        -H "Content-Type: application/json" ^
        -d "{\"username\": \"testuser%%i\", \"password\": \"wrongpassword\"}" > nul
    
    timeout /t 1 /nobreak > nul
)

echo.
echo [4/6] Making authenticated API calls...
REM First get a valid token
curl -s -X POST %BASE_URL%/api/auth/signin ^
    -H "Content-Type: application/json" ^
    -d "{\"username\": \"testuser1\", \"password\": \"password123\"}" > temp\auth_token.json

REM Extract token (this is simplified - you might need a JSON parser)
REM For now, we'll make calls without token to generate 401 errors and metrics

for /L %%i in (1,1,%API_CALLS%) do (
    echo Making API call %%i/%API_CALLS%...
    
    REM Test various endpoints to generate different metrics
    set /a "endpoint=%%i %% 4"
    
    if !endpoint!==0 (
        curl -s %BASE_URL%/api/test/all > nul
    ) else if !endpoint!==1 (
        curl -s %BASE_URL%/api/test/user > nul
    ) else if !endpoint!==2 (
        curl -s %BASE_URL%/api/test/mod > nul
    ) else (
        curl -s %BASE_URL%/api/test/admin > nul
    )
    
    REM Random delay between 500ms and 2s to simulate real usage
    set /a "delay=(!random! %% 3) + 1"
    timeout /t !delay! /nobreak > nul
)

echo.
echo [5/6] Generating burst traffic...
echo Creating traffic spikes for interesting visualizations...

for /L %%batch in (1,1,5) do (
    echo Burst %%batch/5: Sending %CONCURRENT_CALLS% concurrent requests...
    
    REM Launch concurrent requests
    for /L %%i in (1,1,%CONCURRENT_CALLS%) do (
        start /B curl -s %BASE_URL%/api/test/all
    )
    
    REM Wait a bit then create another burst
    timeout /t 3 /nobreak > nul
    
    REM Some successful logins in between
    for /L %%i in (1,1,5) do (
        curl -s -X POST %BASE_URL%/api/auth/signin ^
            -H "Content-Type: application/json" ^
            -d "{\"username\": \"testuser%%i\", \"password\": \"password123\"}" > nul
    )
    
    timeout /t 5 /nobreak > nul
)

echo.
echo [6/6] Mixed workload simulation...
echo Simulating real user behavior patterns...

for /L %%cycle in (1,1,10) do (
    echo Cycle %%cycle/10: Mixed operations...
    
    REM User registration
    set /a "usernum=%%cycle + %USERS_TO_CREATE%"
    curl -s -X POST %BASE_URL%/api/auth/signup ^
        -H "Content-Type: application/json" ^
        -d "{\"username\": \"mixeduser!usernum!\", \"email\": \"mixeduser!usernum!@example.com\", \"password\": \"password123\"}" > nul
    
    REM Successful login
    curl -s -X POST %BASE_URL%/api/auth/signin ^
        -H "Content-Type: application/json" ^
        -d "{\"username\": \"testuser1\", \"password\": \"password123\"}" > nul
    
    REM Failed login
    curl -s -X POST %BASE_URL%/api/auth/signin ^
        -H "Content-Type: application/json" ^
        -d "{\"username\": \"testuser1\", \"password\": \"wrongpass\"}" > nul
    
    REM API calls
    curl -s %BASE_URL%/api/test/all > nul
    curl -s %BASE_URL%/api/test/user > nul
    
    REM Random delay
    set /a "delay=(!random! %% 3) + 1"
    timeout /t !delay! /nobreak > nul
)

echo.
echo =====================================================
echo           STRESS TEST COMPLETED!
echo =====================================================
echo.
echo Check your Grafana dashboard at: http://localhost:3000
echo Check Prometheus metrics at: http://localhost:9090
echo.
echo Generated metrics include:
echo - %USERS_TO_CREATE% new user registrations
echo - ~%LOGIN_ATTEMPTS% total login attempts (success + failures)
echo - ~%API_CALLS% API endpoint calls
echo - Burst traffic patterns
echo - Mixed workload simulation
echo.
echo Your dashboard should now show:
echo - HTTP Request Rate spikes
echo - Authentication Events (success/failure)
echo - Response Time variations
echo - Active Users count
echo - JVM Memory usage patterns
echo.

REM Cleanup
if exist temp rmdir /s /q temp

echo Press any key to exit...
pause > nul