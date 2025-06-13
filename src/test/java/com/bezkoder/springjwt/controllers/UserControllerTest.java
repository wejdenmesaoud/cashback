package com.bezkoder.springjwt.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.bezkoder.springjwt.models.ERole;
import com.bezkoder.springjwt.models.Role;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.payload.response.UserResponse;
import com.bezkoder.springjwt.repository.RoleRepository;
import com.bezkoder.springjwt.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private User testManager;
    private Role userRole;
    private Role managerRole;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Create test roles
        userRole = new Role(ERole.ROLE_USER);
        userRole.setId(1);

        managerRole = new Role(ERole.ROLE_MODERATOR);
        managerRole.setId(2);

        // Create test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        Set<Role> userRoles = new HashSet<>();
        userRoles.add(userRole);
        testUser.setRoles(userRoles);

        // Create test manager
        testManager = new User();
        testManager.setId(2L);
        testManager.setUsername("testmanager");
        testManager.setEmail("manager@example.com");
        testManager.setPassword("password");
        testManager.setFirstName("Test");
        testManager.setLastName("Manager");
        Set<Role> managerRoles = new HashSet<>();
        managerRoles.add(userRole);
        managerRoles.add(managerRole);
        testManager.setRoles(managerRoles);
    }

    @Test
    public void testGetAllManagers() {
        // Setup test data
        List<User> users = new ArrayList<>();
        users.add(testUser);
        users.add(testManager);

        // Mock repository method
        when(userRepository.findAll()).thenReturn(users);

        // Execute the test
        ResponseEntity<List<UserResponse>> response = userController.getAllManagers();

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("testmanager", response.getBody().get(0).getUsername());
    }

    @Test
    public void testGetManagerById_Success() {
        // Mock repository method
        when(userRepository.findById(2L)).thenReturn(Optional.of(testManager));

        // Execute the test
        ResponseEntity<?> response = userController.getManagerById(2L);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        UserResponse userResponse = (UserResponse) response.getBody();
        assertEquals("testmanager", userResponse.getUsername());
    }

    @Test
    public void testGetManagerById_NotManager() {
        // Mock repository method
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Execute the test
        ResponseEntity<?> response = userController.getManagerById(1L);

        // Verify results
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Error: User is not a manager!", messageResponse.getMessage());
    }

    @Test
    public void testGetManagerById_NotFound() {
        // Mock repository method
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        // Execute the test
        ResponseEntity<?> response = userController.getManagerById(3L);

        // Verify results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testAddManagerRole_Success() {
        // Mock repository methods
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Execute the test
        ResponseEntity<?> response = userController.addManagerRole(1L);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Manager role added successfully!", messageResponse.getMessage());
    }

    @Test
    public void testRemoveManagerRole_Success() {
        // Mock repository methods
        when(userRepository.findById(2L)).thenReturn(Optional.of(testManager));
        when(userRepository.save(any(User.class))).thenReturn(testManager);

        // Execute the test
        ResponseEntity<?> response = userController.removeManagerRole(2L);

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Manager role removed successfully!", messageResponse.getMessage());
    }

    @Test
    public void testAddManagerRoleByUsername_Success() {
        // Mock repository methods
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(roleRepository.findByName(ERole.ROLE_MODERATOR)).thenReturn(Optional.of(managerRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // Execute the test
        ResponseEntity<?> response = userController.addManagerRoleByUsername("testuser");

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Manager role added successfully!", messageResponse.getMessage());
    }

    @Test
    public void testAddManagerRoleByUsername_UserNotFound() {
        // Mock repository method
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Execute the test
        ResponseEntity<?> response = userController.addManagerRoleByUsername("nonexistent");

        // Verify results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testRemoveManagerRoleByUsername_Success() {
        // Mock repository methods
        when(userRepository.findByUsername("testmanager")).thenReturn(Optional.of(testManager));
        when(userRepository.save(any(User.class))).thenReturn(testManager);

        // Execute the test
        ResponseEntity<?> response = userController.removeManagerRoleByUsername("testmanager");

        // Verify results
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        MessageResponse messageResponse = (MessageResponse) response.getBody();
        assertEquals("Manager role removed successfully!", messageResponse.getMessage());
    }

    @Test
    public void testRemoveManagerRoleByUsername_UserNotFound() {
        // Mock repository method
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Execute the test
        ResponseEntity<?> response = userController.removeManagerRoleByUsername("nonexistent");

        // Verify results
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
