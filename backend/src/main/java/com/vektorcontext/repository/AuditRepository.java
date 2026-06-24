package com.vektorcontext.repository;

import com.vektorcontext.models.Audit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditRepository extends JpaRepository<Audit, Long> {
    List<Audit> findAllByOrderByCreatedAtDesc();
}