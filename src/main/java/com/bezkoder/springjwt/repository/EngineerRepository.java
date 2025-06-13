package com.bezkoder.springjwt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.models.Team;

@Repository
public interface EngineerRepository extends JpaRepository<Engineer, Long> {
    List<Engineer> findByTeam(Team team);
    
    Engineer findByFullName(String fullName);
    
    List<Engineer> findByManager(String manager);
} 