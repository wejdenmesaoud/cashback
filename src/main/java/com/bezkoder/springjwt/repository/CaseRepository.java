package com.bezkoder.springjwt.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bezkoder.springjwt.models.Case;
import com.bezkoder.springjwt.models.Engineer;
import com.bezkoder.springjwt.models.Report;

@Repository
public interface CaseRepository extends JpaRepository<Case, Long> {
    List<Case> findByEngineer(Engineer engineer);
    
    List<Case> findByReport(Report report);
    
    List<Case> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT c FROM Case c WHERE c.engineer = :engineer AND c.date BETWEEN :startDate AND :endDate")
    List<Case> findByEngineerAndDateBetween(@Param("engineer") Engineer engineer, 
                                           @Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT c FROM Case c WHERE c.engineer.team.id = :teamId")
    List<Case> findByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT c FROM Case c WHERE c.date BETWEEN :startDate AND :endDate")
    Stream<Case> streamCasesByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(c) FROM Case c WHERE c.engineer = :engineer AND c.date BETWEEN :startDate AND :endDate")
    Long countCasesResolvedByEngineer(@Param("engineer") Engineer engineer,
                                     @Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate);

    @Query("SELECT AVG(c.cesRating) FROM Case c WHERE c.engineer = :engineer AND c.date BETWEEN :startDate AND :endDate")
    Double calculateAverageCesRating(@Param("engineer") Engineer engineer,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
}