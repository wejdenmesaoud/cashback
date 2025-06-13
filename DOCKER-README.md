# Docker Setup for CaseCashBack

This document provides instructions for running the CaseCashBack application using Docker.

## Components

The Docker setup includes:

1. **Spring Boot Application** - The backend application
2. **MySQL Database** - The database server with root user (password: admin)
3. **phpMyAdmin** - Web interface for MySQL database management

## Prerequisites

- Docker and Docker Compose installed on your system
- Port 8080, 8081, and 3306 available on your host machine

## Getting Started

### 1. Build and Start the Containers

```bash
# Navigate to the project directory
cd path/to/CaseCashBack

# Build and start all services
docker-compose up -d
```

### 2. Access the Services

- **Spring Boot Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **phpMyAdmin**: http://localhost:8081
  - Server: mysql
  - Username: root
  - Password: admin

### 3. Stop the Containers

```bash
docker-compose down
```

To remove volumes as well (this will delete all data):

```bash
docker-compose down -v
```

## Database Configuration

- **Database Name**: cashcase
- **Root Username**: root
- **Root Password**: admin
- **Regular User**: user
- **Regular Password**: password

## Troubleshooting

### Application Can't Connect to Database

If the application can't connect to the database, it might be because the database initialization is taking longer than expected. Try restarting the application container:

```bash
docker-compose restart app
```

### Checking Logs

To check logs for any service:

```bash
# For the Spring Boot application
docker-compose logs app

# For MySQL
docker-compose logs mysql

# For phpMyAdmin
docker-compose logs phpmyadmin
```

### Rebuilding the Application

If you make changes to the application code, you need to rebuild the Docker image:

```bash
docker-compose build app
docker-compose up -d
```

## Data Persistence

The MySQL data is stored in a Docker volume named `mysql-data`. This ensures that your data persists even if you stop or remove the containers.

## Custom Configuration

You can modify the environment variables in the `docker-compose.yml` file to customize the setup according to your needs.
