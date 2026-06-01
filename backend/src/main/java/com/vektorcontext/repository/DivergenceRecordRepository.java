package com.vektorcontext.repository;

import com.vektorcontext.models.DivergenceRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DivergenceRecordRepository extends JpaRepository<DivergenceRecord, Long> {

    List<DivergenceRecord> findByDate(LocalDate date);

    List<DivergenceRecord> findByDateAndStoreCode(LocalDate date, Integer storeCode);
}