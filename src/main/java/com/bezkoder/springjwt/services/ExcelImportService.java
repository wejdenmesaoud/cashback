package com.bezkoder.springjwt.services;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.bezkoder.springjwt.exception.InvalidDataException;
import com.bezkoder.springjwt.models.Case;
import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.repository.CaseRepository;
import com.bezkoder.springjwt.repository.EngineerRepository;

/**
 * Service for importing data from Excel files.
 *
 * Expected Excel format for case imports:
 * Column A: Engineer Full Name (required)
 * Column B: Time Hierarchy (Day)
 * Column C: SAP Case ID
 * Column D: Case Description (required)
 * Column E: Top Contract Type
 * Column F: Survey Source (required, must be "Case" or "Chat" only)
 * Column G: Customer Effort Score (CES) Rating (must be between 1 and 5)
 * Column H: CES Driver - Correct Solution
 * Column I: CES Driver - Timely Updates
 * Column J: CES Driver - Timely Solution
 * Column K: CES Driver - Professionalism
 * Column L: CES Driver - Expertise
 * Column M: Chat Session ID
 * Column N: Survey Feedback
 * Column O: Manager Name (optional, will use "Default Manager" if not provided)
 *
 * Validation rules:
 * - Engineer Full Name and Case Description are required
 * - Survey Source must be either "Case" or "Chat"
 * - CES Rating must be between 1 and 5
 * - Manager field will be set to "Default Manager" if not provided in the Excel
 */
