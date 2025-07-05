package com.bezkoder.springjwt.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

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

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {
  @Autowired
  AuthenticationManager authenticationManager;

  @Autowired
  UserRepository userRepository;

  @Autowired
  RoleRepository roleRepository;

  @Autowired
  PasswordEncoder encoder;

  @Autowired
  JwtUtils jwtUtils;

  @Autowired
  private MetricsConfig metricsConfig;

  @Autowired
  private Counter loginSuccessCounter;

  @Autowired
  private Counter loginFailureCounter;

  @Autowired
  private Counter userRegistrationCounter;

  @Autowired
  private Timer authenticationTimer;

  @Operation(summary = "Authenticate user", description = "Sign in a user and return a JWT token")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully authenticated",
          content = @Content(schema = @Schema(implementation = JwtResponse.class))),
      @ApiResponse(responseCode = "401", description = "Invalid credentials")
  })
  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
    Timer.Sample sample = Timer.start();
    
    try {
      Authentication authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

      SecurityContextHolder.getContext().setAuthentication(authentication);
      String jwt = jwtUtils.generateJwtToken(authentication);

      UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
      List<String> roles = userDetails.getAuthorities().stream()
          .map(item -> item.getAuthority())
          .collect(Collectors.toList());

      // Increment metrics for successful login
      loginSuccessCounter.increment();
      // Track user activity with timestamp instead of simple increment
      metricsConfig.trackUserActivity(userDetails.getUsername());
      sample.stop(authenticationTimer);

      return ResponseEntity.ok(new JwtResponse(jwt,
                           userDetails.getId(),
                           userDetails.getUsername(),
                           userDetails.getEmail(),
                           roles));
    } catch (Exception e) {
      // Increment metrics for failed login
      loginFailureCounter.increment();
      sample.stop(authenticationTimer);
      throw e;
    }
  }



  @Operation(summary = "Register user", description = "Create a new user account")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "User registered successfully",
          content = @Content(schema = @Schema(implementation = MessageResponse.class))),
      @ApiResponse(responseCode = "400", description = "Username or email already in use")
  })

  @PostMapping("/signup")
  public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
    if (userRepository.existsByUsername(signUpRequest.getUsername())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Username is already taken!"));
    }

    if (userRepository.existsByEmail(signUpRequest.getEmail())) {
      return ResponseEntity
          .badRequest()
          .body(new MessageResponse("Error: Email is already in use!"));
    }

    // Create new user's account
    User user = new User(signUpRequest.getUsername(),
               signUpRequest.getEmail(),
               encoder.encode(signUpRequest.getPassword()),
               signUpRequest.getUsername(), // Using username as firstName by default
               "");                         // Empty lastName by default

    Set<String> strRoles = signUpRequest.getRole();
    Set<Role> roles = new HashSet<>();

    if (strRoles == null) {
      Role userRole = roleRepository.findByName(ERole.ROLE_USER)
          .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
      roles.add(userRole);
    } else {
      strRoles.forEach(role -> {
        switch (role) {
        case "admin":
          Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(adminRole);

          break;
        case "mod":
          Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(modRole);

          break;
        default:
          Role userRole = roleRepository.findByName(ERole.ROLE_USER)
              .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
          roles.add(userRole);
        }
      });
    }

    user.setRoles(roles);
    userRepository.save(user);

    // Increment user registration metric
    userRegistrationCounter.increment();

    return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
  }

  @Operation(summary = "Sign out user", description = "Sign out the current user and decrement active users")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Successfully signed out")
  })
  @PostMapping("/signout")
  public ResponseEntity<?> logoutUser() {
    // Decrement active users when user logs out
    metricsConfig.decrementActiveUsers();
    
    // Clear the security context
    SecurityContextHolder.clearContext();
    
    return ResponseEntity.ok(new MessageResponse("User signed out successfully!"));
  }
}
