# CaseCashBack - Enterprise Bonus Automation & Case Management System

## Architecture Overview

CaseCashBack is built on a layered architecture that separates concerns for maintainability and scalability:

```
┌───────────────────────────────────────────────────────────┐
│                  Presentation Layer                        │
│  (REST Controllers, Request/Response DTOs, Input Validation)│
└─────────────────────────────┬─────────────────────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────┐
│                  Service Layer                             │
│  (Business Logic, Transaction Management, Bonus Algorithm) │
└─────────────────────────────┬─────────────────────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────┐
│                 Repository Layer                           │
│  (Data Access, JPA, Cursor-based Processing)              │
└─────────────────────────────┬─────────────────────────────┘
                              │
┌─────────────────────────────▼─────────────────────────────┐
│                  Persistence Layer                         │
│  (Database Schema, MySQL, Hibernate)                       │
└───────────────────────────────────────────────────────────┘
```

## Core Components & Features

### 1. Automated Bonus Calculation System

The system implements a sophisticated algorithm for calculating bonuses based on:
- Case resolution count
- Customer Experience Score (CES) ratings
- Configurable thresholds and multipliers

#### Algorithm Implementation

```java
/**
 * Core bonus calculation algorithm:
 *
 * For each resolved case:
 * - Base amount per case (configurable)
 * - Multiplier based on CES rating (configurable)
 * - Threshold for minimum acceptable CES (configurable)
 *
 * For an engineer with multiple cases, the total bonus is the sum of
 * individual case bonuses above the threshold.
 */
private BigDecimal calculateBonus(List<Case> cases) {
    BigDecimal totalBonus = BigDecimal.ZERO;

    for (Case caseItem : cases) {
        if (caseItem.getCesRating() != null && caseItem.getCesRating() >= MIN_CES_RATING) {
            BigDecimal caseBonus = BASE_BONUS_PER_CASE.multiply(
                    BigDecimal.valueOf(caseItem.getCesRating())
                    .multiply(CES_RATING_MULTIPLIER));

            totalBonus = totalBonus.add(caseBonus);
        }
    }

    return totalBonus.setScale(2, RoundingMode.HALF_UP);
}
```

### 2. Case Management & Tracking

The system provides comprehensive tracking of support cases:
- Full history of case resolution
- CES ratings and survey sources
- Engineer performance metrics
- Detailed reporting and analytics

### 3. Advanced Data Processing

For large-scale operations, we implement cursor-based streaming to efficiently process data:

```java
/**
 * Stream-based processing handles large datasets without memory issues.
 * This allows for efficient processing of historical data without
 * requiring all records to be loaded into memory simultaneously.
 */
@Transactional(readOnly = true)
public List<CaseStatistics> generateLargeReport(LocalDate startDate, LocalDate endDate) {
    try (Stream<Case> caseStream = caseRepository.streamCasesByDateRange(startDate, endDate)) {
        return caseStream
            .collect(Collectors.groupingBy(Case::getEngineer))
            .entrySet().stream()
            .map(entry -> {
                Engineer engineer = entry.getKey();
                List<Case> engineerCases = entry.getValue();

                double avgRating = engineerCases.stream()
                    .mapToInt(Case::getCesRating)
                    .filter(rating -> rating > 0)
                    .average()
                    .orElse(0.0);

                BigDecimal bonusAmount = calculateBonus(engineerCases);

                return new CaseStatistics(engineer, engineerCases.size(),
                                         avgRating, bonusAmount);
            })
            .collect(Collectors.toList());
    }
}
```

### 4. Security Framework

The system implements a robust security framework with JWT authentication:
- Token-based authentication using JWT
- Role-based access control
- Password encryption with BCrypt
- CSRF protection and XSS prevention
- Resource-based authorization

## Detailed Database Schema

```sql
-- Users and Authentication
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(20) NOT NULL UNIQUE,
    email VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL
);

CREATE TABLE roles (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id INT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Team Management
CREATE TABLE teams (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Engineer Management
CREATE TABLE engineers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    phone_number VARCHAR(20),
    email VARCHAR(50),
    gender VARCHAR(10),
    manager VARCHAR(50) NOT NULL,
    team_id BIGINT,
    FOREIGN KEY (team_id) REFERENCES teams(id)
);

-- Case Tracking
CREATE TABLE cases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    case_description TEXT NOT NULL,
    date DATE NOT NULL,
    ces_rating INT,
    survey_source VARCHAR(50),
    engineer_id BIGINT,
    report_id BIGINT,
    FOREIGN KEY (engineer_id) REFERENCES engineers(id),
    FOREIGN KEY (report_id) REFERENCES reports(id),
    INDEX idx_date (date),
    INDEX idx_engineer (engineer_id),
    INDEX idx_rating (ces_rating)
);

-- Reporting
CREATE TABLE reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat TEXT,
    total INT,
    engineer_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- System Configuration
CREATE TABLE settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

## Advanced Configuration Options

### Performance Tuning

```properties
# Connection Pool Configuration
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=30000

