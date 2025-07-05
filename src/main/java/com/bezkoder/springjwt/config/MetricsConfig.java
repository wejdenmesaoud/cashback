package com.bezkoder.springjwt.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.util.Map;

@Configuration
@EnableScheduling
public class MetricsConfig {

    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger totalCashbackRequests = new AtomicInteger(0);
    private final AtomicInteger totalLoginAttempts = new AtomicInteger(0);
    
    // Track active user sessions with timestamps
    private final Map<String, LocalDateTime> activeUserSessions = new ConcurrentHashMap<>();

    @Bean
    public Counter loginSuccessCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cashback_login_success_total")
                .description("Total number of successful logins")
                .tag("type", "authentication")
                .register(meterRegistry);
    }

    @Bean
    public Counter loginFailureCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cashback_login_failure_total")
                .description("Total number of failed login attempts")
                .tag("type", "authentication")
                .register(meterRegistry);
    }

    @Bean
    public Counter userRegistrationCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cashback_user_registration_total")
                .description("Total number of user registrations")
                .tag("type", "user_management")
                .register(meterRegistry);
    }

    @Bean
    public Counter cashbackRequestCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cashback_requests_total")
                .description("Total number of cashback requests")
                .tag("type", "business")
                .register(meterRegistry);
    }

    @Bean
    public Timer authenticationTimer(MeterRegistry meterRegistry) {
        return Timer.builder("cashback_authentication_duration")
                .description("Time taken for authentication requests")
                .tag("type", "performance")
                .register(meterRegistry);
    }

    @Bean
    public Timer databaseQueryTimer(MeterRegistry meterRegistry) {
        return Timer.builder("cashback_database_query_duration")
                .description("Time taken for database queries")
                .tag("type", "database")
                .register(meterRegistry);
    }

    @Bean
    public Gauge activeUsersGauge(MeterRegistry meterRegistry) {
        return Gauge.builder("cashback_active_users", this, config -> config.getActiveUsers().get())
                .description("Number of currently active users")
                .tag("type", "business")
                .register(meterRegistry);
    }

    @Bean
    public InfoContributor cashbackInfoContributor() {
        return new InfoContributor() {
            @Override
            public void contribute(Builder builder) {
                builder.withDetail("app", "Cashback Management System")
                       .withDetail("version", "1.0.0")
                       .withDetail("description", "Spring Boot JWT Authentication with Cashback Management")
                       .withDetail("monitoring", "Prometheus + Grafana");
            }
        };
    }

    // Track user activity with timestamp (for JWT-based sessions)
    public void trackUserActivity(String username) {
        activeUserSessions.put(username, LocalDateTime.now());
        updateActiveUsersCount();
    }

    // Remove inactive users (sessions older than 30 minutes)
    @Scheduled(fixedRate = 60000) // Run every minute
    public void cleanupInactiveUsers() {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(30);
        activeUserSessions.entrySet().removeIf(entry -> entry.getValue().isBefore(cutoff));
        updateActiveUsersCount();
    }

    private void updateActiveUsersCount() {
        activeUsers.set(activeUserSessions.size());
    }

    // Legacy methods for backward compatibility
    public void incrementActiveUsers() {
        // For immediate testing - will be overridden by actual session tracking
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        if (activeUsers.get() > 0) {
            activeUsers.decrementAndGet();
        }
    }

    public void incrementCashbackRequests() {
        totalCashbackRequests.incrementAndGet();
    }

    public void incrementLoginAttempts() {
        totalLoginAttempts.incrementAndGet();
    }

    public AtomicInteger getActiveUsers() {
        return activeUsers;
    }

    public AtomicInteger getTotalCashbackRequests() {
        return totalCashbackRequests;
    }

    public int getCurrentActiveUsersCount() {
        return activeUserSessions.size();
    }
}