@echo off
setlocal enabledelayedexpansion
echo ===== Testing CaseCashBack API =====

:: Register a new user
echo.
echo Testing user registration...
curl -X POST http://localhost:8080/api/auth/signup ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"email\":\"admin@test.com\",\"password\":\"password123\",\"role\":[\"admin\"]}"

:: Login with the user
echo.
echo Testing user login...
curl -X POST http://localhost:8080/api/auth/signin ^
  -H "Content-Type: application/json" ^
  -d "{\"username\":\"admin\",\"password\":\"password123\"}" > token.json

:: Extracting token (this is a simplistic approach, might need adjustment)
echo.
echo Extracting token...
for /f "tokens=3 delims=:," %%a in ('type token.json ^| findstr "accessToken"') do (
    set token=%%a
    set token=!token:"=!
    set token=!token: =!
)

:: Create Engineer
echo.
echo Creating an engineer...
curl -X POST http://localhost:8080/api/engineers ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer !token!" ^
  -d "{\"fullName\":\"John Doe\",\"phoneNumber\":\"123-456-7890\",\"email\":\"john@example.com\",\"gender\":\"Male\",\"manager\":\"admin\"}"

:: Create Team
echo.
echo Creating a team...
curl -X POST http://localhost:8080/api/teams ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer !token!" ^
  -d "{\"name\":\"Support Team A\"}"

:: Create a case
echo.
echo Creating a case...
curl -X POST http://localhost:8080/api/cases ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer !token!" ^
  -d "{\"caseDescription\":\"Customer having issues with login\",\"date\":\"2023-08-15\",\"cesRating\":4,\"surveySource\":\"Email\"}"

:: Get all engineers
echo.
echo Getting all engineers...
curl -X GET http://localhost:8080/api/engineers ^
  -H "Authorization: Bearer !token!"

:: Calculate bonus
echo.
echo Calculating bonus...
curl -X GET "http://localhost:8080/api/bonus/calculate/team/1?startDate=2023-01-01&endDate=2023-12-31" ^
  -H "Authorization: Bearer !token!"

echo.
echo ===== Testing complete =====
endlocal