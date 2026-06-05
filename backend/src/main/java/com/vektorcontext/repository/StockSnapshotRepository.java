package com.vektorcontext.repository;

import com.vektorcontext.models.StockSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Optional;

public interface StockSnapshotRepository extends JpaRepository<StockSnapshot, Long> {

    Optional<StockSnapshot> findTopByProductCodeOrderByCapturedAtDesc(Integer productCode);

    @Query("SELECT s FROM StockSnapshot s " +
           "WHERE s.productCode = :productCode " +
           "AND CAST(s.capturedAt AS DATE) = :date " +
           "ORDER BY s.capturedAt DESC " +
           "LIMIT 1")
    Optional<StockSnapshot> findByProductCodeAndDate(@Param("productCode") Integer productCode, 
                                                     @Param("date") LocalDate date);
}