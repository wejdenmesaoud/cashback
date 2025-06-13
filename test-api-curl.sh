#!/bin/bash

# Colors for output
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

echo -e "${YELLOW}===== Testing CaseCashBack API =====${NC}"

# Base URL
BASE_URL="http://localhost:8080/api"

# Step 1: Register a new admin user
echo -e "\n${YELLOW}1. Testing user registration (admin)...${NC}"
curl -s -X POST "${BASE_URL}/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"admin@test.com","password":"password123","role":["admin"]}' | jq

# Step 2: Register a regular user
echo -e "\n${YELLOW}2. Testing user registration (regular user)...${NC}"
curl -s -X POST "${BASE_URL}/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","email":"user1@test.com","password":"password123","role":["user"]}' | jq

# Step 3: Register a moderator user
echo -e "\n${YELLOW}3. Testing user registration (moderator)...${NC}"
curl -s -X POST "${BASE_URL}/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"username":"mod1","email":"mod1@test.com","password":"password123","role":["mod"]}' | jq

# Step 4: Login with admin user
echo -e "\n${YELLOW}4. Testing user login (admin)...${NC}"
ADMIN_TOKEN=$(curl -s -X POST "${BASE_URL}/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"password123"}' | jq -r '.accessToken')

echo "Admin token: ${ADMIN_TOKEN:0:20}..."

# Step 5: Login with regular user
echo -e "\n${YELLOW}5. Testing user login (regular user)...${NC}"
USER_TOKEN=$(curl -s -X POST "${BASE_URL}/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"username":"user1","password":"password123"}' | jq -r '.accessToken')

echo "User token: ${USER_TOKEN:0:20}..."

# Step 6: Login with moderator user
echo -e "\n${YELLOW}6. Testing user login (moderator)...${NC}"
MOD_TOKEN=$(curl -s -X POST "${BASE_URL}/auth/signin" \
  -H "Content-Type: application/json" \
  -d '{"username":"mod1","password":"password123"}' | jq -r '.accessToken')

echo "Moderator token: ${MOD_TOKEN:0:20}..."

# Step 7: Test public endpoint
echo -e "\n${YELLOW}7. Testing public endpoint...${NC}"
curl -s -X GET "${BASE_URL}/test/all" | jq -r

# Step 8: Test user endpoint with admin token
echo -e "\n${YELLOW}8. Testing user endpoint with admin token...${NC}"
curl -s -X GET "${BASE_URL}/test/user" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r

# Step 9: Test admin endpoint with admin token
echo -e "\n${YELLOW}9. Testing admin endpoint with admin token...${NC}"
curl -s -X GET "${BASE_URL}/test/admin" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq -r

# Step 10: Create a team
echo -e "\n${YELLOW}10. Creating a team...${NC}"
TEAM_RESPONSE=$(curl -s -X POST "${BASE_URL}/teams" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{"name":"Support Team A","user":{"id":1}}')

echo "$TEAM_RESPONSE" | jq
TEAM_ID=$(echo "$TEAM_RESPONSE" | jq -r '.id')

# Step 11: Create another team
echo -e "\n${YELLOW}11. Creating another team...${NC}"
curl -s -X POST "${BASE_URL}/teams" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d '{"name":"Support Team B","user":{"id":1}}' | jq

# Step 12: Get all teams
echo -e "\n${YELLOW}12. Getting all teams...${NC}"
curl -s -X GET "${BASE_URL}/teams" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 13: Create an engineer
echo -e "\n${YELLOW}13. Creating an engineer...${NC}"
ENGINEER_RESPONSE=$(curl -s -X POST "${BASE_URL}/engineers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"fullName\":\"John Doe\",\"phoneNumber\":\"123-456-7890\",\"email\":\"john@example.com\",\"gender\":\"Male\",\"manager\":\"mod1\",\"team\":{\"id\":${TEAM_ID}}}")

echo "$ENGINEER_RESPONSE" | jq
ENGINEER_ID=$(echo "$ENGINEER_RESPONSE" | jq -r '.id')

# Step 14: Create another engineer
echo -e "\n${YELLOW}14. Creating another engineer...${NC}"
curl -s -X POST "${BASE_URL}/engineers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"fullName\":\"Jane Smith\",\"phoneNumber\":\"987-654-3210\",\"email\":\"jane@example.com\",\"gender\":\"Female\",\"manager\":\"admin\",\"team\":{\"id\":${TEAM_ID}}}" | jq

# Step 15: Get all engineers
echo -e "\n${YELLOW}15. Getting all engineers...${NC}"
curl -s -X GET "${BASE_URL}/engineers" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 16: Get engineers by team
echo -e "\n${YELLOW}16. Getting engineers by team...${NC}"
curl -s -X GET "${BASE_URL}/engineers/team/${TEAM_ID}" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 17: Create a case
echo -e "\n${YELLOW}17. Creating a case...${NC}"
CASE_RESPONSE=$(curl -s -X POST "${BASE_URL}/cases" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"caseDescription\":\"Customer having issues with login\",\"date\":\"2023-08-15\",\"cesRating\":4,\"surveySource\":\"Email\",\"engineer\":{\"id\":${ENGINEER_ID}}}")

echo "$CASE_RESPONSE" | jq
CASE_ID=$(echo "$CASE_RESPONSE" | jq -r '.id')

# Step 18: Create another case
echo -e "\n${YELLOW}18. Creating another case...${NC}"
curl -s -X POST "${BASE_URL}/cases" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"caseDescription\":\"Customer needs password reset\",\"date\":\"2023-08-16\",\"cesRating\":5,\"surveySource\":\"Phone\",\"engineer\":{\"id\":${ENGINEER_ID}}}" | jq

# Step 19: Get all cases
echo -e "\n${YELLOW}19. Getting all cases...${NC}"
curl -s -X GET "${BASE_URL}/cases" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 20: Get cases by engineer
echo -e "\n${YELLOW}20. Getting cases by engineer...${NC}"
curl -s -X GET "${BASE_URL}/cases/engineer/${ENGINEER_ID}" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 21: Get cases by date range
echo -e "\n${YELLOW}21. Getting cases by date range...${NC}"
curl -s -X GET "${BASE_URL}/cases/date-range?startDate=2023-01-01&endDate=2023-12-31" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 22: Create a report
echo -e "\n${YELLOW}22. Creating a report...${NC}"
REPORT_RESPONSE=$(curl -s -X POST "${BASE_URL}/reports" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"chat\":\"Weekly performance report\",\"total\":2,\"engineerName\":\"John Doe\"}")

echo "$REPORT_RESPONSE" | jq
REPORT_ID=$(echo "$REPORT_RESPONSE" | jq -r '.id')

# Step 23: Get all reports
echo -e "\n${YELLOW}23. Getting all reports...${NC}"
curl -s -X GET "${BASE_URL}/reports" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 24: Get reports by engineer name
echo -e "\n${YELLOW}24. Getting reports by engineer name...${NC}"
curl -s -X GET "${BASE_URL}/reports/engineer/John%20Doe" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 25: Update a case to associate with a report
echo -e "\n${YELLOW}25. Updating a case to associate with a report...${NC}"
curl -s -X PUT "${BASE_URL}/cases/${CASE_ID}" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"caseDescription\":\"Customer having issues with login\",\"date\":\"2023-08-15\",\"cesRating\":4,\"surveySource\":\"Email\",\"engineer\":{\"id\":${ENGINEER_ID}},\"report\":{\"id\":${REPORT_ID}}}" | jq

# Step 26: Get cases by report
echo -e "\n${YELLOW}26. Getting cases by report...${NC}"
curl -s -X GET "${BASE_URL}/cases/report/${REPORT_ID}" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 27: Create a setting
echo -e "\n${YELLOW}27. Creating a setting...${NC}"
SETTING_RESPONSE=$(curl -s -X POST "${BASE_URL}/settings" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" \
  -d "{\"settingKey\":\"theme\",\"user\":{\"id\":1}}")

echo "$SETTING_RESPONSE" | jq
SETTING_ID=$(echo "$SETTING_RESPONSE" | jq -r '.id')

# Step 28: Get all settings
echo -e "\n${YELLOW}28. Getting all settings...${NC}"
curl -s -X GET "${BASE_URL}/settings" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 29: Get settings by user
echo -e "\n${YELLOW}29. Getting settings by user...${NC}"
curl -s -X GET "${BASE_URL}/settings/user/1" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

# Step 30: Get engineer statistics
echo -e "\n${YELLOW}30. Getting engineer statistics...${NC}"
curl -s -X GET "${BASE_URL}/cases/statistics/engineer/${ENGINEER_ID}?startDate=2023-01-01&endDate=2023-12-31" \
  -H "Authorization: Bearer ${ADMIN_TOKEN}" | jq

echo -e "\n${GREEN}===== Testing complete =====${NC}"
