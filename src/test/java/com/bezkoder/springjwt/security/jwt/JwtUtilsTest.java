package com.bezkoder.springjwt.security.jwt;

import com.bezkoder.springjwt.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class JwtUtilsTest {

    @InjectMocks
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private UserDetailsImpl userDetails;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", "testSecretKey12345678901234567890123456789012345678901234567890");
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 86400000);

        userDetails = new UserDetailsImpl(
                1L,
                "testuser",
                "test@example.com",
                "password",
                new ArrayList<>()
        );

        when(authentication.getPrincipal()).thenReturn(userDetails);
    }

    @Test
    public void testGenerateJwtToken() {
        String token = jwtUtils.generateJwtToken(authentication);
        
        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    public void testGetUserNameFromJwtToken() {
        String token = jwtUtils.generateJwtToken(authentication);
        String username = jwtUtils.getUserNameFromJwtToken(token);
        
        assertEquals("testuser", username);
    }

    @Test
    public void testValidateJwtToken_ValidToken() {
        String token = jwtUtils.generateJwtToken(authentication);
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        assertTrue(isValid);
    }

    @Test
    public void testValidateJwtToken_InvalidToken() {
        boolean isValid = jwtUtils.validateJwtToken("invalidToken");
        
        assertFalse(isValid);
    }

    @Test
    public void testValidateJwtToken_ExpiredToken() throws Exception {
        // Set a very short expiration time
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1);
        
        String token = jwtUtils.generateJwtToken(authentication);
        
        // Wait for token to expire
        Thread.sleep(10);
        
        boolean isValid = jwtUtils.validateJwtToken(token);
        
        assertFalse(isValid);
    }
} 