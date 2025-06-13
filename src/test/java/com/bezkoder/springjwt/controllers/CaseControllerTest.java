package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.models.Case;
import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.models.Report;
import com.bezkoder.springjwt.repository.CaseRepository;
import com.bezkoder.springjwt.repository.EngineerRepository;
import com.bezkoder.springjwt.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CaseControllerTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EngineerRepository engineerRepository;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private CaseController caseController;

    private Case testCase;
    private Engineer testEngineer;
    private Report testReport;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);

        testEngineer = new Engineer();
        testEngineer.setId(1L);
        testEngineer.setFullName("John Doe");
        testEngineer.setEmail("john.doe@example.com");
        testEngineer.setPhoneNumber("1234567890");
        testEngineer.setGender("Male");
        testEngineer.setManager("manager_username"); // Now using username instead of manager name

        testReport = new Report();
        testReport.setId(1L);
        testReport.setChat("Test Report Chat");
        testReport.setTotal(5);
        testReport.setEngineerName("John Doe");

        testCase = new Case();
        testCase.setId(1L);
        testCase.setCaseDescription("Test Case");
        testCase.setDate(LocalDateTime.now());
        testCase.setCesRating(5);
        testCase.setSurveySource("Email");
        testCase.setEngineer(testEngineer);
        testCase.setReport(testReport);

        startDate = LocalDateTime.now().minusDays(7);
        endDate = LocalDateTime.now();
    }

    @Test
    public void testGetAllCases() {
        List<Case> cases = new ArrayList<>();
        cases.add(testCase);

        when(caseRepository.findAll()).thenReturn(cases);

        ResponseEntity<List<Case>> response = caseController.getAllCases();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testCase.getId(), response.getBody().get(0).getId());
    }

    @Test
    public void testGetCaseById_Found() {
        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCase));

        ResponseEntity<Case> response = caseController.getCaseById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testCase.getId(), response.getBody().getId());
    }

    @Test
    public void testGetCaseById_NotFound() {
        when(caseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Case> response = caseController.getCaseById(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetCasesByEngineer_Found() {
        List<Case> cases = new ArrayList<>();
        cases.add(testCase);

        when(engineerRepository.findById(1L)).thenReturn(Optional.of(testEngineer));
        when(caseRepository.findByEngineer(testEngineer)).thenReturn(cases);

        ResponseEntity<List<Case>> response = caseController.getCasesByEngineer(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testGetCasesByEngineer_NotFound() {
        when(engineerRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<List<Case>> response = caseController.getCasesByEngineer(99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testGetCasesByDateRange() {
        List<Case> cases = new ArrayList<>();
        cases.add(testCase);

        when(caseRepository.findByDateBetween(startDate, endDate)).thenReturn(cases);

        ResponseEntity<List<Case>> response = caseController.getCasesByDateRange(startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    public void testCreateCase_Success() {
        when(caseRepository.save(any(Case.class))).thenReturn(testCase);

        ResponseEntity<Case> response = caseController.createCase(testCase);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testCase.getId(), response.getBody().getId());
    }

    @Test
    public void testUpdateCase_Success() {
        Case updatedCase = new Case();
        updatedCase.setCaseDescription("Updated Description");
        updatedCase.setDate(LocalDateTime.now());
        updatedCase.setCesRating(5);
        updatedCase.setSurveySource("Phone");
        updatedCase.setEngineer(testEngineer);
        updatedCase.setReport(testReport);

        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(caseRepository.save(any(Case.class))).thenReturn(updatedCase);

        ResponseEntity<Case> response = caseController.updateCase(1L, updatedCase);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Updated Description", response.getBody().getCaseDescription());
        assertEquals(Integer.valueOf(5), response.getBody().getCesRating());
        assertEquals("Phone", response.getBody().getSurveySource());
    }

    @Test
    public void testUpdateCase_NotFound() {
        when(caseRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Case> response = caseController.updateCase(99L, testCase);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testAssignCaseToEngineer_Success() {
        Engineer newEngineer = new Engineer();
        newEngineer.setId(2L);
        newEngineer.setFullName("Jane Smith");
        newEngineer.setEmail("jane.smith@example.com");
        newEngineer.setPhoneNumber("0987654321");
        newEngineer.setGender("Female");
        newEngineer.setManager("another_username"); // Now using username instead of manager name

        Case updatedCase = new Case();
        updatedCase.setId(1L);
        updatedCase.setEngineer(newEngineer);

        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(engineerRepository.findById(2L)).thenReturn(Optional.of(newEngineer));
        when(caseRepository.save(any(Case.class))).thenReturn(updatedCase);

        ResponseEntity<Case> response = caseController.assignCaseToEngineer(1L, 2L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(newEngineer.getId(), response.getBody().getEngineer().getId());
    }

    @Test
    public void testAssignCaseToEngineer_NotFound() {
        when(caseRepository.findById(1L)).thenReturn(Optional.of(testCase));
        when(engineerRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<Case> response = caseController.assignCaseToEngineer(1L, 99L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    public void testDeleteCase_Success() {
        doNothing().when(caseRepository).deleteById(1L);

        ResponseEntity<HttpStatus> response = caseController.deleteCase(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(caseRepository, times(1)).deleteById(1L);
    }

    @Test
    public void testDeleteCase_Exception() {
        doThrow(new RuntimeException()).when(caseRepository).deleteById(1L);

        ResponseEntity<HttpStatus> response = caseController.deleteCase(1L);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    public void testGetEngineerStatistics() {
        when(engineerRepository.findById(1L)).thenReturn(Optional.of(testEngineer));
        when(caseRepository.countCasesResolvedByEngineer(testEngineer, startDate, endDate)).thenReturn(10L);
        when(caseRepository.calculateAverageCesRating(testEngineer, startDate, endDate)).thenReturn(4.5);

        ResponseEntity<?> response = caseController.getEngineerStatistics(1L, startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals(1L, responseBody.get("engineerId"));
        assertEquals(10L, responseBody.get("caseCount"));
        assertEquals(4.5, responseBody.get("averageCesRating"));
    }

    @Test
    public void testStreamCasesByDateRange() {
        List<Case> cases = new ArrayList<>();
        cases.add(testCase);
        Stream<Case> caseStream = cases.stream();

        when(caseRepository.streamCasesByDateRange(startDate, endDate)).thenReturn(caseStream);

        ResponseEntity<List<Case>> response = caseController.streamCasesByDateRange(startDate, endDate);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
}