package com.bezkoder.springjwt.controllers;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.springjwt.models.Setting;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.SettingRepository;
import com.bezkoder.springjwt.repository.UserRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/settings")
public class SettingController {
    @Autowired
    SettingRepository settingRepository;

    @Autowired
    UserRepository userRepository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Setting>> getAllSettings() {
        List<Setting> settings = settingRepository.findAll();
        return new ResponseEntity<>(settings, HttpStatus.OK);
    }

    @GetMapping("/global")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getGlobalSetting() {
        List<Setting> settings = settingRepository.findAll();
        if (settings.isEmpty()) {
            return new ResponseEntity<>("No global setting found", HttpStatus.NOT_FOUND);
        }
        // Return the first setting as the global setting
        return new ResponseEntity<>(settings.get(0), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Setting> getSettingById(@PathVariable("id") Long id) {
        Optional<Setting> settingData = settingRepository.findById(id);

        if (settingData.isPresent()) {
            return new ResponseEntity<>(settingData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Setting>> getSettingsByUser(@PathVariable("userId") Long userId) {
        Optional<User> userData = userRepository.findById(userId);

        if (userData.isPresent()) {
            List<Setting> settings = settingRepository.findByUser(userData.get());
            return new ResponseEntity<>(settings, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSetting(@Valid @RequestBody Setting setting) {
        try {
            // Check if a setting already exists
            List<Setting> existingSettings = settingRepository.findAll();
            if (!existingSettings.isEmpty()) {
                return new ResponseEntity<>("Setting not found", HttpStatus.NOT_FOUND);
            }

            Setting savedSetting = settingRepository.save(setting);
            return new ResponseEntity<>(savedSetting, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating setting: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createSettingForUser(@Valid @RequestBody Setting setting, @PathVariable("userId") Long userId) {
        try {
            // Check if a setting already exists
            List<Setting> existingSettings = settingRepository.findAll();
            if (!existingSettings.isEmpty()) {
                return new ResponseEntity<>("Setting not found", HttpStatus.NOT_FOUND);
            }

            Optional<User> userData = userRepository.findById(userId);

            if (userData.isPresent()) {
                setting.setUser(userData.get());
                Setting savedSetting = settingRepository.save(setting);
                return new ResponseEntity<>(savedSetting, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error creating setting: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateSetting(@PathVariable("id") Long id, @Valid @RequestBody Setting setting) {
        Optional<Setting> settingData = settingRepository.findById(id);

        if (settingData.isPresent()) {
            Setting existingSetting = settingData.get();
            existingSetting.setSettingKey(setting.getSettingKey());
            existingSetting.setCaseCoefficient(setting.getCaseCoefficient());
            existingSetting.setChatCoefficient(setting.getChatCoefficient());
            existingSetting.setUser(setting.getUser());

            return new ResponseEntity<>(settingRepository.save(existingSetting), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Setting not found", HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteSetting(@PathVariable("id") Long id) {
        try {
            settingRepository.deleteById(id);
            return new ResponseEntity<>("Setting deleted successfully", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error deleting setting: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}