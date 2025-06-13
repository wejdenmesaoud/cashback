@echo off
echo ===== Starting CaseCashBack Docker Environment =====

echo Building and starting Docker containers...
docker-compose up -d

echo.
echo Services:
echo - Spring Boot Application: http://localhost:8080
echo - Swagger UI: http://localhost:8080/swagger-ui.html
echo - phpMyAdmin: http://localhost:8081 (Server: mysql, Username: root, Password: admin)
echo.

echo Checking container status...
docker-compose ps

echo.
echo To stop the containers, run: docker-compose down
echo To view logs, run: docker-compose logs [service_name]
echo.
