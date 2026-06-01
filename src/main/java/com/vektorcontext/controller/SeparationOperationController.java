package com.vektorcontext.controller;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.repository.SeparationOperationRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/separation-operations")
public class SeparationOperationController {

    private final SeparationOperationRepository separationOperationRepository;

    public SeparationOperationController(SeparationOperationRepository separationOperationRepository) {
        this.separationOperationRepository = separationOperationRepository;
    }


    @GetMapping("/separators")
    public ResponseEntity<ApiResponse<List<String>>> separators() {
        List<String> names = separationOperationRepository.findDistinctSeparatorNames();
        return ResponseEntity.ok(ApiResponse.ok("OK", names));
    }
}