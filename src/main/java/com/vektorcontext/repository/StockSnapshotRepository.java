package com.vektorcontext.repository;

import com.vektorcontext.models.StockSnapshot;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockSnapshotRepository extends JpaRepository<StockSnapshot, Long> {
}
