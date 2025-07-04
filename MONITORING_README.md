# Cashback Application Monitoring with Prometheus & Grafana

This document explains how to set up and use the monitoring stack for the Cashback Management System.

## 🚀 Quick Start

### Prerequisites
- Docker and Docker Compose installed
- At least 4GB RAM available for containers
- Ports 3000, 8080, 9090 available

### Start the Complete Stack
```bash
# Start all services including monitoring
start-monitoring.bat

# Or manually with Docker Compose
docker-compose up -d
```

## 📊 Access Points

| Service | URL | Credentials |
|---------|-----|-------------|
| **Cashback API** | http://localhost:8080 | - |
| **Swagger UI** | http://localhost:8080/swagger-ui.html | - |
| **Prometheus** | http://localhost:9090 | - |
| **Grafana** | http://localhost:3000 | admin/admin |
| **Jenkins** | http://localhost:8081 | admin/admin |
| **SonarQube** | http://localhost:9000 | admin/admin |
| **phpMyAdmin** | http://localhost:8082 | root/admin |

## 🔍 Monitoring Endpoints

### Health & Status
- **Health Check**: `GET /actuator/health`
- **Application Info**: `GET /actuator/info`
- **Prometheus Metrics**: `GET /actuator/prometheus`
- **Custom Health**: `GET /api/monitoring/health`
- **Metrics Summary**: `GET /api/monitoring/metrics/summary`

### Test Endpoints (for generating sample data)
- **Test Login Success**: `POST /api/monitoring/test/login-success`
- **Test Login Failure**: `POST /api/monitoring/test/login-failure`
- **Test Cashback Request**: `POST /api/monitoring/test/cashback-request`
- **Test User Registration**: `POST /api/monitoring/test/user-registration`

## 📈 Custom Metrics

The application tracks the following business metrics:

### Counters
- `cashback_login_success_total` - Successful authentication attempts
- `cashback_login_failure_total` - Failed authentication attempts
- `cashback_user_registration_total` - New user registrations
- `cashback_requests_total` - Cashback request submissions
- `cashback_health_checks_total` - Health endpoint calls

### Gauges
- `cashback_active_users` - Currently active users

### Timers
- `cashback_authentication_duration` - Authentication response time
- `cashback_database_query_duration` - Database query performance

## 🎯 Grafana Dashboard

The pre-configured dashboard includes:

1. **HTTP Request Rate** - Real-time API traffic
2. **Active Users** - Current user activity gauge
3. **Response Time** - 50th and 95th percentile latencies
4. **Authentication Events** - Login success/failure rates
5. **Business Metrics** - Cashback requests and registrations
6. **JVM Memory Usage** - Application memory consumption

### Accessing the Dashboard
1. Go to http://localhost:3000
2. Login with `admin/admin`
3. Navigate to "Dashboards" → "Cashback Application Dashboard"

## 🧪 Testing the Setup

Run the monitoring test script:
```bash
test-monitoring.bat
```

This will:
- Test all health endpoints
- Generate sample metrics data
- Verify Prometheus connectivity
- Display access URLs

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │───▶│   Prometheus    │───▶│     Grafana     │
│   Application   │    │   (Metrics)     │    │  (Dashboards)   │
│                 │    │                 │    │                 │
│ /actuator/      │    │ :9090           │    │ :3000           │
│ prometheus      │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
        │
        ▼
┌─────────────────┐
│     MySQL       │
│   Database      │
│     :3306       │
└─────────────────┘
```

## 🔧 Configuration Files

- `monitoring/prometheus/prometheus.yml` - Prometheus scraping configuration
- `monitoring/grafana/provisioning/` - Grafana auto-configuration
- `monitoring/grafana/dashboards/` - Pre-built dashboards
- `application-docker.properties` - Docker-specific Spring Boot config

## 📝 Monitoring Best Practices

### Adding Custom Metrics
1. Inject `MonitoringService` into your controller/service
2. Use appropriate metric types:
   - **Counters** for events (logins, requests)
   - **Gauges** for current values (active users, queue size)
   - **Timers** for duration measurements

Example:
```java
@Autowired
private MonitoringService monitoringService;

public void handleLogin(boolean success) {
    if (success) {
        monitoringService.recordSuccessfulLogin();
    } else {
        monitoringService.recordFailedLogin();
    }
}
```

### Creating Alerts
1. Define alert rules in Prometheus
2. Set up notification channels in Grafana
3. Configure thresholds for:
   - High error rates
   - Slow response times
   - Memory usage
   - Database connection issues

## 🛠️ Troubleshooting

### Common Issues

**Application not starting:**
```bash
docker-compose logs app
```

**Prometheus not scraping metrics:**
- Check if app is accessible: `curl http://localhost:8080/actuator/prometheus`
- Verify Prometheus config: `docker-compose logs prometheus`

**Grafana dashboard empty:**
- Ensure Prometheus datasource is configured
- Check if metrics are being generated
- Run test script to generate sample data

**Database connection issues:**
```bash
docker-compose logs mysql
docker-compose restart mysql
```

### Useful Commands

```bash
# View all container logs
docker-compose logs -f

# Restart specific service
docker-compose restart [service-name]

# Scale application instances
docker-compose up -d --scale app=2

# Stop all services
docker-compose down

# Remove all data volumes
docker-compose down -v
```

## 🔄 Continuous Monitoring

The setup includes Jenkins for CI/CD with monitoring integration:

1. **Pipeline Metrics** - Build success/failure rates
2. **Code Quality** - SonarQube integration
3. **Deployment Monitoring** - Automated health checks

## 📊 Performance Baselines

Expected metrics for a healthy application:
- **Response Time**: < 200ms (95th percentile)
- **Error Rate**: < 1%
- **Memory Usage**: < 80% of allocated heap
- **Database Connections**: < 80% of pool size

## 🎯 Next Steps

1. **Custom Dashboards** - Create role-specific views
2. **Alerting Rules** - Set up proactive monitoring
3. **Log Aggregation** - Add ELK stack for log analysis
4. **Distributed Tracing** - Implement Jaeger/Zipkin
5. **Business Intelligence** - Connect to BI tools for analytics