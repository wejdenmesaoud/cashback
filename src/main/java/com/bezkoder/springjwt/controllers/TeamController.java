package com.bezkoder.springjwt.controllers;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.springjwt.models.Team;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.TeamRepository;
import com.bezkoder.springjwt.repository.UserRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/teams")
public class TeamController {
    @Autowired
    TeamRepository teamRepository;
    
    @Autowired
    UserRepository userRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Team>> getAllTeams() {
        List<Team> teams = teamRepository.findAll();
        return new ResponseEntity<>(teams, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Team> getTeamById(@PathVariable("id") Long id) {
        Optional<Team> teamData = teamRepository.findById(id);
        
        if (teamData.isPresent()) {
            return new ResponseEntity<>(teamData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/name/{name}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Team> getTeamByName(@PathVariable("name") String name) {
        Team team = teamRepository.findByName(name);
        
        if (team != null) {
            return new ResponseEntity<>(team, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Team>> getTeamsByUser(@PathVariable("userId") Long userId) {
        Optional<User> userData = userRepository.findById(userId);
        
        if (userData.isPresent()) {
            List<Team> teams = teamRepository.findByUser(userData.get());
            return new ResponseEntity<>(teams, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Team> createTeam(@Valid @RequestBody Team team) {
        try {
            Team _team = teamRepository.save(team);
            return new ResponseEntity<>(_team, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Team> createTeamWithManager(@Valid @RequestBody Team team, @PathVariable("userId") Long userId) {
        try {
            Optional<User> userData = userRepository.findById(userId);
            
            if (userData.isPresent()) {
                team.setUser(userData.get());
                Team _team = teamRepository.save(team);
                return new ResponseEntity<>(_team, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Team> updateTeam(@PathVariable("id") Long id, @Valid @RequestBody Team team) {
        Optional<Team> teamData = teamRepository.findById(id);
        
        if (teamData.isPresent()) {
            Team _team = teamData.get();
            _team.setName(team.getName());
            _team.setUser(team.getUser());
            
            return new ResponseEntity<>(teamRepository.save(_team), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Team> assignTeamToUser(@PathVariable("id") Long id, @PathVariable("userId") Long userId) {
        Optional<Team> teamData = teamRepository.findById(id);
        Optional<User> userData = userRepository.findById(userId);
        
        if (teamData.isPresent() && userData.isPresent()) {
            Team _team = teamData.get();
            _team.setUser(userData.get());
            
            return new ResponseEntity<>(teamRepository.save(_team), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteTeam(@PathVariable("id") Long id) {
        try {
            teamRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 