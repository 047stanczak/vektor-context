package com.vektorcontext.controller;

import com.vektorcontext.dto.CountingItemDTO;
import com.vektorcontext.models.StockSnapshot;
import com.vektorcontext.repository.StockSnapshotRepository;
import com.vektorcontext.services.PdfCountingService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/counting")
public class CountingController {

    private final StockSnapshotRepository repository;
    private final PdfCountingService pdfCountingService;

    public CountingController(StockSnapshotRepository repository, PdfCountingService pdfCountingService) {
        this.repository = repository;
        this.pdfCountingService = pdfCountingService;
    }

    @GetMapping("/brands")
    public ResponseEntity<List<String>> brands() {
        return ResponseEntity.ok(repository.findDistinctBrandsToday(LocalDate.now()));
    }

    @GetMapping("/by-brand")
    public ResponseEntity<List<CountingItemDTO>> byBrand(@RequestParam String brand) {
        return ResponseEntity.ok(deduplicated(repository.findByBrandToday(brand, LocalDate.now())));
    }

    @GetMapping("/by-product")
    public ResponseEntity<List<CountingItemDTO>> byProduct(@RequestParam Integer productCode) {
        return repository.findTopByProductCodeOrderByCapturedAtDesc(productCode)
            .map(s -> {
                String brand = s.getProduct() != null ? s.getProduct().getBrand() : null;
                if (brand == null) return ResponseEntity.ok(List.<CountingItemDTO>of());
                return ResponseEntity.ok(deduplicated(repository.findByBrandToday(brand, LocalDate.now())));
            })
            .orElse(ResponseEntity.ok(List.of()));
    }

    private List<CountingItemDTO> deduplicated(List<StockSnapshot> snapshots) {
        return snapshots.stream()
            .collect(Collectors.toMap(
                StockSnapshot::getProductCode,
                s -> s,
                (a, b) -> a.getCapturedAt().isAfter(b.getCapturedAt()) ? a : b
            ))
            .values().stream()
            .map(CountingItemDTO::from)
            .sorted(Comparator.comparing(s -> s.getDescription() != null ? s.getDescription() : ""))
            .toList();
    }

    @GetMapping("/brand-by-product")
    public ResponseEntity<String> brandByProduct(@RequestParam Integer productCode) {
        return repository.findTopByProductCodeOrderByCapturedAtDesc(productCode)
            .map(s -> ResponseEntity.ok(s.getProduct() != null ? s.getProduct().getBrand() : ""))
            .orElse(ResponseEntity.ok(""));
    }

    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> reportPdf(@RequestParam String brand) {
        List<CountingItemDTO> items = deduplicated(repository.findByBrandToday(brand, LocalDate.now()));
        byte[] pdf = pdfCountingService.generate(brand, items);
        return ResponseEntity.ok()
            .header("Content-Type", "application/pdf")
            .header("Content-Disposition", "inline; filename=\"contagem-" + brand + ".pdf\"")
            .body(pdf);
    }

    @GetMapping("/departments")
    public ResponseEntity<List<String>> departments() {
        return ResponseEntity.ok(repository.findDistinctDepartmentsToday(LocalDate.now()));
    }

    @GetMapping("/by-department")
    public ResponseEntity<List<CountingItemDTO>> byDepartment(@RequestParam String department) {
        return ResponseEntity.ok(deduplicated(repository.findByDepartmentToday(department, LocalDate.now())));
    }

}