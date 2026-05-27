package com.vektorcontext.repository;

import com.vektorcontext.models.SeparationOperation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeparationOperationRepository
        extends JpaRepository<SeparationOperation, String> {
}