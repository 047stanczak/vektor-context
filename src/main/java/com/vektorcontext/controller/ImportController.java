package com.vektorcontext.controller;

import com.vektorcontext.models.ImportJob;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.parser.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/import")
public class ImportController {

    private final ParserProduct parserProduct;
    private final ParserSeparatedProducts parserSeparationProducts;
    private final ParserSeparationOperation parserSeparationOperations;
    private final ImportJobService importJobService;

    public ImportController(
            ParserProduct parserProduct,
            ParserSeparatedProducts parserSeparationProducts,
            ParserSeparationOperation parserSeparationOperations,
            ImportJobService importJobService
    ) {
        this.parserProduct = parserProduct;
        this.parserSeparationProducts = parserSeparationProducts;
        this.parserSeparationOperations = parserSeparationOperations;
        this.importJobService = importJobService;
    }

    @PostMapping("/products")
    public ResponseEntity<ImportJob> importProducts(@RequestParam("file") MultipartFile file) throws Exception {
        ImportJob job = importJobService.create(file.getOriginalFilename(), "PRODUCTS");
        parserProduct.parseProducts(file.getBytes(), job.getId());
        return ResponseEntity.accepted().body(job);
    }

    @PostMapping("/separation-operations")
    public ResponseEntity<ImportJob> importSeparationOperations(@RequestParam("file") MultipartFile file) throws Exception {
        ImportJob job = importJobService.create(file.getOriginalFilename(), "SEPARATION_OPERATIONS");
        parserSeparationOperations.parseSeparationOperations(file.getBytes(), job.getId());
        return ResponseEntity.accepted().body(job);
    }

    @PostMapping("/separated-products")
    public ResponseEntity<ImportJob> importSeparatedProducts(@RequestParam("file") MultipartFile file) throws Exception {
        ImportJob job = importJobService.create(file.getOriginalFilename(), "SEPARATED_PRODUCTS");
        parserSeparationProducts.parseSeparatedProducts(file.getBytes(), job.getId());
        return ResponseEntity.accepted().body(job);
    }
}