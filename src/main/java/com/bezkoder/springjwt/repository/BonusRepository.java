package com.bezkoder.springjwt.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bezkoder.springjwt.models.Bonus;
import com.bezkoder.springjwt.models.Engineer;

@Repository
public interface BonusRepository extends JpaRepository<Bonus, Long> {
    List<Bonus> findByEngineer(Engineer engineer);
    
    List<Bonus> findByCalculationDateBetween(LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT b FROM Bonus b WHERE b.engineer = :engineer AND b.calculationDate BETWEEN :startDate AND :endDate")
    List<Bonus> findByEngineerAndCalculationDateBetween(
        @Param("engineer") Engineer engineer, 
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT b FROM Bonus b WHERE b.engineer.team.id = :teamId")
    List<Bonus> findByTeamId(@Param("teamId") Long teamId);
}
