package com.vektorcontext.controller;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.dto.DivergenceQueryResponse;
import com.vektorcontext.dto.DivergenceRankingItem;
import com.vektorcontext.dto.DivergenceRecordRequest;
import com.vektorcontext.dto.DivergenceRecordResponse;
import com.vektorcontext.repository.DivergenceRecordRepository;
import com.vektorcontext.services.DivergenceService;
import com.vektorcontext.services.PdfReportService;

import jakarta.validation.Valid;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/divergence")
public class DivergenceController {

    private final DivergenceService divergenceService;
    private final PdfReportService pdfReportService;
    private final DivergenceRecordRepository divergenceRecordRepository;

    public DivergenceController(DivergenceService divergenceService,
                                PdfReportService pdfReportService,
                            DivergenceRecordRepository divergenceRecordRepository) {
        this.divergenceService = divergenceService;
        this.pdfReportService = pdfReportService;
        this.divergenceRecordRepository = divergenceRecordRepository;
    }


    @GetMapping("/query")
    public ResponseEntity<ApiResponse<DivergenceQueryResponse>> query(
            @RequestParam(required = false) Integer productCode,
            @RequestParam(required = false) String barcode,
            @RequestParam Integer storeCode
    ) {
        if (productCode == null && barcode == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "Informe productCode ou barcode"));
        }

        try {
            DivergenceQueryResponse data = productCode != null
                    ? divergenceService.queryByProductCode(productCode, storeCode)
                    : divergenceService.queryByBarcode(barcode, storeCode);

            return ResponseEntity.ok(ApiResponse.ok("Produto encontrado", data));

        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }


    @PostMapping
    public ResponseEntity<ApiResponse<List<DivergenceRecordResponse>>> saveAll(
            @RequestBody @Valid List<DivergenceRecordRequest> requests
    ) {
        List<DivergenceRecordResponse> saved = divergenceService.saveAll(requests);
        return ResponseEntity.ok(ApiResponse.ok("Divergências salvas", saved));
    }


    @GetMapping
    public ResponseEntity<ApiResponse<List<DivergenceRecordResponse>>> findByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        List<DivergenceRecordResponse> records = divergenceService.findByDate(date);
        return ResponseEntity.ok(ApiResponse.ok("OK", records));
    }


    @GetMapping("/report/pdf")
    public ResponseEntity<byte[]> reportPdf(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        try {
            List<DivergenceRecordResponse> records = divergenceService.findByDateForReport(date);
            byte[] pdf = pdfReportService.generate(date, records);

            String filename = "divergencias-" + date + ".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(pdf);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DivergenceRecordResponse>> update(
            @PathVariable Long id,
            @RequestBody @Valid DivergenceRecordRequest request
    ) {
        try {
            DivergenceRecordResponse updated = divergenceService.update(id, request);
            return ResponseEntity.ok(ApiResponse.ok("Divergência atualizada", updated));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        try {
            divergenceService.delete(id);
            return ResponseEntity.ok(ApiResponse.ok("Divergência excluída"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error(404, e.getMessage()));
        }
    }

    @GetMapping("/ranking/separator")
    public ResponseEntity<List<DivergenceRankingItem>> rankBySeparator(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(divergenceRecordRepository.rankBySeparator(from, to));
    }

    @GetMapping("/ranking/product")
    public ResponseEntity<List<DivergenceRankingItem>> rankByProduct(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return ResponseEntity.ok(divergenceRecordRepository.rankByProduct(from, to));
    }

}