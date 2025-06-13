package com.bezkoder.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bezkoder.springjwt.models.ERole;
import com.bezkoder.springjwt.models.Role;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.payload.response.UserResponse;
import com.bezkoder.springjwt.repository.RoleRepository;
import com.bezkoder.springjwt.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "User and manager management APIs")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Operation(summary = "Get all managers", description = "Retrieve all users with manager role (ROLE_MODERATOR)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved managers"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/managers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllManagers() {
        List<User> users = userRepository.findAll();

        List<UserResponse> managers = users.stream()
            .filter(user -> user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_MODERATOR))
            .map(this::convertToUserResponse)
            .collect(Collectors.toList());

        return new ResponseEntity<>(managers, HttpStatus.OK);
    }

    @Operation(summary = "Get manager by ID", description = "Retrieve a specific manager by user ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved manager"),
        @ApiResponse(responseCode = "404", description = "Manager not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @GetMapping("/managers/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getManagerById(@PathVariable("id") Long id) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();

            // Check if user has manager role
            boolean isManager = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_MODERATOR);

            if (isManager) {
                return new ResponseEntity<>(convertToUserResponse(user), HttpStatus.OK);
            } else {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User is not a manager!"));
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Update user", description = "Update user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User updated successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUser(@PathVariable("id") Long id, @Valid @RequestBody User userRequest) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();

            // Update user fields
            if (userRequest.getUsername() != null) {
                // Check if username is already taken by another user
                if (!user.getUsername().equals(userRequest.getUsername()) &&
                    userRepository.existsByUsername(userRequest.getUsername())) {
                    return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Username is already taken!"));
                }
                user.setUsername(userRequest.getUsername());
            }

            if (userRequest.getEmail() != null) {
                // Check if email is already in use by another user
                if (!user.getEmail().equals(userRequest.getEmail()) &&
                    userRepository.existsByEmail(userRequest.getEmail())) {
                    return ResponseEntity
                        .badRequest()
                        .body(new MessageResponse("Error: Email is already in use!"));
                }
                user.setEmail(userRequest.getEmail());
            }

            if (userRequest.getPassword() != null && !userRequest.getPassword().isEmpty()) {
                user.setPassword(encoder.encode(userRequest.getPassword()));
            }

            if (userRequest.getFirstName() != null) {
                user.setFirstName(userRequest.getFirstName());
            }

            if (userRequest.getLastName() != null) {
                user.setLastName(userRequest.getLastName());
            }

            User updatedUser = userRepository.save(user);
            return new ResponseEntity<>(convertToUserResponse(updatedUser), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Add manager role by ID", description = "Add manager role (ROLE_MODERATOR) to a user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role added successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "User already has manager role"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/{id}/role/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addManagerRole(@PathVariable("id") Long id) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();

            // Check if user already has manager role
            boolean isManager = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_MODERATOR);

            if (isManager) {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User already has manager role!"));
            }

            // Add manager role
            Role managerRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Error: Manager role not found."));

            Set<Role> roles = user.getRoles();
            roles.add(managerRole);
            user.setRoles(roles);

            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Manager role added successfully!"));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Add manager role by username", description = "Add manager role (ROLE_MODERATOR) to a user by username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role added successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "User already has manager role"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PutMapping("/username/{username}/role/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addManagerRoleByUsername(@PathVariable("username") String username) {
        Optional<User> userData = userRepository.findByUsername(username);

        if (userData.isPresent()) {
            User user = userData.get();

            // Check if user already has manager role
            boolean isManager = user.getRoles().stream()
                .anyMatch(role -> role.getName() == ERole.ROLE_MODERATOR);

            if (isManager) {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User already has manager role!"));
            }

            // Add manager role
            Role managerRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                .orElseThrow(() -> new RuntimeException("Error: Manager role not found."));

            Set<Role> roles = user.getRoles();
            roles.add(managerRole);
            user.setRoles(roles);

            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Manager role added successfully!"));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Remove manager role by ID", description = "Remove manager role (ROLE_MODERATOR) from a user by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role removed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "User does not have manager role"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/{id}/role/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeManagerRole(@PathVariable("id") Long id) {
        Optional<User> userData = userRepository.findById(id);

        if (userData.isPresent()) {
            User user = userData.get();

            // Find manager role
            Optional<Role> managerRoleOpt = user.getRoles().stream()
                .filter(role -> role.getName() == ERole.ROLE_MODERATOR)
                .findFirst();

            if (managerRoleOpt.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User does not have manager role!"));
            }

            // Remove manager role
            Set<Role> roles = user.getRoles();
            roles.remove(managerRoleOpt.get());
            user.setRoles(roles);

            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Manager role removed successfully!"));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(summary = "Remove manager role by username", description = "Remove manager role (ROLE_MODERATOR) from a user by username")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Role removed successfully"),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "400", description = "User does not have manager role"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @DeleteMapping("/username/{username}/role/manager")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> removeManagerRoleByUsername(@PathVariable("username") String username) {
        Optional<User> userData = userRepository.findByUsername(username);

        if (userData.isPresent()) {
            User user = userData.get();

            // Find manager role
            Optional<Role> managerRoleOpt = user.getRoles().stream()
                .filter(role -> role.getName() == ERole.ROLE_MODERATOR)
                .findFirst();

            if (managerRoleOpt.isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: User does not have manager role!"));
            }

            // Remove manager role
            Set<Role> roles = user.getRoles();
            roles.remove(managerRoleOpt.get());
            user.setRoles(roles);

            userRepository.save(user);

            return ResponseEntity.ok(new MessageResponse("Manager role removed successfully!"));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    // Helper method to convert User to UserResponse
    private UserResponse convertToUserResponse(User user) {
        List<String> roles = user.getRoles().stream()
            .map(role -> role.getName().name())
            .collect(Collectors.toList());

        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            roles
        );
    }
}
