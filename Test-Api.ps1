Write-Host "===== Testing CaseCashBack API =====" -ForegroundColor Green

# Register a new user
Write-Host "`nTesting user registration..." -ForegroundColor Yellow
$registerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" -Method Post -ContentType "application/json" -Body '{"username":"admin","email":"admin@test.com","password":"password123","role":["admin"]}' -ErrorAction SilentlyContinue
if ($registerResponse) {
    Write-Host "Registration Response: $($registerResponse | ConvertTo-Json)" -ForegroundColor Cyan
} else {
    Write-Host "Registration failed or user already exists" -ForegroundColor Yellow
}

# Login with the user
Write-Host "`nTesting user login..." -ForegroundColor Yellow
$loginResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signin" -Method Post -ContentType "application/json" -Body '{"username":"admin","password":"password123"}' -ErrorAction SilentlyContinue
if ($loginResponse) {
    Write-Host "Login successful, token received" -ForegroundColor Green
    $token = $loginResponse.accessToken
} else {
    Write-Host "Login failed" -ForegroundColor Red
    exit
}

# Create Engineer
Write-Host "`nCreating an engineer..." -ForegroundColor Yellow
$engineerResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/engineers" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $token"} -Body '{"fullName":"John Doe","phoneNumber":"123-456-7890","email":"john@example.com","gender":"Male","manager":"moderator"}' -ErrorAction SilentlyContinue
if ($engineerResponse) {
    Write-Host "Engineer created with ID: $($engineerResponse.id)" -ForegroundColor Green
    $engineerId = $engineerResponse.id
} else {
    Write-Host "Failed to create engineer" -ForegroundColor Red
}

# Create Team
Write-Host "`nCreating a team..." -ForegroundColor Yellow
$teamResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/teams" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $token"} -Body '{"name":"Support Team A"}' -ErrorAction SilentlyContinue
if ($teamResponse) {
    Write-Host "Team created with ID: $($teamResponse.id)" -ForegroundColor Green
    $teamId = $teamResponse.id
} else {
    Write-Host "Failed to create team" -ForegroundColor Red
}

# Assign Engineer to Team
if ($engineerId -and $teamId) {
    Write-Host "`nAssigning engineer to team..." -ForegroundColor Yellow
    $assignResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/engineers/$engineerId/team/$teamId" -Method Put -Headers @{Authorization = "Bearer $token"} -ErrorAction SilentlyContinue
    if ($assignResponse) {
        Write-Host "Engineer assigned to team successfully" -ForegroundColor Green
    } else {
        Write-Host "Failed to assign engineer to team" -ForegroundColor Red
    }
}

# Create a case
Write-Host "`nCreating a case..." -ForegroundColor Yellow
$caseBody = @{
    caseDescription = "Customer having issues with login"
    date = "2023-08-15"
    cesRating = 4
    surveySource = "Email"
}
if ($engineerId) {
    $caseBody.Add("engineer", @{id = $engineerId})
}
$caseResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/cases" -Method Post -ContentType "application/json" -Headers @{Authorization = "Bearer $token"} -Body ($caseBody | ConvertTo-Json) -ErrorAction SilentlyContinue
if ($caseResponse) {
    Write-Host "Case created with ID: $($caseResponse.id)" -ForegroundColor Green
    $caseId = $caseResponse.id
} else {
    Write-Host "Failed to create case" -ForegroundColor Red
}

# Get all engineers
Write-Host "`nGetting all engineers..." -ForegroundColor Yellow
$engineers = Invoke-RestMethod -Uri "http://localhost:8080/api/engineers" -Method Get -Headers @{Authorization = "Bearer $token"} -ErrorAction SilentlyContinue
if ($engineers) {
    Write-Host "Engineers: $($engineers | ConvertTo-Json -Depth 1)" -ForegroundColor Cyan
} else {
    Write-Host "Failed to get engineers" -ForegroundColor Red
}

# Calculate bonus
if ($teamId) {
    Write-Host "`nCalculating bonus for team..." -ForegroundColor Yellow
    $bonusResponse = Invoke-RestMethod -Uri "http://localhost:8080/api/bonus/calculate/team/$teamId?startDate=2023-01-01&endDate=2023-12-31" -Method Get -Headers @{Authorization = "Bearer $token"} -ErrorAction SilentlyContinue
    if ($bonusResponse) {
        Write-Host "Bonus calculation: $($bonusResponse | ConvertTo-Json -Depth 3)" -ForegroundColor Cyan
    } else {
        Write-Host "Failed to calculate bonus" -ForegroundColor Red
    }
}

Write-Host "`n===== Testing complete =====" -ForegroundColor Green