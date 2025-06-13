# Manager (Moderator) API Documentation

## Base URL
```
http://localhost:8080/api
```

## 1. Create Manager (Moderator)

### Register New Moderator Account
- **URL**: `/auth/signup`
- **Method**: POST
- **Auth Required**: Yes (Admin only)
- **Authorization**: Bearer Token
- **Body**:
```json
{
    "username": "string",
    "email": "string",
    "password": "string",
    "role": ["mod"]
}
```
- **Success Response**: `200 OK`
```json
{
    "message": "User registered successfully!"
}
```
- **Error Response**: `400 Bad Request`
```json
{
    "message": "Error: Username is already taken!"
}
```

### Assign Manager to Team
- **URL**: `/teams/{teamId}`
- **Method**: PUT
- **Auth Required**: Yes (Admin only)
- **Authorization**: Bearer Token
- **Body**:
```json
{
    "name": "string",
    "user": {
        "id": "number" // ID of the moderator user
    }
}
```
- **Success Response**: `200 OK`
```json
{
    "id": "number",
    "name": "string",
    "user": {
        "id": "number",
        "username": "string",
        "email": "string"
    }
}
```

## 2. Update Manager

### Update Manager Profile
- **URL**: `/auth/users/{id}`
- **Method**: PUT
- **Auth Required**: Yes (Admin or Self)
- **Authorization**: Bearer Token
- **Body**:
```json
{
    "username": "string",
    "email": "string",
    "password": "string" // Optional
}
```
- **Success Response**: `200 OK`
```json
{
    "id": "number",
    "username": "string",
    "email": "string",
    "roles": ["ROLE_MODERATOR"]
}
```

### Update Manager's Team Assignment
- **URL**: `/teams/{teamId}/user/{userId}`
- **Method**: PUT
- **Auth Required**: Yes (Admin only)
- **Authorization**: Bearer Token
- **Success Response**: `200 OK`
```json
{
    "id": "number",
    "name": "string",
    "user": {
        "id": "number",
        "username": "string"
    }
}
```

## 3. Delete Manager

### Remove Manager Role
- **URL**: `/auth/users/{id}/role`
- **Method**: DELETE
- **Auth Required**: Yes (Admin only)
- **Authorization**: Bearer Token
- **Query Parameters**:
  - role: "mod"
- **Success Response**: `200 OK`
```json
{
    "message": "Role removed successfully"
}
```

### Delete Manager Account
- **URL**: `/auth/users/{id}`
- **Method**: DELETE
- **Auth Required**: Yes (Admin only)
- **Authorization**: Bearer Token
- **Success Response**: `200 OK`
```json
{
    "message": "User deleted successfully"
}
```

## Example Usage

### Create Manager (PowerShell)
```powershell
# 1. Create moderator account
$moderatorData = @{
    username = "manager1"
    email = "manager1@example.com"
    password = "password123"
    role = @("mod")
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/signup" `
    -Method Post `
    -ContentType "application/json" `
    -Headers @{Authorization = "Bearer $adminToken"} `
    -Body $moderatorData

# 2. Assign to team
$teamAssignment = @{
    name = "Team A"
    user = @{
        id = $response.id
    }
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/teams/1" `
    -Method Put `
    -ContentType "application/json" `
    -Headers @{Authorization = "Bearer $adminToken"} `
    -Body $teamAssignment
```

### Create Manager (cURL)
```bash
# 1. Create moderator account
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "username": "manager1",
    "email": "manager1@example.com",
    "password": "password123",
    "role": ["mod"]
  }'

# 2. Assign to team
curl -X PUT http://localhost:8080/api/teams/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -d '{
    "name": "Team A",
    "user": {
      "id": 1
    }
  }'
```

## Notes

1. Only administrators can create, update, or delete managers
2. Managers can update their own profile information
3. When deleting a manager, consider reassigning their team first
4. All requests require proper authentication using JWT tokens
5. Error handling should include:
   - 400: Bad Request (invalid input)
   - 401: Unauthorized (invalid/missing token)
   - 403: Forbidden (insufficient permissions)
   - 404: Not Found (user/team not found)
   - 409: Conflict (username/email already exists)