# JPA/Hibernate Optimization
spring.jpa.properties.hibernate.jdbc.batch_size=30
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true
spring.jpa.properties.hibernate.jdbc.batch_versioned_data=true

# Query Cache Configuration
spring.jpa.properties.hibernate.cache.use_second_level_cache=true
spring.jpa.properties.hibernate.cache.use_query_cache=true
spring.jpa.properties.hibernate.cache.region.factory_class=org.hibernate.cache.ehcache.EhCacheRegionFactory

# Statement Timeout
spring.datasource.hikari.connection-timeout=60000
```

### Bonus Calculation Parameters

```properties
# Bonus Calculation Settings
app.bonus.baseAmountPerCase=5.00
app.bonus.cesRatingMultiplier=1.2
app.bonus.minimumCesRating=3
app.bonus.periodicityDays=30
app.bonus.highPerformerThreshold=4.5
app.bonus.highPerformerMultiplier=1.5
```

## API Reference

### Authentication API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/api/auth/signup` | POST | Register a new user | No |
| `/api/auth/signin` | POST | Authenticate and get JWT token | No |
| `/api/auth/refreshtoken` | POST | Get a new access token | Yes |

### Engineer Management API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/api/engineers` | GET | Get all engineers | Yes |
| `/api/engineers/{id}` | GET | Get engineer by ID | Yes |
| `/api/engineers/team/{teamId}` | GET | Get engineers by team | Yes |
| `/api/engineers` | POST | Create a new engineer | Yes |
| `/api/engineers/{id}` | PUT | Update engineer details | Yes |
| `/api/engineers/{id}/team/{teamId}` | PUT | Assign engineer to team | Yes |
| `/api/engineers/{id}` | DELETE | Delete an engineer | Yes |

### Case Management API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/api/cases` | GET | Get all cases | Yes |
| `/api/cases/{id}` | GET | Get case by ID | Yes |
| `/api/cases/engineer/{engineerId}` | GET | Get cases by engineer | Yes |
| `/api/cases/team/{teamId}` | GET | Get cases by team | Yes |
| `/api/cases/date-range` | GET | Get cases within date range | Yes |
| `/api/cases/stream/date-range` | GET | Stream cases within date range | Yes |
| `/api/cases` | POST | Create a new case | Yes |
| `/api/cases/{id}` | PUT | Update case details | Yes |
| `/api/cases/{id}` | DELETE | Delete a case | Yes |
| `/api/cases/statistics/engineer/{engineerId}` | GET | Get engineer case statistics | Yes |

### Bonus Calculation API

| Endpoint | Method | Description | Auth Required |
|----------|--------|-------------|--------------|
| `/api/bonus/calculate/engineer/{engineerId}` | GET | Calculate bonus for an engineer | Yes |
| `/api/bonus/calculate/team/{teamId}` | GET | Calculate bonus for a team | Yes |
| `/api/bonus/generate-report/team/{teamId}` | POST | Generate team bonus report | Yes |

## Performance Optimization

### Identified Bottlenecks & Solutions

1. **Query Performance Issues**
   - Added strategic indexes on commonly queried columns
   - Optimized JPQL queries with join fetching
   - Implemented pagination for large result sets

2. **Memory Consumption**
   - Implemented cursor-based streaming for large datasets
   - Added batch processing for bulk operations
   - Optimized entity relationships and lazy loading

3. **Calculation Speed**
   - Implemented caching for frequently accessed data
   - Parallelized bonus calculations for teams
   - Added asynchronous processing for report generation

## Deployment Guide

### Docker Deployment

The project includes a complete Docker setup with MySQL, SQL Server, and phpMyAdmin UI.

#### Prerequisites

- Docker and Docker Compose installed on your system
- Port 8080, 8081, and 3306 available on your host machine

#### Quick Start

```bash
# Start the Docker environment
docker-compose up -d
```

Or use the provided helper scripts:
```bash
# On Windows PowerShell
.\docker-start.ps1

# On Windows Command Prompt
docker-start.bat
```

#### Accessing Services

- **Spring Boot Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **phpMyAdmin**: http://localhost:8081
  - Server: mysql
  - Username: root
  - Password: admin

#### Database Configuration

- **Database Name**: cashcase
- **Root Username**: root
- **Root Password**: admin

#### Stopping the Environment

```bash
docker-compose down
```

