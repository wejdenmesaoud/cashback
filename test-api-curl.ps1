# PowerShell script to test CaseCashBack API

Write-Host "===== Testing CaseCashBack API =====" -ForegroundColor Yellow

# Base URL
$baseUrl = "http://localhost:8080/api"

# Step 1: Register a new admin user
Write-Host "`n1. Testing user registration (admin)..." -ForegroundColor Yellow
$adminRegResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signup" -Method Post -ContentType "application/json" -Body '{"username":"admin","email":"admin@test.com","password":"password123","role":["admin"]}' -ErrorAction SilentlyContinue
$adminRegResponse | ConvertTo-Json

# Step 2: Register a regular user
Write-Host "`n2. Testing user registration (regular user)..." -ForegroundColor Yellow
$userRegResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signup" -Method Post -ContentType "application/json" -Body '{"username":"user1","email":"user1@test.com","password":"password123","role":["user"]}' -ErrorAction SilentlyContinue
$userRegResponse | ConvertTo-Json

# Step 3: Register a moderator user
Write-Host "`n3. Testing user registration (moderator)..." -ForegroundColor Yellow
$modRegResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signup" -Method Post -ContentType "application/json" -Body '{"username":"mod1","email":"mod1@test.com","password":"password123","role":["mod"]}' -ErrorAction SilentlyContinue
$modRegResponse | ConvertTo-Json

# Step 4: Login with admin user
Write-Host "`n4. Testing user login (admin)..." -ForegroundColor Yellow
$adminLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"password123"}' -ErrorAction SilentlyContinue
$adminToken = $adminLoginResponse.accessToken
Write-Host "Admin token: $($adminToken.Substring(0, [Math]::Min(20, $adminToken.Length)))..."

# Step 5: Login with regular user
Write-Host "`n5. Testing user login (regular user)..." -ForegroundColor Yellow
$userLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method Post -ContentType "application/json" -Body '{"username":"user1","password":"password123"}' -ErrorAction SilentlyContinue
$userToken = $userLoginResponse.accessToken
Write-Host "User token: $($userToken.Substring(0, [Math]::Min(20, $userToken.Length)))..."

# Step 6: Login with moderator user
Write-Host "`n6. Testing user login (moderator)..." -ForegroundColor Yellow
$modLoginResponse = Invoke-RestMethod -Uri "$baseUrl/auth/signin" -Method Post -ContentType "application/json" -Body '{"username":"mod1","password":"password123"}' -ErrorAction SilentlyContinue
$modToken = $modLoginResponse.accessToken
Write-Host "Moderator token: $($modToken.Substring(0, [Math]::Min(20, $modToken.Length)))..."

# Step 7: Test public endpoint
Write-Host "`n7. Testing public endpoint..." -ForegroundColor Yellow
$publicResponse = Invoke-RestMethod -Uri "$baseUrl/test/all" -Method Get -ErrorAction SilentlyContinue
$publicResponse

# Step 8: Test user endpoint with admin token
Write-Host "`n8. Testing user endpoint with admin token..." -ForegroundColor Yellow
$userEndpointResponse = Invoke-RestMethod -Uri "$baseUrl/test/user" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$userEndpointResponse

# Step 9: Test admin endpoint with admin token
Write-Host "`n9. Testing admin endpoint with admin token..." -ForegroundColor Yellow
$adminEndpointResponse = Invoke-RestMethod -Uri "$baseUrl/test/admin" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$adminEndpointResponse

# Step 10: Create a team
Write-Host "`n10. Creating a team..." -ForegroundColor Yellow
$teamResponse = Invoke-RestMethod -Uri "$baseUrl/teams" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body '{"name":"Support Team A","user":{"id":1}}' -ErrorAction SilentlyContinue
$teamResponse | ConvertTo-Json
$teamId = $teamResponse.id

# Step 11: Create another team
Write-Host "`n11. Creating another team..." -ForegroundColor Yellow
$team2Response = Invoke-RestMethod -Uri "$baseUrl/teams" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body '{"name":"Support Team B","user":{"id":1}}' -ErrorAction SilentlyContinue
$team2Response | ConvertTo-Json

# Step 12: Get all teams
Write-Host "`n12. Getting all teams..." -ForegroundColor Yellow
$teamsResponse = Invoke-RestMethod -Uri "$baseUrl/teams" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$teamsResponse | ConvertTo-Json -Depth 3

# Step 13: Create an engineer
Write-Host "`n13. Creating an engineer..." -ForegroundColor Yellow
$engineerBody = @{
    fullName = "John Doe"
    phoneNumber = "123-456-7890"
    email = "john@example.com"
    gender = "Male"
    manager = "mod1" # Using username instead of manager name
    team = @{id = $teamId}
} | ConvertTo-Json

$engineerResponse = Invoke-RestMethod -Uri "$baseUrl/engineers" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $engineerBody -ErrorAction SilentlyContinue
$engineerResponse | ConvertTo-Json
$engineerId = $engineerResponse.id

# Step 14: Create another engineer
Write-Host "`n14. Creating another engineer..." -ForegroundColor Yellow
$engineer2Body = @{
    fullName = "Jane Smith"
    phoneNumber = "987-654-3210"
    email = "jane@example.com"
    gender = "Female"
    manager = "admin" # Using username instead of manager name
    team = @{id = $teamId}
} | ConvertTo-Json

$engineer2Response = Invoke-RestMethod -Uri "$baseUrl/engineers" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $engineer2Body -ErrorAction SilentlyContinue
$engineer2Response | ConvertTo-Json

# Step 15: Get all engineers
Write-Host "`n15. Getting all engineers..." -ForegroundColor Yellow
$engineersResponse = Invoke-RestMethod -Uri "$baseUrl/engineers" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$engineersResponse | ConvertTo-Json -Depth 3

