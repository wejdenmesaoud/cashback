package com.bezkoder.springjwt.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.bezkoder.springjwt.exception.InvalidDataException;
import com.bezkoder.springjwt.services.ExcelImportService;

class ExcelImportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExcelImportService excelImportService;

    @InjectMocks
    private ExcelImportController excelImportController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(excelImportController).build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testImportCasesFromExcel_Success() throws Exception {
        // Create a mock Excel file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test data".getBytes()
        );

        // Mock service responses
        when(excelImportService.isExcelFormat(any())).thenReturn(true);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", 2);
        result.put("errors", new ArrayList<String>());
        when(excelImportService.processExcelFile(any())).thenReturn(result);

        // Perform the request and validate
        mockMvc.perform(multipart("/api/excel/import-cases")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Successfully imported 2 cases"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testImportCasesFromExcel_WithErrors() throws Exception {
        // Create a mock Excel file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test data".getBytes()
        );

        // Mock service responses
        when(excelImportService.isExcelFormat(any())).thenReturn(true);

        Map<String, Object> result = new HashMap<>();
        result.put("totalRows", 1);
        List<String> errors = new ArrayList<>();
        errors.add("Error in row 2: Engineer name is required");
        result.put("errors", errors);
        when(excelImportService.processExcelFile(any())).thenReturn(result);

        // Perform the request and validate
        mockMvc.perform(multipart("/api/excel/import-cases")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Imported 1 cases with 1 errors:\n- Error in row 2: Engineer name is required\n"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testImportCasesFromExcel_EmptyFile() throws Exception {
        // Create an empty file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            new byte[0]
        );

        // Perform the request and validate
        mockMvc.perform(multipart("/api/excel/import-cases")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Please upload a file"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testImportCasesFromExcel_InvalidFormat() throws Exception {
        // Create a non-Excel file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.txt",
            "text/plain",
            "This is not an Excel file".getBytes()
        );

        // Mock service responses
        when(excelImportService.isExcelFormat(any())).thenReturn(false);

        // Perform the request and validate
        mockMvc.perform(multipart("/api/excel/import-cases")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Please upload an Excel file (XLSX format)"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testImportCasesFromExcel_ServiceException() throws Exception {
        // Create a mock Excel file
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.xlsx",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "test data".getBytes()
        );

        // Mock service responses
        when(excelImportService.isExcelFormat(any())).thenReturn(true);
        when(excelImportService.processExcelFile(any())).thenThrow(new InvalidDataException("Invalid data in Excel file"));

        // Perform the request and validate
        mockMvc.perform(multipart("/api/excel/import-cases")
                .file(file)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.message").value("Failed to import data: Invalid data in Excel file"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testDownloadTemplate() throws Exception {
        // Perform the request and validate
        mockMvc.perform(get("/api/excel/template"))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", "attachment; filename=case-import-template.csv"))
            .andExpect(content().contentType("text/csv"));
    }
}
