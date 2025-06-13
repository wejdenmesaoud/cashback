package com.bezkoder.springjwt.controllers;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.models.Team;
import com.bezkoder.springjwt.repository.EngineerRepository;
import com.bezkoder.springjwt.repository.TeamRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/engineers")
public class EngineerController {
    @Autowired
    EngineerRepository engineerRepository;

    @Autowired
    TeamRepository teamRepository;

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Engineer>> getAllEngineers() {
        List<Engineer> engineers = engineerRepository.findAll();
        return new ResponseEntity<>(engineers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Engineer> getEngineerById(@PathVariable("id") Long id) {
        Optional<Engineer> engineerData = engineerRepository.findById(id);

        if (engineerData.isPresent()) {
            return new ResponseEntity<>(engineerData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Engineer>> getEngineersByTeam(@PathVariable("teamId") Long teamId) {
        Optional<Team> teamData = teamRepository.findById(teamId);

        if (teamData.isPresent()) {
            List<Engineer> engineers = engineerRepository.findByTeam(teamData.get());
            return new ResponseEntity<>(engineers, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/manager/{username}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Engineer>> getEngineersByManager(@PathVariable("username") String username) {
        List<Engineer> engineers = engineerRepository.findByManager(username);
        return new ResponseEntity<>(engineers, HttpStatus.OK);
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Engineer> createEngineer(@Valid @RequestBody Engineer engineer) {
        try {
            Engineer _engineer = engineerRepository.save(engineer);
            return new ResponseEntity<>(_engineer, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Engineer> updateEngineer(@PathVariable("id") Long id, @Valid @RequestBody Engineer engineer) {
        Optional<Engineer> engineerData = engineerRepository.findById(id);

        if (engineerData.isPresent()) {
            Engineer _engineer = engineerData.get();
            _engineer.setFullName(engineer.getFullName());
            _engineer.setPhoneNumber(engineer.getPhoneNumber());
            _engineer.setEmail(engineer.getEmail());
            _engineer.setGender(engineer.getGender());
            _engineer.setManager(engineer.getManager()); // Still using manager field but now it stores username
            _engineer.setTeam(engineer.getTeam());

            return new ResponseEntity<>(engineerRepository.save(_engineer), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{id}/team/{teamId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Engineer> assignEngineerToTeam(@PathVariable("id") Long id, @PathVariable("teamId") Long teamId) {
        Optional<Engineer> engineerData = engineerRepository.findById(id);
        Optional<Team> teamData = teamRepository.findById(teamId);

        if (engineerData.isPresent() && teamData.isPresent()) {
            Engineer _engineer = engineerData.get();
            _engineer.setTeam(teamData.get());

            return new ResponseEntity<>(engineerRepository.save(_engineer), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteEngineer(@PathVariable("id") Long id) {
        try {
            engineerRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}