@echo off
echo ===========================================
echo Starting Cashback Application with Monitoring
echo ===========================================

echo Stopping any existing containers...
docker-compose down

echo Building application image...
docker-compose build app

echo Starting the complete monitoring stack...
docker-compose up -d mysql
timeout /t 10
docker-compose up -d app
timeout /t 15
docker-compose up -d prometheus grafana
timeout /t 10
docker-compose up -d sonarqube jenkins nexus phpmyadmin

echo.
echo ===========================================
echo Application Stack Status
echo ===========================================
echo Cashback API:      http://localhost:8080
echo Swagger UI:        http://localhost:8080/swagger-ui.html
echo Actuator Health:   http://localhost:8080/actuator/health
echo Prometheus:        http://localhost:9090
echo Grafana:           http://localhost:3000 (admin/admin)
echo Jenkins:           http://localhost:8081 (admin/admin)
echo SonarQube:         http://localhost:9000 (admin/admin)
echo phpMyAdmin:        http://localhost:8082 (root/admin)
echo Nexus:             http://localhost:8083 (admin/admin123)
echo.

echo Waiting for services to be ready...
timeout /t 30

echo Checking service status...
docker-compose ps

echo.
echo ===========================================
echo Monitoring Setup Complete!
echo ===========================================
echo.
echo To test metrics:
echo 1. Visit http://localhost:8080/api/monitoring/health
echo 2. Visit http://localhost:8080/actuator/prometheus
echo 3. Check Grafana dashboard at http://localhost:3000
echo.
echo To view logs: docker-compose logs -f [service-name]
echo To stop all: docker-compose down
echo.
pause