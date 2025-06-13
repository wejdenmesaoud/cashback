package com.bezkoder.springjwt.security;

import com.bezkoder.springjwt.security.jwt.AuthEntryPointJwt;
import com.bezkoder.springjwt.security.jwt.AuthTokenFilter;
import com.bezkoder.springjwt.security.services.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
public class WebSecurityConfigTest {

    @Autowired
    private WebSecurityConfig webSecurityConfig;
    
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    
    @MockBean
    private AuthEntryPointJwt unauthorizedHandler;
    
    @Test
    public void testAuthenticationJwtTokenFilter() {
        AuthTokenFilter filter = webSecurityConfig.authenticationJwtTokenFilter();
        assertNotNull(filter);
    }
    
    @Test
    public void testPasswordEncoder() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        assertNotNull(encoder);
        
        String encoded = encoder.encode("password");
        assertTrue(encoder.matches("password", encoded));
        assertFalse(encoder.matches("wrongpassword", encoded));
    }
    
    @Test
    public void testAuthenticationManager() throws Exception {
        AuthenticationConfiguration authConfig = mock(AuthenticationConfiguration.class);
        AuthenticationManager authenticationManager = webSecurityConfig.authenticationManager(authConfig);
        
        // Just test that it doesn't throw exception
        assertNotNull(authenticationManager);
    }
    
    @Test
    public void testFilterChain() throws Exception {
        HttpSecurity http = mock(HttpSecurity.class);
        
        // While we can't fully test the filter chain configuration without extensive mocking,
        // we can at least verify that the method can be called without exceptions
        try {
            SecurityFilterChain filterChain = webSecurityConfig.filterChain(http);
            // If we reach here, no exception was thrown, which is what we're testing
            assertTrue(true);
        } catch (Exception e) {
            fail("Exception thrown when configuring security filter chain: " + e.getMessage());
        }
    }
} 