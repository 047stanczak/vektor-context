package com.vektorcontext.services.parser;

import com.opencsv.CSVReader;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.StockSnapshot;
import com.vektorcontext.repository.ProductRepository;
import com.vektorcontext.repository.StockSnapshotRepository;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.csv.CsvHelper;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ParserProduct {

    private final ProductRepository productRepository;
    private final StockSnapshotRepository stockSnapshotRepository;
    private final CsvHelper csvHelper;
    private final ImportJobService importJobService;

    public ParserProduct(
            ProductRepository productRepository,
            StockSnapshotRepository stockSnapshotRepository,
            CsvHelper csvHelper,
            ImportJobService importJobService
    ) {
        this.productRepository = productRepository;
        this.stockSnapshotRepository = stockSnapshotRepository;
        this.csvHelper = csvHelper;
        this.importJobService = importJobService;
    }

    @Async
    public void parseProducts(byte[] fileBytes, Long jobId) {

        try {

            List<Product> products = new ArrayList<>();
            List<StockSnapshot> snapshots = new ArrayList<>();

            LocalDateTime capturedAt = LocalDateTime.now();

            try (CSVReader reader = csvHelper.buildReader(fileBytes)) {

                String[] header = reader.readNext();
                if (header == null) {
                    throw new RuntimeException("Arquivo vazio");
                }

                Map<String, Integer> headerIndex = csvHelper.buildHeaderIndex(header);

                Integer codeIndex        = csvHelper.headerIndex(headerIndex, "codigo");
                Integer barcodeIndex     = csvHelper.headerIndex(headerIndex, "codigo_barras");
                Integer descriptionIndex = csvHelper.headerIndex(headerIndex, "descricao");
                Integer complementIndex  = csvHelper.headerIndex(headerIndex, "complemento");
                Integer brandIndex       = csvHelper.headerIndex(headerIndex, "marca");
                Integer groupIndex       = csvHelper.headerIndex(headerIndex, "grupo");
                Integer deptIndex        = csvHelper.headerIndex(headerIndex, "dpto");
                Integer supplierIndex    = csvHelper.headerIndex(headerIndex, "nome_ult_forn");
                Integer packQtyIndex     = csvHelper.headerIndex(headerIndex, "qtde_emb");
                Integer stockIndex       = csvHelper.headerIndex(headerIndex, "estoque");

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols)) continue;

                    String codeValue = csvHelper.col(cols, codeIndex);
                    if (codeValue == null) continue;

                    Product product = new Product();
                    product.setCode(Integer.parseInt(codeValue));
                    product.setBarcode(csvHelper.col(cols, barcodeIndex));
                    product.setDescription(csvHelper.col(cols, descriptionIndex));
                    product.setComplement(csvHelper.col(cols, complementIndex));
                    product.setBrand(csvHelper.col(cols, brandIndex));
                    product.setProductGroup(csvHelper.col(cols, groupIndex));
                    product.setDepartment(csvHelper.col(cols, deptIndex));
                    product.setSupplierName(csvHelper.col(cols, supplierIndex));
                    product.setPackQuantity(csvHelper.parseQuantity(csvHelper.col(cols, packQtyIndex)));

                    products.add(product);

                    StockSnapshot snapshot = new StockSnapshot();
                    snapshot.setProductCode(product.getCode());
                    snapshot.setCurrentStock(csvHelper.parseDouble(csvHelper.col(cols, stockIndex)));
                    snapshot.setCapturedAt(capturedAt);

                    snapshots.add(snapshot);
                }
            }

            productRepository.saveAll(products);
            stockSnapshotRepository.saveAll(snapshots);
            importJobService.success(jobId);

        } catch (Exception e) {
            importJobService.error(jobId, e.getMessage());
        }
    }

    public Product findOrCreateProduct(Product parsed) {
        if (parsed == null || parsed.getCode() == null) {
            throw new RuntimeException("Código de produto inválido");
        }

        return productRepository.findById(parsed.getCode())
                .orElseGet(() -> productRepository.save(parsed));
    }
}