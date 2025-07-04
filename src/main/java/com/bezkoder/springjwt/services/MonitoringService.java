package com.bezkoder.springjwt.services;

import com.bezkoder.springjwt.config.MetricsConfig;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MonitoringService {

    @Autowired
    private Counter loginSuccessCounter;

    @Autowired
    private Counter loginFailureCounter;

    @Autowired
    private Counter userRegistrationCounter;

    @Autowired
    private Counter cashbackRequestCounter;

    @Autowired
    private Timer authenticationTimer;

    @Autowired
    private Timer databaseQueryTimer;

    @Autowired
    private MetricsConfig metricsConfig;

    public void recordSuccessfulLogin() {
        loginSuccessCounter.increment();
        metricsConfig.incrementActiveUsers();
    }

    public void recordFailedLogin() {
        loginFailureCounter.increment();
    }

    public void recordUserRegistration() {
        userRegistrationCounter.increment();
    }

    public void recordCashbackRequest() {
        cashbackRequestCounter.increment();
        metricsConfig.incrementCashbackRequests();
    }

    public void recordUserLogout() {
        metricsConfig.decrementActiveUsers();
    }

    public Timer.Sample startAuthenticationTimer() {
        return Timer.start();
    }

    public void stopAuthenticationTimer(Timer.Sample sample) {
        sample.stop(authenticationTimer);
    }

    public Timer.Sample startDatabaseQueryTimer() {
        return Timer.start();
    }

    public void stopDatabaseQueryTimer(Timer.Sample sample) {
        sample.stop(databaseQueryTimer);
    }
}