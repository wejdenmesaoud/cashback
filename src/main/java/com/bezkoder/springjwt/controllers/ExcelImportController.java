package com.bezkoder.springjwt.controllers;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.springjwt.models.Case;
import com.bezkoder.springjwt.payload.response.MessageResponse;
import com.bezkoder.springjwt.services.ExcelImportService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/excel")
@Tag(name = "Excel Import", description = "APIs for importing data from Excel files")
public class ExcelImportController {

    @Autowired
    private ExcelImportService excelImportService;

    @Operation(summary = "Download case import template", description = "Download a CSV template file for case imports")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Template downloaded successfully"),
        @ApiResponse(responseCode = "500", description = "Server error")
    })
    @GetMapping("/template")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> downloadTemplate() {
        try {
            Resource resource = new ClassPathResource("static/templates/case-import-template.txt");

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=case-import-template.csv");

            return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(resource);
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Failed to download template: " + e.getMessage()));
        }
    }

    @Operation(summary = "Import cases from Excel", description = "Import cases from Excel file. The first sheet will be processed.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully imported cases"),
        @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PostMapping("/import-cases")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> importCasesFromExcel(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Please upload a file"));
        }

        if (!excelImportService.isExcelFormat(file)) {
            return ResponseEntity
                .badRequest()
                .body(new MessageResponse("Please upload an Excel file (XLSX format)"));
        }

        try {
            Map<String, Object> result = excelImportService.processExcelFile(file);

            int totalRows = (int) result.get("totalRows");
            List<String> errors = (List<String>) result.get("errors");

            if (errors.isEmpty()) {
                return ResponseEntity.ok(new MessageResponse("Successfully imported " + totalRows + " cases"));
            } else {
                StringBuilder message = new StringBuilder("Imported " + totalRows + " cases with " + errors.size() + " errors:\n");
                for (String error : errors) {
                    message.append("- ").append(error).append("\n");
                }
                return ResponseEntity.ok(new MessageResponse(message.toString()));
            }
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("Failed to import data: " + e.getMessage()));
        }
    }
}
