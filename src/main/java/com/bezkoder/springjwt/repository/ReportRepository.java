package com.bezkoder.springjwt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bezkoder.springjwt.models.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByEngineerName(String engineerName);
    
    List<Report> findByTotalGreaterThan(Integer total);
} 