Or use the provided helper scripts:
```bash
# On Windows PowerShell
.\docker-stop.ps1

# On Windows Command Prompt
docker-stop.bat
```

#### Building Manually

If you want to build the application manually:

```bash
# Build the application
./mvnw clean package -DskipTests

# Build the Docker image
docker build -t casecashback .

# Run with Docker Compose
docker-compose up -d
```

### Environment-Specific Setup

Production:
```bash
java -jar target/spring-boot-security-jwt-0.0.1-SNAPSHOT.jar --spring.profiles.active=prod
```

Development:
```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Troubleshooting Guide

### Common Issues

1. **Database Connection Errors**
   - Verify MySQL is running: `systemctl status mysql` or `docker-compose ps`
   - Check connection properties in application.properties
   - Ensure database 'cashcase' exists

2. **Authentication Issues**
   - JWT token expiration: check token lifetime settings
   - Role mapping problems: verify user roles in database
   - Invalid credentials: check password encryption

3. **Performance Problems**
   - Enable SQL logging: `logging.level.org.hibernate.SQL=DEBUG`
   - Check for N+1 query issues with relationship fetching
   - Monitor memory usage for potential leaks

### Docker-Specific Issues

1. **Container Startup Problems**
   - Check container logs: `docker-compose logs [service_name]`
   - Verify port availability: ensure ports 8080, 8081, and 3306 are not in use
   - Check Docker disk space: `docker system df`

2. **Application Can't Connect to Database**
   - Ensure the MySQL container is fully initialized before the app starts
   - Check network connectivity between containers: `docker network inspect casecashback-network`
   - Verify environment variables in docker-compose.yml

3. **Data Persistence Issues**
   - Verify volume mounting: `docker volume ls` and `docker volume inspect mysql-data`
   - Check file permissions in mounted volumes
   - Ensure proper shutdown with `docker-compose down` instead of `docker-compose kill`

4. **Rebuilding After Code Changes**
   ```bash
   # Rebuild the application container
   docker-compose build app

   # Restart the application container
   docker-compose up -d --no-deps app
   ```

## Implementation Examples

### Team Bonus Calculation Example

```java
@GetMapping("/calculate/team/{teamId}")
@PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
@Transactional(readOnly = true)
public ResponseEntity<?> calculateBonusForTeam(
        @PathVariable("teamId") Long teamId,
        @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
        @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

    Optional<Team> teamData = teamRepository.findById(teamId);

    if (teamData.isPresent()) {
        Team team = teamData.get();
        List<Engineer> engineers = engineerRepository.findByTeam(team);

        // Parallel processing for large teams
        List<Map<String, Object>> engineerBonuses = engineers.parallelStream().map(engineer -> {
            List<Case> cases = caseRepository.findByEngineerAndDateBetween(engineer, startDate, endDate);
            BigDecimal engineerBonus = calculateBonus(cases);
            Double avgCesRating = calculateAverageCesRating(cases);

            Map<String, Object> engineerResult = new HashMap<>();
            engineerResult.put("engineerId", engineer.getId());
            engineerResult.put("engineerName", engineer.getFullName());
            engineerResult.put("totalCases", cases.size());
            engineerResult.put("bonus", engineerBonus);
            engineerResult.put("averageCesRating", avgCesRating);

            return engineerResult;
        }).collect(Collectors.toList());

        BigDecimal teamTotalBonus = engineerBonuses.stream()
                .map(map -> (BigDecimal) map.get("bonus"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int teamTotalCases = engineerBonuses.stream()
                .mapToInt(map -> (Integer) map.get("totalCases"))
                .sum();

        Map<String, Object> result = new HashMap<>();
        result.put("teamId", teamId);
        result.put("teamName", team.getName());
        result.put("totalEngineers", engineers.size());
        result.put("totalCases", teamTotalCases);
        result.put("totalBonus", teamTotalBonus);
        result.put("startDate", startDate);
        result.put("endDate", endDate);
        result.put("engineerDetails", engineerBonuses);

        return ResponseEntity.ok(result);
    } else {
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
```

## System Requirements

- Java 17 or higher
- MySQL 8.0 or higher
- Maven 3.6+
- 4GB RAM minimum, 8GB recommended
- 2 CPU cores minimum, 4 cores recommended for production

## Support and Contribution

For support, feature requests, or contributions:
- File an issue on our GitHub repository
- Submit pull requests for bugfixes or improvements
- Consult our documentation for detailed API information

## Security Best Practices

- Store JWT secrets securely in environment variables
- Rotate keys periodically
- Implement proper CORS configuration
- Use TLS/SSL in production
- Sanitize inputs and validate requests
- Apply principle of least privilege for roles

## License

This project is licensed under the MIT License - see the LICENSE file for details.