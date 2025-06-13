Write-Host "===== Stopping CaseCashBack Docker Environment =====" -ForegroundColor Green

Write-Host "Stopping and removing Docker containers..." -ForegroundColor Yellow
docker-compose down

Write-Host "`nAll containers have been stopped." -ForegroundColor Cyan
Write-Host "To restart the environment, run: ./docker-start.ps1" -ForegroundColor Magenta
