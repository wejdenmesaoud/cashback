package com.bezkoder.springjwt.controllers;

import java.util.List;
import java.util.Optional;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bezkoder.springjwt.models.Case;
import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.models.Report;
import com.bezkoder.springjwt.repository.CaseRepository;
import com.bezkoder.springjwt.repository.EngineerRepository;
import com.bezkoder.springjwt.repository.ReportRepository;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/reports")
public class ReportController {
    @Autowired
    ReportRepository reportRepository;
    
    @Autowired
    CaseRepository caseRepository;
    
    @Autowired
    EngineerRepository engineerRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Report>> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Report> getReportById(@PathVariable("id") Long id) {
        Optional<Report> reportData = reportRepository.findById(id);
        
        if (reportData.isPresent()) {
            return new ResponseEntity<>(reportData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/engineer/{engineerName}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Report>> getReportsByEngineer(@PathVariable("engineerName") String engineerName) {
        List<Report> reports = reportRepository.findByEngineerName(engineerName);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
    
    @GetMapping("/total-greater-than/{total}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Report>> getReportsByTotalGreaterThan(@PathVariable("total") Integer total) {
        List<Report> reports = reportRepository.findByTotalGreaterThan(total);
        return new ResponseEntity<>(reports, HttpStatus.OK);
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Report> createReport(@Valid @RequestBody Report report) {
        try {
            Report _report = reportRepository.save(report);
            return new ResponseEntity<>(_report, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/generate/engineer/{engineerId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Report> generateEngineerReport(@PathVariable("engineerId") Long engineerId) {
        try {
            Optional<Engineer> engineerData = engineerRepository.findById(engineerId);
            
            if (engineerData.isPresent()) {
                Engineer engineer = engineerData.get();
                List<Case> engineerCases = caseRepository.findByEngineer(engineer);
                
                // Calculate the total cases
                int totalCases = engineerCases.size();
                
                // Create a new report
                Report report = new Report(
                        "Cases report for engineer: " + engineer.getFullName(),
                        totalCases,
                        engineer.getFullName()
                );
                
                Report savedReport = reportRepository.save(report);
                
                // Assign the report to all cases
                for (Case caseItem : engineerCases) {
                    caseItem.setReport(savedReport);
                    caseRepository.save(caseItem);
                }
                
                return new ResponseEntity<>(savedReport, HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Report> updateReport(@PathVariable("id") Long id, @Valid @RequestBody Report report) {
        Optional<Report> reportData = reportRepository.findById(id);
        
        if (reportData.isPresent()) {
            Report _report = reportData.get();
            _report.setChat(report.getChat());
            _report.setTotal(report.getTotal());
            _report.setEngineerName(report.getEngineerName());
            
            return new ResponseEntity<>(reportRepository.save(_report), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteReport(@PathVariable("id") Long id) {
        try {
            reportRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
} 