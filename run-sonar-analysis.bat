@echo off
echo Starting SonarQube Analysis...
echo.

REM Check if SonarQube is running
echo Checking SonarQube connection...
powershell -Command "try { $response = Invoke-WebRequest -Uri 'http://127.0.0.1:9000' -UseBasicParsing -TimeoutSec 5; if ($response.StatusCode -eq 200) { Write-Host 'SonarQube is accessible' -ForegroundColor Green } } catch { Write-Host 'SonarQube is not accessible. Please start Docker containers first.' -ForegroundColor Red; exit 1 }"

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo Please run: docker-compose up -d
    echo Then wait a few minutes for SonarQube to fully start
    pause
    exit /b 1
)

echo.
echo Running Maven SonarQube analysis...
mvnw.cmd clean test sonar:sonar ^
    -Dsonar.host.url=http://127.0.0.1:9000 ^
    -Dsonar.login=admin ^
    -Dsonar.password=admin ^
    -Dsonar.projectKey=cashback-security-jwt ^
    -Dsonar.projectName="Cashback Security JWT"

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo SonarQube Analysis Completed Successfully!
    echo View results at: http://localhost:9000
    echo ========================================
) else (
    echo.
    echo ========================================
    echo SonarQube Analysis Failed!
    echo Check the logs above for details
    echo ========================================
)

pause