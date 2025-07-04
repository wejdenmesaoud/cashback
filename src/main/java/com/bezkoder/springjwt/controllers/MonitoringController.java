package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.services.MonitoringService;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/monitoring")
@CrossOrigin(origins = "*", maxAge = 3600)
public class MonitoringController {

    @Autowired
    private MonitoringService monitoringService;

    @Autowired
    private MeterRegistry meterRegistry;

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealthStatus() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", System.currentTimeMillis());
        health.put("application", "Cashback Management System");
        health.put("version", "1.0.0");
        
        // Record this health check request
        meterRegistry.counter("cashback_health_checks_total").increment();
        
        return ResponseEntity.ok(health);
    }

    @GetMapping("/metrics/summary")
    public ResponseEntity<Map<String, Object>> getMetricsSummary() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Get counters
        metrics.put("login_success_total", 
            meterRegistry.counter("cashback_login_success_total").count());
        metrics.put("login_failure_total", 
            meterRegistry.counter("cashback_login_failure_total").count());
        metrics.put("user_registration_total", 
            meterRegistry.counter("cashback_user_registration_total").count());
        metrics.put("cashback_requests_total", 
            meterRegistry.counter("cashback_requests_total").count());
        
        // Get gauge values - using find() method to safely get existing gauges
        try {
            Double activeUsers = meterRegistry.find("cashback_active_users").gauge() != null 
                ? meterRegistry.find("cashback_active_users").gauge().value() : 0.0;
            metrics.put("active_users", activeUsers);
        } catch (Exception e) {
            metrics.put("active_users", 0.0);
        }
        
        // JVM metrics - using find() method for safety
        try {
            Double jvmMemoryUsed = meterRegistry.find("jvm.memory.used").tag("area", "heap").gauge() != null 
                ? meterRegistry.find("jvm.memory.used").tag("area", "heap").gauge().value() : 0.0;
            metrics.put("jvm_memory_used", jvmMemoryUsed);
            
            Double jvmMemoryMax = meterRegistry.find("jvm.memory.max").tag("area", "heap").gauge() != null 
                ? meterRegistry.find("jvm.memory.max").tag("area", "heap").gauge().value() : 0.0;
            metrics.put("jvm_memory_max", jvmMemoryMax);
        } catch (Exception e) {
            metrics.put("jvm_memory_used", 0.0);
            metrics.put("jvm_memory_max", 0.0);
        }
        
        return ResponseEntity.ok(metrics);
    }

    @PostMapping("/test/login-success")
    public ResponseEntity<String> testLoginSuccess() {
        monitoringService.recordSuccessfulLogin();
        return ResponseEntity.ok("Login success metric recorded");
    }

    @PostMapping("/test/login-failure")
    public ResponseEntity<String> testLoginFailure() {
        monitoringService.recordFailedLogin();
        return ResponseEntity.ok("Login failure metric recorded");
    }

    @PostMapping("/test/cashback-request")
    public ResponseEntity<String> testCashbackRequest() {
        monitoringService.recordCashbackRequest();
        return ResponseEntity.ok("Cashback request metric recorded");
    }

    @PostMapping("/test/user-registration")
    public ResponseEntity<String> testUserRegistration() {
        monitoringService.recordUserRegistration();
        return ResponseEntity.ok("User registration metric recorded");
    }
}