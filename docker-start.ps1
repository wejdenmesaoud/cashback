Write-Host "===== Starting CaseCashBack Docker Environment =====" -ForegroundColor Green

Write-Host "Building and starting Docker containers..." -ForegroundColor Yellow
docker-compose up -d

Write-Host "`nServices:" -ForegroundColor Cyan
Write-Host "- Spring Boot Application: http://localhost:8080" -ForegroundColor White
Write-Host "- Swagger UI: http://localhost:8080/swagger-ui.html" -ForegroundColor White
Write-Host "- phpMyAdmin: http://localhost:8081 (Server: mysql, Username: root, Password: admin)" -ForegroundColor White

Write-Host "`nChecking container status..." -ForegroundColor Yellow
docker-compose ps

Write-Host "`nTo stop the containers, run: docker-compose down" -ForegroundColor Magenta
Write-Host "To view logs, run: docker-compose logs [service_name]" -ForegroundColor Magenta
