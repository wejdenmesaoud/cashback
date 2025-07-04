@echo off
echo ===========================================
echo Testing Prometheus and Grafana Monitoring
echo ===========================================

echo Testing Spring Boot Actuator endpoints...
echo.

echo 1. Health Check:
curl -s http://localhost:8080/actuator/health
echo.
echo.

echo 2. Application Info:
curl -s http://localhost:8080/actuator/info
echo.
echo.

echo 3. Prometheus Metrics (first 20 lines):
curl -s http://localhost:8080/actuator/prometheus | head -20
echo.
echo.

echo 4. Custom Monitoring Health:
curl -s http://localhost:8080/api/monitoring/health
echo.
echo.

echo 5. Metrics Summary:
curl -s http://localhost:8080/api/monitoring/metrics/summary
echo.
echo.

echo ===========================================
echo Testing Custom Metrics Generation
echo ===========================================

echo Generating test login success metrics...
curl -X POST http://localhost:8080/api/monitoring/test/login-success
echo.

echo Generating test login failure metrics...
curl -X POST http://localhost:8080/api/monitoring/test/login-failure
echo.

echo Generating test cashback request metrics...
curl -X POST http://localhost:8080/api/monitoring/test/cashback-request
echo.

echo Generating test user registration metrics...
curl -X POST http://localhost:8080/api/monitoring/test/user-registration
echo.

echo ===========================================
echo Checking Prometheus Targets
echo ===========================================

echo Prometheus targets status:
curl -s http://localhost:9090/api/v1/targets | findstr "health"
echo.

echo ===========================================
echo Monitoring Test Complete!
echo ===========================================
echo.
echo Next steps:
echo 1. Check Prometheus at: http://localhost:9090
echo 2. View Grafana dashboard at: http://localhost:3000
echo 3. Login to Grafana with admin/admin
echo 4. Navigate to the "Cashback Application Dashboard"
echo.
pause