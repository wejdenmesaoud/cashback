package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.config.MetricsConfig;
import com.bezkoder.springjwt.models.ERole;
import com.bezkoder.springjwt.models.Role;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.request.LoginRequest;
import com.bezkoder.springjwt.payload.request.SignupRequest;
import com.bezkoder.springjwt.payload.response.JwtResponse;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.repository.RoleRepository;
import com.bezkoder.springjwt.repository.UserRepository;
import com.bezkoder.springjwt.security.jwt.JwtUtils;
import com.bezkoder.springjwt.security.services.UserDetailsImpl;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    // Mock metrics beans
    @Mock
    private MetricsConfig metricsConfig;

    @Mock
    private Counter loginSuccessCounter;

    @Mock
    private Counter loginFailureCounter;

    @Mock
    private Counter userRegistrationCounter;

    @Mock
    private Timer authenticationTimer;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testAuthenticateUser() {
        // Prepare test data
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        UserDetailsImpl userDetails = new UserDetailsImpl(1L, "testuser", "test@example.com", "password", 
                Collections.emptyList());
        
        // Mock dependencies
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("test-jwt-token");

        // Execute method
        ResponseEntity<?> response = authController.authenticateUser(loginRequest);

        // Verify results
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof JwtResponse);
        JwtResponse jwtResponse = (JwtResponse) response.getBody();
        assertEquals("test-jwt-token", jwtResponse.getAccessToken());
        assertEquals(1L, jwtResponse.getId());
        assertEquals("testuser", jwtResponse.getUsername());
        assertEquals("test@example.com", jwtResponse.getEmail());
        
        // Verify metrics were called
        verify(loginSuccessCounter, times(1)).increment();
        verify(metricsConfig, times(1)).trackUserActivity("testuser");
    }

    @Test
    public void testRegisterUser() throws Exception {
        // Prepare test data
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");
        
        Set<String> roles = new HashSet<>();
        roles.add("user");
        signupRequest.setRole(roles);

        // Mock dependencies
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        
        Role userRole = new Role();
        userRole.setId(1);
        userRole.setName(ERole.ROLE_USER);
        
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(encoder.encode("password")).thenReturn("encoded_password");

        // Execute method
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Verify results
        assertEquals(200, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("User registered successfully!", messageResponse.getMessage());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRegistrationCounter, times(1)).increment();
    }

    @Test
    public void testRegisterUserWithExistingUsername() {
        // Prepare test data
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("existinguser");
        signupRequest.setEmail("newuser@example.com");
        signupRequest.setPassword("password");

        // Mock dependencies
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Execute method
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Verify results
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error: Username is already taken!", messageResponse.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    public void testRegisterUserWithExistingEmail() {
        // Prepare test data
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("newuser");
        signupRequest.setEmail("existing@example.com");
        signupRequest.setPassword("password");

        // Mock dependencies
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Execute method
        ResponseEntity<?> response = authController.registerUser(signupRequest);

        // Verify results
        assertEquals(400, response.getStatusCodeValue());
        assertTrue(response.getBody() instanceof MessageResponse);
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error: Email is already in use!", messageResponse.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}