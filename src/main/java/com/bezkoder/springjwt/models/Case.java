package com.bezkoder.springjwt.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "cases")
public class Case {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 1000)
    private String caseDescription;

    @NotNull
    private LocalDateTime date;

    private Integer cesRating;

    @Size(max = 50)
    private String surveySource;

    @Size(max = 50)
    private String sapCaseId;

    @Size(max = 50)
    private String topContractType;

    private Integer cesDriverCorrectSolution;

    private Integer cesDriverTimelyUpdates;

    private Integer cesDriverTimelySolution;

    private Integer cesDriverProfessionalism;

    private Integer cesDriverExpertise;

    @Size(max = 100)
    private String chatSessionId;

    @Column(length = 2000)
    private String surveyFeedback;

    @ManyToOne
    @JoinColumn(name = "engineer_id")
    private Engineer engineer;

    @ManyToOne
    @JoinColumn(name = "report_id")
    private Report report;

    public Case() {
    }

    public Case(String caseDescription, LocalDateTime date, Integer cesRating, String surveySource, Engineer engineer) {
        this.caseDescription = caseDescription;
        this.date = date;
        this.cesRating = cesRating;
        this.surveySource = surveySource;
        this.engineer = engineer;
    }

    public Case(String caseDescription, LocalDateTime date, Integer cesRating, String surveySource,
                String sapCaseId, String topContractType, Engineer engineer,
                Integer cesDriverCorrectSolution, Integer cesDriverTimelyUpdates,
                Integer cesDriverTimelySolution, Integer cesDriverProfessionalism,
                Integer cesDriverExpertise, String chatSessionId, String surveyFeedback) {
        this.caseDescription = caseDescription;
        this.date = date;
        this.cesRating = cesRating;
        this.surveySource = surveySource;
        this.sapCaseId = sapCaseId;
        this.topContractType = topContractType;
        this.engineer = engineer;
        this.cesDriverCorrectSolution = cesDriverCorrectSolution;
        this.cesDriverTimelyUpdates = cesDriverTimelyUpdates;
        this.cesDriverTimelySolution = cesDriverTimelySolution;
        this.cesDriverProfessionalism = cesDriverProfessionalism;
        this.cesDriverExpertise = cesDriverExpertise;
        this.chatSessionId = chatSessionId;
        this.surveyFeedback = surveyFeedback;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCaseDescription() {
        return caseDescription;
    }

    public void setCaseDescription(String caseDescription) {
        this.caseDescription = caseDescription;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public Integer getCesRating() {
        return cesRating;
    }

    public void setCesRating(Integer cesRating) {
        this.cesRating = cesRating;
    }

    public String getSurveySource() {
        return surveySource;
    }

    public void setSurveySource(String surveySource) {
        this.surveySource = surveySource;
    }

    public Engineer getEngineer() {
        return engineer;
    }

    public void setEngineer(Engineer engineer) {
        this.engineer = engineer;
    }

    public Report getReport() {
        return report;
    }

    public void setReport(Report report) {
        this.report = report;
    }

    public String getSapCaseId() {
        return sapCaseId;
    }

    public void setSapCaseId(String sapCaseId) {
        this.sapCaseId = sapCaseId;
    }

    public String getTopContractType() {
        return topContractType;
    }

    public void setTopContractType(String topContractType) {
        this.topContractType = topContractType;
    }

    public Integer getCesDriverCorrectSolution() {
        return cesDriverCorrectSolution;
    }

    public void setCesDriverCorrectSolution(Integer cesDriverCorrectSolution) {
        this.cesDriverCorrectSolution = cesDriverCorrectSolution;
    }

    public Integer getCesDriverTimelyUpdates() {
        return cesDriverTimelyUpdates;
    }

    public void setCesDriverTimelyUpdates(Integer cesDriverTimelyUpdates) {
        this.cesDriverTimelyUpdates = cesDriverTimelyUpdates;
    }

    public Integer getCesDriverTimelySolution() {
        return cesDriverTimelySolution;
    }

    public void setCesDriverTimelySolution(Integer cesDriverTimelySolution) {
        this.cesDriverTimelySolution = cesDriverTimelySolution;
    }

    public Integer getCesDriverProfessionalism() {
        return cesDriverProfessionalism;
    }

    public void setCesDriverProfessionalism(Integer cesDriverProfessionalism) {
        this.cesDriverProfessionalism = cesDriverProfessionalism;
    }

    public Integer getCesDriverExpertise() {
        return cesDriverExpertise;
    }

    public void setCesDriverExpertise(Integer cesDriverExpertise) {
        this.cesDriverExpertise = cesDriverExpertise;
    }

    public String getChatSessionId() {
        return chatSessionId;
    }

    public void setChatSessionId(String chatSessionId) {
        this.chatSessionId = chatSessionId;
    }

    public String getSurveyFeedback() {
        return surveyFeedback;
    }

    public void setSurveyFeedback(String surveyFeedback) {
        this.surveyFeedback = surveyFeedback;
    }
}