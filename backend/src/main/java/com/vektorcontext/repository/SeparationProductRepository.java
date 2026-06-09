package com.vektorcontext.repository;

import com.vektorcontext.models.SeparationProduct;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SeparationProductRepository extends JpaRepository<SeparationProduct, Long> {
    @Query("SELECT s FROM SeparationProduct s WHERE s.snapshotDate = :today AND s.releaseDate <= :limit")
    List<SeparationProduct> findOldPending(@Param("today") LocalDate today, @Param("limit") LocalDate limit);

    @Query("""
        SELECT s FROM SeparationProduct s
        WHERE s.snapshotDate = :today
        AND s.releaseDate <= :limit
        AND EXISTS (
            SELECT 1 FROM StockSnapshot ss
            WHERE ss.productCode = s.productCode
            AND ss.capturedAt >= :todayStart
            AND ss.currentStock > 0
        )
    """)
    List<SeparationProduct> findOldPendingWithStock(
        @Param("today") LocalDate today,
        @Param("limit") LocalDate limit,
        @Param("todayStart") LocalDateTime todayStart
    );

    @Query("""
        SELECT s FROM SeparationProduct s
        WHERE s.snapshotDate = :today
        AND s.releaseDate <= :limit
        AND NOT EXISTS (
            SELECT 1 FROM StockSnapshot ss
            WHERE ss.productCode = s.productCode
            AND ss.capturedAt >= :todayStart
            AND ss.currentStock > 0
        )
    """)
    List<SeparationProduct> findOldPendingNoStock(
        @Param("today") LocalDate today,
        @Param("limit") LocalDate limit,
        @Param("todayStart") LocalDateTime todayStart
    );
}