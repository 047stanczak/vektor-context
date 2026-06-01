package com.vektorcontext.services.parser;

import com.opencsv.CSVReader;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.SeparatedProduct;
import com.vektorcontext.models.SeparationOperation;
import com.vektorcontext.repository.SeparatedProductRepository;
import com.vektorcontext.repository.SeparationOperationRepository;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.csv.CsvHelper;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParserSeparatedProducts {

    private final SeparatedProductRepository separatedProductRepository;
    private final ParserProduct parserProduct;
    private final CsvHelper csvHelper;
    private final ImportJobService importJobService;
    private final SeparationOperationRepository separationOperationRepository;

    public ParserSeparatedProducts(
            SeparatedProductRepository separatedProductRepository,
            ParserProduct parserProduct,
            CsvHelper csvHelper,
            ImportJobService importJobService,
            SeparationOperationRepository separationOperationRepository
    ) {
        this.separatedProductRepository = separatedProductRepository;
        this.parserProduct = parserProduct;
        this.csvHelper = csvHelper;
        this.importJobService = importJobService;
        this.separationOperationRepository = separationOperationRepository;
    }

    @Async
    public void parseSeparatedProducts(byte[] fileBytes, Long jobId) {

        try {

            List<SeparatedProduct> separatedProducts = new ArrayList<>();
            Map<Integer, Product> productCache = new HashMap<>();

            try (CSVReader reader = csvHelper.buildReader(fileBytes)) {

                String[] header = reader.readNext();
                if (header == null) {
                    throw new RuntimeException("Arquivo vazio");
                }

                reader.skip(1);

                Map<String, Integer> headerIndex = csvHelper.buildHeaderIndex(header);

                Integer operationIndex = csvHelper.headerIndex(headerIndex, "tp", "tipo", "operacao", "operação", "operation");
                Integer transactionIndex = csvHelper.headerIndex(headerIndex, "transacao_bx", "transação_bx", "transaction_bx");
                Integer storeCodeIndex = csvHelper.headerIndex(headerIndex, "orig", "origem", "loja_origem", "store_code");
                Integer quantityIndex = csvHelper.headerIndex(headerIndex,"qtde_un");
                Integer dateIndex = csvHelper.headerIndex(headerIndex, "baixa", "data_baixa", "data baixa", "baixa_data");

                Integer productCodeIndex = csvHelper.headerIndex(headerIndex,"cod_prod");


                if (transactionIndex == null) {
                    throw new RuntimeException("Coluna 'Transação Bx' não encontrada no arquivo.");
                }
                if (storeCodeIndex == null) {
                    throw new RuntimeException("Coluna 'Orig' (loja origem) não encontrada no arquivo.");
                }
                if (dateIndex == null) {
                    throw new RuntimeException("Coluna 'Baixa' não encontrada no arquivo.");
                }

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols)) {
                        continue;
                    }

                    String transaction = csvHelper.col(cols, transactionIndex);
                    if (transaction == null || transaction.isBlank()) {
                        continue;
                    }

                    Integer storeCode = csvHelper.parseQuantity(
                        csvHelper.col(cols, storeCodeIndex)
                    );

                    SeparatedProduct sp = new SeparatedProduct();

                    if (!separationOperationRepository.existsById(transaction)) {

                        SeparationOperation op = new SeparationOperation();

                        op.setTransaction(transaction);
                        op.setOperation("MANUAL");
                        op.setUserName("manual");

                        separationOperationRepository.save(op);
                    }

                    
                    sp.setOperation(csvHelper.col(cols, idx(operationIndex, 3)));

                    sp.setTransaction(transaction);
                    sp.setStoreCode(storeCode);

                    String rawDate = csvHelper.col(cols, dateIndex);
                    if (rawDate == null || rawDate.isBlank()) {
                        continue;
                    }
                    sp.setDate(csvHelper.parseDate(rawDate));

                    Product product = parserProduct.findOrCreateProduct(cols, headerIndex, idx(productCodeIndex, 2), productCache);
                    if (product == null) {
                        continue;
                    }

                    sp.setProductCode(product.getCode());
                    sp.setProduct(product);


                    sp.setQuantity(csvHelper.parseDouble(csvHelper.col(cols, idx(quantityIndex, 25))));

                    separatedProducts.add(sp);
                }
            }

            separatedProductRepository.saveAll(separatedProducts);
            importJobService.success(jobId);

        } catch (Exception e) {
            importJobService.error(jobId, e.getMessage());
        }
    }

    private int idx(Integer index, int fallback) {
        return index == null ? fallback : index;
    }
}