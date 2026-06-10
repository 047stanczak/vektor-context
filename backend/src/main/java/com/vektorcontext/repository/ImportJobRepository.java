package com.vektorcontext.repository;

import com.vektorcontext.models.ImportJob;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ImportJobRepository extends JpaRepository<ImportJob, Long> {
    List<ImportJob> findAllByOrderByIdDesc();
}