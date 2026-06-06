package com.vektorcontext.controller;

import com.vektorcontext.models.SeparationProduct;
import com.vektorcontext.repository.SeparationProductRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SeparationProductController {

    private final SeparationProductRepository repository;

    public SeparationProductController(SeparationProductRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/old-pending")
    public ResponseEntity<List<SeparationProduct>> oldPending() {
        return ResponseEntity.ok(
            repository.findOldPending(LocalDate.now(), LocalDate.now().minusDays(15))
        );
    }

    @GetMapping("/old-pending-with-stock")
    public ResponseEntity<List<SeparationProduct>> oldPendingWithStock() {
        return ResponseEntity.ok(
            repository.findOldPendingWithStock(
                LocalDate.now(),
                LocalDate.now().minusDays(15),
                LocalDate.now().atStartOfDay()
            )
        );
    }
}