package com.vektorcontext.controller;

import com.vektorcontext.dto.SeparationProductDTO;
import com.vektorcontext.models.SeparationProduct;
import com.vektorcontext.repository.SeparationProductRepository;
import com.vektorcontext.services.TransactionFinder;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
public class SeparationProductController {

    private final SeparationProductRepository repository;
    private final TransactionFinder transactionFinder;

    public SeparationProductController(SeparationProductRepository repository, TransactionFinder transactionFinder) {
        this.repository = repository;
        this.transactionFinder = transactionFinder;
    }

    @GetMapping("/old-pending")
    public ResponseEntity<List<SeparationProductDTO>> oldPending(
        @RequestParam(defaultValue = "15") int days
    ) {
        return ResponseEntity.ok(
            repository.findOldPending(LocalDate.now(), LocalDate.now().minusDays(days))
                .stream().map(SeparationProductDTO::from).toList()
        );
    }

    @GetMapping("/old-pending-with-stock")
    public ResponseEntity<List<SeparationProductDTO>> oldPendingWithStock(
        @RequestParam(defaultValue = "15") int days
    ) {
        return ResponseEntity.ok(
            repository.findOldPendingWithStock(
                LocalDate.now(),
                LocalDate.now().minusDays(days),
                LocalDate.now().atStartOfDay()
            ).stream().map(SeparationProductDTO::from).toList()
        );
    }

    @GetMapping("/old-pending-no-stock")
    public ResponseEntity<List<SeparationProductDTO>> oldPendingNoStock(
        @RequestParam(defaultValue = "15") int days
    ) {
        return ResponseEntity.ok(
            repository.findOldPendingNoStock(
                LocalDate.now(),
                LocalDate.now().minusDays(days),
                LocalDate.now().atStartOfDay()
            ).stream().map(SeparationProductDTO::from).toList()
        );
    }

    @GetMapping("/pending-by-barcode")
    public ResponseEntity<List<SeparationProductDTO>> pendingByBarcode(@RequestParam String barcode) {
        return ResponseEntity.ok(
            repository.findPendingByBarcode(barcode, LocalDate.now())
                .stream()
                .map(product -> SeparationProductDTO.from(product, transactionFinder.findCurrentStock(product.getProductCode())))
                .toList()
        );
    }

    @GetMapping("/pending-by-code")
    public ResponseEntity<List<SeparationProductDTO>> pendingByCode(@RequestParam String code) {
        List<SeparationProduct> products;
        try {
            products = repository.findPendingByProductCode(Integer.parseInt(code), LocalDate.now());
        } catch (NumberFormatException e) {
            products = repository.findPendingByBarcode(code, LocalDate.now());
        }
        return ResponseEntity.ok(products.stream()
            .map(product -> SeparationProductDTO.from(product, transactionFinder.findCurrentStock(product.getProductCode())))
            .toList());
    }

}