@echo off
echo ===== Stopping CaseCashBack Docker Environment =====

echo Stopping and removing Docker containers...
docker-compose down

echo.
echo All containers have been stopped.
echo To restart the environment, run: docker-start.bat
echo.
