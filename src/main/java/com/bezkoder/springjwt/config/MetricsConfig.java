package com.bezkoder.springjwt.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Gauge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.boot.actuate.info.Info.Builder;

import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class MetricsConfig {

    private final AtomicInteger activeUsers = new AtomicInteger(0);
    private final AtomicInteger totalCashbackRequests = new AtomicInteger(0);

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
        return Gauge.builder("cashback_active_users")
                .description("Number of currently active users")
                .tag("type", "business")
                .register(meterRegistry, this, config -> config.getActiveUsers().get());
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

    // Utility methods for updating metrics
    public void incrementActiveUsers() {
        activeUsers.incrementAndGet();
    }

    public void decrementActiveUsers() {
        activeUsers.decrementAndGet();
    }

    public void incrementCashbackRequests() {
        totalCashbackRequests.incrementAndGet();
    }

    public AtomicInteger getActiveUsers() {
        return activeUsers;
    }

    public AtomicInteger getTotalCashbackRequests() {
        return totalCashbackRequests;
    }
}