# Step 16: Get engineers by team
Write-Host "`n16. Getting engineers by team..." -ForegroundColor Yellow
$teamEngineersResponse = Invoke-RestMethod -Uri "$baseUrl/engineers/team/$teamId" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$teamEngineersResponse | ConvertTo-Json -Depth 3

# Step 17: Create a case
Write-Host "`n17. Creating a case..." -ForegroundColor Yellow
$caseBody = @{
    caseDescription = "Customer having issues with login"
    date = "2023-08-15"
    cesRating = 4
    surveySource = "Email"
    engineer = @{id = $engineerId}
} | ConvertTo-Json

$caseResponse = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $caseBody -ErrorAction SilentlyContinue
$caseResponse | ConvertTo-Json
$caseId = $caseResponse.id

# Step 18: Create another case
Write-Host "`n18. Creating another case..." -ForegroundColor Yellow
$case2Body = @{
    caseDescription = "Customer needs password reset"
    date = "2023-08-16"
    cesRating = 5
    surveySource = "Phone"
    engineer = @{id = $engineerId}
} | ConvertTo-Json

$case2Response = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $case2Body -ErrorAction SilentlyContinue
$case2Response | ConvertTo-Json

# Step 19: Get all cases
Write-Host "`n19. Getting all cases..." -ForegroundColor Yellow
$casesResponse = Invoke-RestMethod -Uri "$baseUrl/cases" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$casesResponse | ConvertTo-Json -Depth 3

# Step 20: Get cases by engineer
Write-Host "`n20. Getting cases by engineer..." -ForegroundColor Yellow
$engineerCasesResponse = Invoke-RestMethod -Uri "$baseUrl/cases/engineer/$engineerId" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$engineerCasesResponse | ConvertTo-Json -Depth 3

# Step 21: Get cases by date range
Write-Host "`n21. Getting cases by date range..." -ForegroundColor Yellow
$dateRangeCasesResponse = Invoke-RestMethod -Uri "$baseUrl/cases/date-range?startDate=2023-01-01&endDate=2023-12-31" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$dateRangeCasesResponse | ConvertTo-Json -Depth 3

# Step 22: Create a report
Write-Host "`n22. Creating a report..." -ForegroundColor Yellow
$reportBody = @{
    chat = "Weekly performance report"
    total = 2
    engineerName = "John Doe"
} | ConvertTo-Json

$reportResponse = Invoke-RestMethod -Uri "$baseUrl/reports" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $reportBody -ErrorAction SilentlyContinue
$reportResponse | ConvertTo-Json
$reportId = $reportResponse.id

# Step 23: Get all reports
Write-Host "`n23. Getting all reports..." -ForegroundColor Yellow
$reportsResponse = Invoke-RestMethod -Uri "$baseUrl/reports" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$reportsResponse | ConvertTo-Json -Depth 3

# Step 24: Get reports by engineer name
Write-Host "`n24. Getting reports by engineer name..." -ForegroundColor Yellow
$engineerReportsResponse = Invoke-RestMethod -Uri "$baseUrl/reports/engineer/John%20Doe" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$engineerReportsResponse | ConvertTo-Json -Depth 3

# Step 25: Update a case to associate with a report
Write-Host "`n25. Updating a case to associate with a report..." -ForegroundColor Yellow
$updateCaseBody = @{
    caseDescription = "Customer having issues with login"
    date = "2023-08-15"
    cesRating = 4
    surveySource = "Email"
    engineer = @{id = $engineerId}
    report = @{id = $reportId}
} | ConvertTo-Json

$updateCaseResponse = Invoke-RestMethod -Uri "$baseUrl/cases/$caseId" -Method Put -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $updateCaseBody -ErrorAction SilentlyContinue
$updateCaseResponse | ConvertTo-Json

# Step 26: Get cases by report
Write-Host "`n26. Getting cases by report..." -ForegroundColor Yellow
$reportCasesResponse = Invoke-RestMethod -Uri "$baseUrl/cases/report/$reportId" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$reportCasesResponse | ConvertTo-Json -Depth 3

# Step 27: Create a setting
Write-Host "`n27. Creating a setting..." -ForegroundColor Yellow
$settingBody = @{
    settingKey = "theme"
    user = @{id = 1}
} | ConvertTo-Json

$settingResponse = Invoke-RestMethod -Uri "$baseUrl/settings" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $adminToken"} -Body $settingBody -ErrorAction SilentlyContinue
$settingResponse | ConvertTo-Json
$settingId = $settingResponse.id

# Step 28: Get all settings
Write-Host "`n28. Getting all settings..." -ForegroundColor Yellow
$settingsResponse = Invoke-RestMethod -Uri "$baseUrl/settings" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$settingsResponse | ConvertTo-Json -Depth 3

# Step 29: Get settings by user
Write-Host "`n29. Getting settings by user..." -ForegroundColor Yellow
$userSettingsResponse = Invoke-RestMethod -Uri "$baseUrl/settings/user/1" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$userSettingsResponse | ConvertTo-Json -Depth 3

# Step 30: Get engineer statistics
Write-Host "`n30. Getting engineer statistics..." -ForegroundColor Yellow
$engineerStatsResponse = Invoke-RestMethod -Uri "$baseUrl/cases/statistics/engineer/$engineerId`?startDate=2023-01-01&endDate=2023-12-31" -Method Get -Headers @{Authorization = "Bearer $adminToken"} -ErrorAction SilentlyContinue
$engineerStatsResponse | ConvertTo-Json

Write-Host "`n===== Testing complete =====" -ForegroundColor Green
