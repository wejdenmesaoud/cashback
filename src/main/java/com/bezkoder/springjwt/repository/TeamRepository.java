package com.bezkoder.springjwt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.springjwt.models.Team;
import com.bezkoder.springjwt.models.User;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByUser(User user);
    
    Team findByName(String name);
} 