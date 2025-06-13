package com.bezkoder.springjwt.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.springjwt.models.Setting;
import com.bezkoder.springjwt.models.User;

@Repository
public interface SettingRepository extends JpaRepository<Setting, Long> {
    List<Setting> findByUser(User user);
    
    Optional<Setting> findBySettingKeyAndUser(String settingKey, User user);
} 