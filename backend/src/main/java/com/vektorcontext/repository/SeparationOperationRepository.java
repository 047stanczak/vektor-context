package com.vektorcontext.repository;

import com.vektorcontext.models.SeparationOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SeparationOperationRepository extends JpaRepository<SeparationOperation, String> {

    @Query("SELECT DISTINCT s.userName FROM SeparationOperation s " +
           "WHERE s.userName IS NOT NULL AND s.userName != 'manual' " +
           "ORDER BY s.userName ASC")
    List<String> findDistinctSeparatorNames();
}