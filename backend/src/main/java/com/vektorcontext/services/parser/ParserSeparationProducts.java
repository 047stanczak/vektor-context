package com.vektorcontext.services.parser;

import com.opencsv.CSVReader;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.SeparationProduct;
import com.vektorcontext.repository.SeparationProductRepository;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.csv.CsvHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ParserSeparationProducts {

    private final ImportJobService importJobService;
    private final SeparationProductRepository separationItemRepository;
    private final ParserProduct parserProduct;
    private final CsvHelper csvHelper;

    public ParserSeparationProducts(
        SeparationProductRepository separationItemRepository,
        ParserProduct parserProduct,
        CsvHelper csvHelper,
        ImportJobService importJobService
    ) {
        this.separationItemRepository = separationItemRepository;
        this.parserProduct = parserProduct;
        this.csvHelper = csvHelper;
        this.importJobService = importJobService;
    }

    @Async
    public void parseSeparationProducts(byte[] fileBytes, Long jobId) {

        try {

            List<SeparationProduct> items = new ArrayList<>();

            try (CSVReader reader = csvHelper.buildReader(fileBytes)) {

                String[] header = reader.readNext();
                if (header == null) {
                    throw new RuntimeException("Arquivo vazio");
                }

                Map<String, Integer> headerIndex = csvHelper.buildHeaderIndex(header);

                Integer transactionIndex        = csvHelper.headerIndex(headerIndex, "transacao");
                Integer releaseDateIndex        = csvHelper.headerIndex(headerIndex, "dt_mvto", "data_movimento");
                Integer storeCodeIndex          = csvHelper.headerIndex(headerIndex, "orig", "origem");
                Integer stockPendingCreatorIndex = csvHelper.headerIndex(headerIndex, "nome");
                Integer quantityIndex           = csvHelper.headerIndex(headerIndex, "qtde_un");

                Integer codeIndex               = csvHelper.headerIndex(headerIndex, "cod_prod");
                Integer descriptionIndex        = csvHelper.headerIndex(headerIndex, "descricao");
                Integer complementIndex         = csvHelper.headerIndex(headerIndex, "complemento");
                Integer brandIndex              = csvHelper.headerIndex(headerIndex, "marca");
                Integer groupIndex              = csvHelper.headerIndex(headerIndex, "grupo");
                Integer deptIndex               = csvHelper.headerIndex(headerIndex, "dpto");
                Integer barcodeIndex            = csvHelper.headerIndex(headerIndex, "codigo_barras");
                Integer packQtyIndex            = csvHelper.headerIndex(headerIndex, "quant_emb");

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols)) continue;

                    String transaction = csvHelper.col(cols, transactionIndex);
                    if (transaction == null || transaction.isBlank()) continue;

                    String codeValue = csvHelper.col(cols, codeIndex);
                    if (codeValue == null) continue;

                    Product product = new Product();
                    product.setCode(Integer.parseInt(codeValue));
                    product.setDescription(csvHelper.col(cols, descriptionIndex));
                    product.setComplement(csvHelper.col(cols, complementIndex));
                    product.setBrand(csvHelper.col(cols, brandIndex));
                    product.setProductGroup(csvHelper.col(cols, groupIndex));
                    product.setDepartment(csvHelper.col(cols, deptIndex));
                    product.setBarcode(csvHelper.col(cols, barcodeIndex));
                    product.setPackQuantity(csvHelper.parseQuantity(csvHelper.col(cols, packQtyIndex)));

                    product = parserProduct.findOrCreateProduct(product);

                    SeparationProduct item = new SeparationProduct();
                    item.setTransaction(transaction);
                    item.setReleaseDate(csvHelper.parseDate(csvHelper.col(cols, releaseDateIndex)));
                    item.setStoreCode(csvHelper.col(cols, storeCodeIndex));
                    item.setProductCode(product.getCode());
                    item.setQuantity(csvHelper.parseDouble(csvHelper.col(cols, quantityIndex)));
                    item.setStockPendingCreator(csvHelper.col(cols, stockPendingCreatorIndex));
                    item.setSnapshotDate(LocalDate.now());

                    items.add(item);
                }
            }

            separationItemRepository.saveAll(items);
            importJobService.success(jobId);

        } catch (Exception e) {
            importJobService.error(jobId, e.getMessage());
        }
    }
}