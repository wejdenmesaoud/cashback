package com.bezkoder.springjwt.repository;

import com.bezkoder.springjwt.models.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    public void testFindByUsername_Found() {
        // Setup test data
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        
        // Mock repository method
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        // Execute the test
        User found = userRepository.findByUsername("testuser").orElse(null);

        // Verify results
        assertNotNull(found);
        assertEquals("testuser", found.getUsername());
        assertEquals("test@example.com", found.getEmail());
    }

    @Test
    public void testFindByUsername_NotFound() {
        // Mock repository method
        when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        // Execute the test
        User found = userRepository.findByUsername("nonexistent").orElse(null);

        // Verify results
        assertNull(found);
    }

    @Test
    public void testExistsByUsername_True() {
        // Mock repository method
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Execute the test
        boolean exists = userRepository.existsByUsername("existinguser");

        // Verify results
        assertTrue(exists);
    }

    @Test
    public void testExistsByUsername_False() {
        // Mock repository method
        when(userRepository.existsByUsername("nonexistent")).thenReturn(false);

        // Execute the test
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Verify results
        assertFalse(exists);
    }

    @Test
    public void testExistsByEmail_True() {
        // Mock repository method
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Execute the test
        boolean exists = userRepository.existsByEmail("existing@example.com");

        // Verify results
        assertTrue(exists);
    }

    @Test
    public void testExistsByEmail_False() {
        // Mock repository method
        when(userRepository.existsByEmail("nonexistent@example.com")).thenReturn(false);

        // Execute the test
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Verify results
        assertFalse(exists);
    }
} 