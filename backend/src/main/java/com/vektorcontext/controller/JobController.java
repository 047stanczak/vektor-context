package com.vektorcontext.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vektorcontext.models.ImportJob;
import com.vektorcontext.services.ImportJobService;

@RestController
@RequestMapping("/api")
public class JobController {

    private final ImportJobService importJobService;

    public JobController(ImportJobService importJobService) {
        this.importJobService = importJobService;
    }
    
    @GetMapping("/status")
        public ResponseEntity<List<ImportJob>> all() {
            return ResponseEntity.ok(importJobService.findAll());
        }
    
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ImportJob> status(@PathVariable Long jobId) {
        ImportJob job = importJobService.findById(jobId);
        if (job == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(job);
    }

}
