package com.bezkoder.springjwt.controllers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/api/cases")
public class CaseController {
    @Autowired
    CaseRepository caseRepository;
    
    @Autowired
    EngineerRepository engineerRepository;
    
    @Autowired
    ReportRepository reportRepository;
    
    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Case>> getAllCases() {
        List<Case> cases = caseRepository.findAll();
        return new ResponseEntity<>(cases, HttpStatus.OK);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Case> getCaseById(@PathVariable("id") Long id) {
        Optional<Case> caseData = caseRepository.findById(id);
        
        if (caseData.isPresent()) {
            return new ResponseEntity<>(caseData.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/engineer/{engineerId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Case>> getCasesByEngineer(@PathVariable("engineerId") Long engineerId) {
        Optional<Engineer> engineerData = engineerRepository.findById(engineerId);
        
        if (engineerData.isPresent()) {
            List<Case> cases = caseRepository.findByEngineer(engineerData.get());
            return new ResponseEntity<>(cases, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/report/{reportId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Case>> getCasesByReport(@PathVariable("reportId") Long reportId) {
        Optional<Report> reportData = reportRepository.findById(reportId);
        
        if (reportData.isPresent()) {
            List<Case> cases = caseRepository.findByReport(reportData.get());
            return new ResponseEntity<>(cases, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @GetMapping("/date-range")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Case>> getCasesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Case> cases = caseRepository.findByDateBetween(startDate, endDate);
        return new ResponseEntity<>(cases, HttpStatus.OK);
    }
    
    @GetMapping("/team/{teamId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Case>> getCasesByTeam(@PathVariable("teamId") Long teamId) {
        List<Case> cases = caseRepository.findByTeamId(teamId);
        return new ResponseEntity<>(cases, HttpStatus.OK);
    }
    
    @GetMapping("/stream/date-range")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<List<Case>> streamCasesByDateRange(
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        List<Case> cases = caseRepository.streamCasesByDateRange(startDate, endDate)
                .collect(Collectors.toList());
        return new ResponseEntity<>(cases, HttpStatus.OK);
    }

    @GetMapping("/statistics/engineer/{engineerId}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getEngineerStatistics(
            @PathVariable("engineerId") Long engineerId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        Optional<Engineer> engineerData = engineerRepository.findById(engineerId);
        
        if (engineerData.isPresent()) {
            Engineer engineer = engineerData.get();
            Long caseCount = caseRepository.countCasesResolvedByEngineer(engineer, startDate, endDate);
            Double avgRating = caseRepository.calculateAverageCesRating(engineer, startDate, endDate);
            
            return ResponseEntity.ok()
                    .body(java.util.Map.of(
                            "engineerId", engineerId,
                            "engineerName", engineer.getFullName(),
                            "caseCount", caseCount,
                            "averageCesRating", avgRating != null ? avgRating : 0.0,
                            "startDate", startDate,
                            "endDate", endDate
                    ));
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Case> createCase(@Valid @RequestBody Case caseObj) {
        try {
            Case _case = caseRepository.save(caseObj);
            return new ResponseEntity<>(_case, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Case> updateCase(@PathVariable("id") Long id, @Valid @RequestBody Case caseObj) {
        Optional<Case> caseData = caseRepository.findById(id);
        
        if (caseData.isPresent()) {
            Case _case = caseData.get();
            _case.setCaseDescription(caseObj.getCaseDescription());
            _case.setDate(caseObj.getDate());
            _case.setCesRating(caseObj.getCesRating());
            _case.setSurveySource(caseObj.getSurveySource());
            _case.setEngineer(caseObj.getEngineer());
            _case.setReport(caseObj.getReport());
            
            return new ResponseEntity<>(caseRepository.save(_case), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}/engineer/{engineerId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Case> assignCaseToEngineer(@PathVariable("id") Long id, @PathVariable("engineerId") Long engineerId) {
        Optional<Case> caseData = caseRepository.findById(id);
        Optional<Engineer> engineerData = engineerRepository.findById(engineerId);
        
        if (caseData.isPresent() && engineerData.isPresent()) {
            Case _case = caseData.get();
            _case.setEngineer(engineerData.get());
            
            return new ResponseEntity<>(caseRepository.save(_case), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{id}/report/{reportId}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<Case> assignCaseToReport(@PathVariable("id") Long id, @PathVariable("reportId") Long reportId) {
        Optional<Case> caseData = caseRepository.findById(id);
        Optional<Report> reportData = reportRepository.findById(reportId);
        
        if (caseData.isPresent() && reportData.isPresent()) {
            Case _case = caseData.get();
            _case.setReport(reportData.get());
            
            return new ResponseEntity<>(caseRepository.save(_case), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HttpStatus> deleteCase(@PathVariable("id") Long id) {
        try {
            caseRepository.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}