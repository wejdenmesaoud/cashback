package com.bezkoder.springjwt.models;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashSet;
import java.util.Set;

public class UserTest {

    @Test
    public void testUserCreation() {
        User user = new User();
        assertNotNull(user);
    }

    @Test
    public void testUserConstructor() {
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        String firstName = "Test";
        String lastName = "User";

        User user = new User(username, email, password, firstName, lastName);

        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
    }

    @Test
    public void testUserGettersAndSetters() {
        User user = new User();

        Long id = 1L;
        String username = "testuser";
        String email = "test@example.com";
        String password = "password";
        String firstName = "Test";
        String lastName = "User";
        Set<Role> roles = new HashSet<>();
        Set<Setting> settings = new HashSet<>();
        Set<Team> managedTeams = new HashSet<>();

        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRoles(roles);
        user.setSettings(settings);
        user.setManagedTeams(managedTeams);

        assertEquals(id, user.getId());
        assertEquals(username, user.getUsername());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(roles, user.getRoles());
        assertEquals(settings, user.getSettings());
        assertEquals(managedTeams, user.getManagedTeams());
    }
}