@Service
public class ExcelImportService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);

    @Autowired
    private EngineerRepository engineerRepository;

    @Autowired
    private CaseRepository caseRepository;

    public static final String TYPE = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    public boolean isExcelFormat(MultipartFile file) {
        return TYPE.equals(file.getContentType());
    }

    public Map<String, Object> processExcelFile(MultipartFile file) {
        try {
            Map<String, Object> result = new HashMap<>();
            List<Case> importedCases = new ArrayList<>();
            List<String> errors = new ArrayList<>();

            try (InputStream is = file.getInputStream();
                 Workbook workbook = new XSSFWorkbook(is)) {

                // Get the first sheet only
                Sheet sheet = workbook.getSheetAt(0);

                // Skip header row
                boolean isFirstRow = true;
                int rowCount = 0;

                for (Row row : sheet) {
                    if (isFirstRow) {
                        isFirstRow = false;
                        continue;
                    }

                    try {
                        Case importedCase = processRow(row);
                        if (importedCase != null) {
                            importedCases.add(importedCase);
                            rowCount++;
                        }
                    } catch (Exception e) {
                        errors.add("Error in row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                        logger.error("Error processing row {}: {}", row.getRowNum() + 1, e.getMessage());
                    }
                }

                result.put("totalRows", rowCount);
                result.put("importedCases", importedCases);
                result.put("errors", errors);

                return result;
            }
        } catch (IOException e) {
            throw new InvalidDataException("Failed to process Excel file: " + e.getMessage());
        }
    }

    private Case processRow(Row row) {
        // Extract data from the row based on the new Excel structure
        String engineerFullName = getCellValueAsString(row.getCell(0));
        String timeHierarchy = getCellValueAsString(row.getCell(1));
        String sapCaseId = getCellValueAsString(row.getCell(2));
        String caseDescription = getCellValueAsString(row.getCell(3));
        String topContractType = getCellValueAsString(row.getCell(4));
        String surveySource = getCellValueAsString(row.getCell(5));
        Integer cesRating = getCellValueAsInteger(row.getCell(6));
        Integer cesDriverCorrectSolution = getCellValueAsInteger(row.getCell(7));
        Integer cesDriverTimelyUpdates = getCellValueAsInteger(row.getCell(8));
        Integer cesDriverTimelySolution = getCellValueAsInteger(row.getCell(9));
        Integer cesDriverProfessionalism = getCellValueAsInteger(row.getCell(10));
        Integer cesDriverExpertise = getCellValueAsInteger(row.getCell(11));
        String chatSessionId = getCellValueAsString(row.getCell(12));
        String surveyFeedback = getCellValueAsString(row.getCell(13));
        String managerName = getCellValueAsString(row.getCell(14)); // Manager name in column O

        // Validate required fields
        if (engineerFullName == null || engineerFullName.trim().isEmpty()) {
            throw new InvalidDataException("Engineer name is required");
        }

        if (caseDescription == null || caseDescription.trim().isEmpty()) {
            throw new InvalidDataException("Case description is required");
        }

        // Validate survey source (should be "Case" or "Chat")
        if (surveySource != null && !surveySource.trim().isEmpty()) {
            if (!surveySource.equals("Case") && !surveySource.equals("Chat")) {
                throw new InvalidDataException("Survey source must be 'Case' or 'Chat', but got '" + surveySource + "'");
            }
        }

        // Validate CES rating (should be between 1 and 5)
        if (cesRating != null) {
            if (cesRating < 1 || cesRating > 5) {
                throw new InvalidDataException("CES rating must be between 1 and 5, but got " + cesRating);
            }
        }

        // Find or create engineer
        Engineer engineer = engineerRepository.findByFullName(engineerFullName);
        if (engineer == null) {
            engineer = new Engineer();
            engineer.setFullName(engineerFullName);

            // Use manager from Excel if available, otherwise use default
            if (managerName != null && !managerName.trim().isEmpty()) {
                engineer.setManager(managerName);
            } else {
                engineer.setManager("Default Manager"); // Set a non-empty default manager to satisfy @NotBlank constraint
            }

            engineer = engineerRepository.save(engineer);
            logger.info("Created new engineer: {} with manager: {}", engineerFullName, engineer.getManager());
        } else {
            // Check if existing engineer has a manager, if not, set a default one
            if (engineer.getManager() == null || engineer.getManager().trim().isEmpty()) {
                if (managerName != null && !managerName.trim().isEmpty()) {
                    engineer.setManager(managerName);
                } else {
                    engineer.setManager("Default Manager");
                }
                engineer = engineerRepository.save(engineer);
                logger.info("Updated existing engineer: {} with manager: {}", engineerFullName, engineer.getManager());
            }
        }

        // Create new case with all the fields
        Case newCase = new Case();
        newCase.setCaseDescription(caseDescription);
        newCase.setSurveySource(surveySource);
        newCase.setCesRating(cesRating);
        newCase.setSapCaseId(sapCaseId);
        newCase.setTopContractType(topContractType);
        newCase.setCesDriverCorrectSolution(cesDriverCorrectSolution);
        newCase.setCesDriverTimelyUpdates(cesDriverTimelyUpdates);
        newCase.setCesDriverTimelySolution(cesDriverTimelySolution);
        newCase.setCesDriverProfessionalism(cesDriverProfessionalism);
        newCase.setCesDriverExpertise(cesDriverExpertise);
        newCase.setChatSessionId(chatSessionId);
        newCase.setSurveyFeedback(surveyFeedback);

        // Try to parse date from timeHierarchy if possible, otherwise use current date
        LocalDate caseDate = parseTimeHierarchy(timeHierarchy);
        // Convert LocalDate to LocalDateTime by setting time to start of day
        newCase.setDate(caseDate != null ? caseDate.atStartOfDay() : LocalDateTime.now());

        newCase.setEngineer(engineer);

        // Save the case
        return caseRepository.save(newCase);
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue());
                } catch (NumberFormatException e) {
                    return null;
                }
            default:
                return null;
        }
    }

    /**
     * Parse date from time hierarchy string like "Jan 7, 2025 (2025)" or "Feb 12, 2025 (2025)"
     *
     * @param timeHierarchy The time hierarchy string from Excel
     * @return LocalDate object if parsing successful, null otherwise
     */
    private LocalDate parseTimeHierarchy(String timeHierarchy) {
        if (timeHierarchy == null || timeHierarchy.trim().isEmpty()) {
            return null;
        }

        try {
            // Handle special case for "Time Hierarchy (Day)" header
            if (timeHierarchy.equals("Time Hierarchy (Day)")) {
                return null;
            }

            // Extract the date part before the parentheses
            String datePart = timeHierarchy;
            int parenthesisIndex = timeHierarchy.indexOf('(');
            if (parenthesisIndex > 0) {
                datePart = timeHierarchy.substring(0, parenthesisIndex).trim();
            }

            // Parse the date using DateTimeFormatter
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM d, yyyy");
            return LocalDate.parse(datePart, formatter);
        } catch (Exception e) {
            logger.warn("Failed to parse date from time hierarchy: {}", timeHierarchy);
            return null;
        }
    }
}
