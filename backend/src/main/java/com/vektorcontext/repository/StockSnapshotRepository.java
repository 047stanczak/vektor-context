package com.vektorcontext.repository;

import com.vektorcontext.models.StockSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
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

    @Query("SELECT DISTINCT s.product.brand FROM StockSnapshot s WHERE CAST(s.capturedAt AS DATE) = :today AND s.currentStock > 0 AND s.product.brand IS NOT NULL ORDER BY s.product.brand")
    List<String> findDistinctBrandsToday(@Param("today") LocalDate today);

    @Query("SELECT s FROM StockSnapshot s WHERE s.product.brand = :brand AND CAST(s.capturedAt AS DATE) = :today AND s.currentStock > 0")
    List<StockSnapshot> findByBrandToday(@Param("brand") String brand, @Param("today") LocalDate today);

    @Query("SELECT DISTINCT s.product.department FROM StockSnapshot s WHERE CAST(s.capturedAt AS DATE) = :today AND s.currentStock > 0 AND s.product.department IS NOT NULL ORDER BY s.product.department")
    List<String> findDistinctDepartmentsToday(@Param("today") LocalDate today);

    @Query("SELECT s FROM StockSnapshot s WHERE s.product.department = :department AND CAST(s.capturedAt AS DATE) = :today AND s.currentStock > 0")
    List<StockSnapshot> findByDepartmentToday(@Param("department") String department, @Param("today") LocalDate today);

    @Query("SELECT s FROM StockSnapshot s WHERE s.product.barcode = :q AND CAST(s.capturedAt AS DATE) = :today AND s.currentStock > 0")
    List<StockSnapshot> searchByBarcodeToday(@Param("q") String q, @Param("today") LocalDate today);

    @Query("SELECT s FROM StockSnapshot s WHERE s.productCode = :code AND CAST(s.capturedAt AS DATE) = :today AND s.currentStock > 0")
    List<StockSnapshot> searchByProductCodeToday(@Param("code") Integer code, @Param("today") LocalDate today);

}