package com.vektorcontext.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.vektorcontext.models.ImportJob;
import com.vektorcontext.services.ImportJobService;

@RestController
public class JobController {

    private final ImportJobService importJobService;

    public JobController(ImportJobService importJobService) {
        this.importJobService = importJobService;
    }
    
    @GetMapping("/status/{jobId}")
    public ResponseEntity<ImportJob> status(@PathVariable Long jobId) {
        return ResponseEntity.accepted().body(importJobService.findById(jobId));
    }

}
