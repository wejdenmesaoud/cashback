package com.bezkoder.springjwt.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.springjwt.exception.InvalidDataException;
import com.bezkoder.springjwt.models.Case;
import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.repository.CaseRepository;
import com.bezkoder.springjwt.repository.EngineerRepository;

class ExcelImportServiceTest {

    @Mock
    private EngineerRepository engineerRepository;
    
    @Mock
    private CaseRepository caseRepository;
    
    @InjectMocks
    private ExcelImportService excelImportService;
    
    private MultipartFile validExcelFile;
    private MultipartFile invalidFormatFile;
    
    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        // Create a valid Excel file for testing
        validExcelFile = createValidExcelFile();
        
        // Create an invalid format file
        invalidFormatFile = new MockMultipartFile(
            "test.txt", 
            "test.txt", 
            "text/plain", 
            "This is not an Excel file".getBytes()
        );
        
        // Mock engineer repository
        Engineer mockEngineer = new Engineer();
        mockEngineer.setId(1L);
        mockEngineer.setFullName("Alice Johnson");
        
        when(engineerRepository.findByFullName("Alice Johnson")).thenReturn(mockEngineer);
        when(engineerRepository.findByFullName("Bob Smith")).thenReturn(null);
        when(engineerRepository.save(any(Engineer.class))).thenAnswer(invocation -> {
            Engineer engineer = invocation.getArgument(0);
            if (engineer.getId() == null) {
                engineer.setId(2L);
            }
            return engineer;
        });
        
        // Mock case repository
        when(caseRepository.save(any(Case.class))).thenAnswer(invocation -> {
            Case caseObj = invocation.getArgument(0);
            if (caseObj.getId() == null) {
                caseObj.setId(1L);
            }
            return caseObj;
        });
    }
    
    @Test
    void testIsExcelFormat() {
        assertTrue(excelImportService.isExcelFormat(validExcelFile));
        assertFalse(excelImportService.isExcelFormat(invalidFormatFile));
    }
    
    private MultipartFile createValidExcelFile() throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet("Cases");
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Engineer", "Case Description", "Survey Source", "CES Rating"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }
            
            // Create data rows
            // Row 1: Existing engineer
            Row dataRow1 = sheet.createRow(1);
            dataRow1.createCell(0).setCellValue("Alice Johnson");
            dataRow1.createCell(1).setCellValue("SAP JVM patch update during upgrade");
            dataRow1.createCell(2).setCellValue("Case");
            dataRow1.createCell(3).setCellValue(5);
            
            // Row 2: New engineer
            Row dataRow2 = sheet.createRow(2);
            dataRow2.createCell(0).setCellValue("Bob Smith");
            dataRow2.createCell(1).setCellValue("Network connectivity issue");
            dataRow2.createCell(2).setCellValue("Email");
            dataRow2.createCell(3).setCellValue(4);
            
            workbook.write(out);
            
            return new MockMultipartFile(
                "test.xlsx",
                "test.xlsx",
                ExcelImportService.TYPE,
                new ByteArrayInputStream(out.toByteArray())
            );
        }
    }
}
