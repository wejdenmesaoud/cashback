package com.bezkoder.springjwt.config;

import com.bezkoder.springjwt.security.jwt.AuthEntryPointJwt;
import com.bezkoder.springjwt.security.jwt.AuthTokenFilter;
import com.bezkoder.springjwt.security.services.UserDetailsServiceImpl;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public UserDetailsService userDetailsServiceForTesting() {
        return mock(UserDetailsServiceImpl.class);
    }

    @Bean
    @Primary
    public AuthTokenFilter authenticationJwtTokenFilterForTesting() {
        return mock(AuthTokenFilter.class);
    }

    @Bean
    @Primary
    public AuthEntryPointJwt unauthorizedHandlerForTesting() {
        return mock(AuthEntryPointJwt.class);
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoderForTesting() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManagerForTesting(AuthenticationConfiguration authConfig) throws Exception {
        return mock(AuthenticationManager.class);
    }
} 