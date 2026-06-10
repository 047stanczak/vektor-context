package com.vektorcontext.repository;

import com.vektorcontext.dto.DivergenceRankingItem;
import com.vektorcontext.models.DivergenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface DivergenceRecordRepository extends JpaRepository<DivergenceRecord, Long> {

    List<DivergenceRecord> findByDate(LocalDate date);

    List<DivergenceRecord> findByDateAndStoreCode(LocalDate date, Integer storeCode);

    @Query("""
        SELECT new com.vektorcontext.dto.DivergenceRankingItem(d.separatorName, COUNT(d))
        FROM DivergenceRecord d
        WHERE d.date BETWEEN :from AND :to
        AND d.separatorName IS NOT NULL
        GROUP BY d.separatorName
        ORDER BY COUNT(d) DESC
    """)
    List<DivergenceRankingItem> rankBySeparator(
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

    @Query("""
        SELECT new com.vektorcontext.dto.DivergenceRankingItem(CONCAT(p.description, ' ', COALESCE(p.complement, '')), COUNT(d))
        FROM DivergenceRecord d
        JOIN Product p ON p.code = d.productCode
        WHERE d.date BETWEEN :from AND :to
        GROUP BY p.description, p.complement
        ORDER BY COUNT(d) DESC
    """)
    List<DivergenceRankingItem> rankByProduct(
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

}