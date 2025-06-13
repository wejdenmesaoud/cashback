package com.bezkoder.springjwt.models;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "bonuses")
public class Bonus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private BigDecimal amount;

    @NotNull
    private LocalDate calculationDate;

    @NotNull
    private LocalDate startPeriod;

    @NotNull
    private LocalDate endPeriod;

    @ManyToOne
    @JoinColumn(name = "engineer_id")
    private Engineer engineer;

    public Bonus() {
    }

    public Bonus(BigDecimal amount, LocalDate calculationDate, LocalDate startPeriod, LocalDate endPeriod, Engineer engineer) {
        this.amount = amount;
        this.calculationDate = calculationDate;
        this.startPeriod = startPeriod;
        this.endPeriod = endPeriod;
        this.engineer = engineer;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public LocalDate getCalculationDate() {
        return calculationDate;
    }

    public void setCalculationDate(LocalDate calculationDate) {
        this.calculationDate = calculationDate;
    }

    public LocalDate getStartPeriod() {
        return startPeriod;
    }

    public void setStartPeriod(LocalDate startPeriod) {
        this.startPeriod = startPeriod;
    }

    public LocalDate getEndPeriod() {
        return endPeriod;
    }

    public void setEndPeriod(LocalDate endPeriod) {
        this.endPeriod = endPeriod;
    }

    public Engineer getEngineer() {
        return engineer;
    }

    public void setEngineer(Engineer engineer) {
        this.engineer = engineer;
    }
}
