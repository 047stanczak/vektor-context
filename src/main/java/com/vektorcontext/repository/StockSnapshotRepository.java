package com.vektorcontext.repository;

import com.vektorcontext.models.StockSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StockSnapshotRepository extends JpaRepository<StockSnapshot, Long> {

    Optional<StockSnapshot> findTopByProductCodeOrderByCapturedAtDesc(Integer productCode);
}