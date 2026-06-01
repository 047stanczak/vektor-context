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
import java.util.Optional;

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

                ProductColumnIndexes columns = resolveProductColumns(headerIndex);
                Integer currentStockIndex = csvHelper.headerIndex(headerIndex, "estoque", "current_stock", "stock");

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols) || csvHelper.col(cols, idx(columns.codeIndex, 0)) == null) {
                        continue;
                    }

                    Product product = parseProductFromRow(cols, columns, 0);
                    if (product == null) {
                        continue;
                    }

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

    public Product findOrCreateProduct(
            String[] cols,
            Map<String, Integer> headerIndex,
            int codeColumnFallback,
            Map<Integer, Product> cache
    ) {
        Product parsed = parseProductFromRow(cols, resolveProductColumns(headerIndex), codeColumnFallback);
        if (parsed == null || parsed.getCode() == null) {
            throw new RuntimeException("Código de produto inválido");
        }

        Integer code = parsed.getCode();
        if (cache.containsKey(code)) {
            return cache.get(code);
        }

        Optional<Product> existing = productRepository.findById(code);
        if (existing.isPresent()) {
            cache.put(code, existing.get());
            return existing.get();
        }

        productRepository.save(parsed);
        cache.put(code, parsed);
        return parsed;
    }

    public Product parseProductFromRow(String[] cols, Map<String, Integer> headerIndex, int codeColumnFallback) {
        return parseProductFromRow(cols, resolveProductColumns(headerIndex), codeColumnFallback);
    }

    private Product parseProductFromRow(String[] cols, ProductColumnIndexes columns, int codeColumnFallback) {
        String codeValue = csvHelper.col(cols, idx(columns.codeIndex, codeColumnFallback));
        if (codeValue == null) {
            return null;
        }

        Product product = new Product();
        product.setCode(Integer.parseInt(codeValue));
        product.setDescription(csvHelper.col(cols, idx(columns.descriptionIndex, 1)));
        product.setComplement(csvHelper.col(cols, idx(columns.complementIndex, 2)));
        product.setBrand(csvHelper.col(cols, idx(columns.brandIndex, 3)));
        product.setProductGroup(csvHelper.col(cols, idx(columns.productGroupIndex, 4)));
        product.setDepartment(csvHelper.col(cols, idx(columns.departmentIndex, 5)));
        product.setBarcode(csvHelper.col(cols, idx(columns.barcodeIndex, 6)));
        product.setSupplierName(csvHelper.col(cols, idx(columns.supplierNameIndex, 10)));
        product.setPackQuantity(
            csvHelper.parseQuantity(csvHelper.col(cols, idx(columns.packQuantityIndex, -1)))
        );
        return product;
    }

    private ProductColumnIndexes resolveProductColumns(Map<String, Integer> headerIndex) {
        return new ProductColumnIndexes(
                csvHelper.headerIndex(headerIndex, "cod_prod", "codigo_produto", "codigo", "code"),
                csvHelper.headerIndex(headerIndex, "descricao", "description", "descrição", "produto", "descricao_produto"),
                csvHelper.headerIndex(headerIndex, "complemento"),
                csvHelper.headerIndex(headerIndex, "marca"),
                csvHelper.headerIndex(headerIndex, "grupo", "product_group"),
                csvHelper.headerIndex(headerIndex, "dpto", "departamento", "department", "dept"),
                csvHelper.headerIndex(headerIndex, "codigo_barras", "codigobarras", "barcode", "ean"),
                csvHelper.headerIndex(headerIndex, "nome_ultimo_forn", "nome_ult_forn", "nome_fornecedor", "fornecedor", "nome"),
                csvHelper.headerIndex(headerIndex, "qtde_emb", "qtde_embalagem", "qtde_emb", "qtde")
        );
    }

    private int idx(Integer index, int fallback) {
        return index == null ? fallback : index;
    }

    private static final class ProductColumnIndexes {
        private final Integer codeIndex;
        private final Integer descriptionIndex;
        private final Integer complementIndex;
        private final Integer brandIndex;
        private final Integer productGroupIndex;
        private final Integer departmentIndex;
        private final Integer barcodeIndex;
        private final Integer supplierNameIndex;
        private final Integer packQuantityIndex;

        private ProductColumnIndexes(
                Integer codeIndex,
                Integer descriptionIndex,
                Integer complementIndex,
                Integer brandIndex,
                Integer productGroupIndex,
                Integer departmentIndex,
                Integer barcodeIndex,
                Integer supplierNameIndex,
                Integer packQuantityIndex
        ) {
            this.codeIndex = codeIndex;
            this.descriptionIndex = descriptionIndex;
            this.complementIndex = complementIndex;
            this.brandIndex = brandIndex;
            this.productGroupIndex = productGroupIndex;
            this.departmentIndex = departmentIndex;
            this.barcodeIndex = barcodeIndex;
            this.supplierNameIndex = supplierNameIndex;
            this.packQuantityIndex = packQuantityIndex;
        }
    }
}