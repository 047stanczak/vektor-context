package com.vektorcontext.services.parser;

import com.opencsv.CSVReader;
import com.vektorcontext.models.SeparatedProduct;
import com.vektorcontext.repository.SeparatedProductRepository;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.csv.CsvHelper;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ParserSeparatedProducts {

    private final SeparatedProductRepository separatedProductRepository;
    private final CsvHelper csvHelper;
    private final ImportJobService importJobService;

    public ParserSeparatedProducts(
            SeparatedProductRepository separatedProductRepository,
            CsvHelper csvHelper,
            ImportJobService importJobService
    ) {
        this.separatedProductRepository = separatedProductRepository;
        this.csvHelper = csvHelper;
        this.importJobService = importJobService;
    }

    @Async
    public void parseSeparatedProducts(byte[] fileBytes, Long jobId) {

        try {

            List<SeparatedProduct> separatedProducts = new ArrayList<>();

            try (CSVReader reader = csvHelper.buildReader(fileBytes)) {

                String[] header = reader.readNext();
                if (header == null) {
                    throw new RuntimeException("Arquivo vazio");
                }

                Map<String, Integer> headerIndex = csvHelper.buildHeaderIndex(header);
                reader.skip(1);

                Integer operationIndex = csvHelper.headerIndex(headerIndex, "operacao", "operação", "operation");
                Integer transactionIndex = csvHelper.headerIndex(headerIndex, "transacao", "transação", "transaction");
                Integer productCodeIndex = csvHelper.headerIndex(headerIndex, "codigo", "code", "product_code");
                Integer quantityIndex = csvHelper.headerIndex(headerIndex, "qtde", "quantity", "quantidade", "qty");

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols) || csvHelper.col(cols, transactionIndex) == null) {
                        continue;
                    }

                    SeparatedProduct sp = new SeparatedProduct();
                    sp.setOperation(csvHelper.col(cols, idx(operationIndex, 0)));
                    sp.setTransaction(csvHelper.col(cols, idx(transactionIndex, 1)));
                    sp.setProductCode(csvHelper.parseQuantity(csvHelper.col(cols, idx(productCodeIndex, 2))));
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