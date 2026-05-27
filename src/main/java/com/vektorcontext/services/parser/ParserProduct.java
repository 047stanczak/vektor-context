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
                reader.skip(1);

                Integer codeIndex = csvHelper.headerIndex(headerIndex, "codigo", "cod", "code");
                Integer descriptionIndex = csvHelper.headerIndex(headerIndex, "descricao", "description", "descrição");
                Integer complementIndex = csvHelper.headerIndex(headerIndex, "complemento");
                Integer brandIndex = csvHelper.headerIndex(headerIndex, "marca");
                Integer productGroupIndex = csvHelper.headerIndex(headerIndex, "grupo", "product_group");
                Integer departmentIndex = csvHelper.headerIndex(headerIndex, "dpto", "departamento", "department", "dept");
                Integer barcodeIndex = csvHelper.headerIndex(headerIndex, "codigo_barras", "codigobarras", "barcode");
                Integer supplierNameIndex = csvHelper.headerIndex(headerIndex, "nome_ultimo_forn", "nome_ult_forn", "nome_fornecedor", "fornecedor", "nome");
                Integer packQuantityIndex = csvHelper.headerIndex(headerIndex, "qtde_emb", "qtde_embalagem", "qtde/emb", "qtde");
                Integer currentStockIndex = csvHelper.headerIndex(headerIndex, "estoque", "current_stock", "stock");

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols) || csvHelper.col(cols, codeIndex) == null) {
                        continue;
                    }

                    Product product = new Product();
                    product.setCode(
                            Integer.parseInt(csvHelper.col(cols, idx(codeIndex, 0)))
                    );
                    product.setDescription(
                            csvHelper.col(cols, idx(descriptionIndex, 1))
                    );
                    product.setComplement(
                            csvHelper.col(cols, idx(complementIndex, 2))
                    );
                    product.setBrand(
                            csvHelper.col(cols, idx(brandIndex, 3))
                    );
                    product.setProductGroup(
                            csvHelper.col(cols, idx(productGroupIndex, 4))
                    );
                    product.setDepartment(
                            csvHelper.col(cols, idx(departmentIndex, 5))
                    );
                    product.setBarcode(
                            csvHelper.col(cols, idx(barcodeIndex, 6))
                    );
                    product.setSupplierName(
                            csvHelper.col(cols, idx(supplierNameIndex, 10))
                    );
                    product.setPackQuantity(
                            csvHelper.parseQuantity(csvHelper.col(cols, idx(packQuantityIndex, 11)))
                    );

                    products.add(product);

                    StockSnapshot snapshot = new StockSnapshot();
                    snapshot.setProductCode(product.getCode());
                    snapshot.setCurrentStock(
                            csvHelper.parseDouble(csvHelper.col(cols, idx(currentStockIndex, 12)))
                    );
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

    private int idx(Integer index, int fallback) {
        return index == null ? fallback : index;
    }
}