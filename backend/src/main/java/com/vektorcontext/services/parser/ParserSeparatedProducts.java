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

            try (CSVReader reader = csvHelper.buildReader(fileBytes)) {

                String[] header = reader.readNext();
                if (header == null) {
                    throw new RuntimeException("Arquivo vazio");
                }

                reader.skip(1);

                Map<String, Integer> headerIndex = csvHelper.buildHeaderIndex(header);

                Integer operationIndex    = csvHelper.headerIndex(headerIndex, "tp", "tipo", "operacao", "operation");
                Integer transactionIndex  = csvHelper.headerIndex(headerIndex, "transacao_bx", "transaction_bx");
                Integer storeCodeIndex    = csvHelper.headerIndex(headerIndex, "orig", "origem", "loja_origem", "store_code");
                Integer quantityIndex     = csvHelper.headerIndex(headerIndex, "qtde_un");
                Integer dateIndex         = csvHelper.headerIndex(headerIndex, "baixa", "data_baixa", "baixa_data");

                Integer codeIndex         = csvHelper.headerIndex(headerIndex, "cod_prod");
                Integer barcodeIndex      = csvHelper.headerIndex(headerIndex, "codigo_barras");
                Integer descriptionIndex  = csvHelper.headerIndex(headerIndex, "descricao");
                Integer complementIndex   = csvHelper.headerIndex(headerIndex, "complemento");
                Integer brandIndex        = csvHelper.headerIndex(headerIndex, "marca");
                Integer groupIndex        = csvHelper.headerIndex(headerIndex, "grupo");
                Integer deptIndex         = csvHelper.headerIndex(headerIndex, "dpto");
                Integer packQtyIndex      = csvHelper.headerIndex(headerIndex, "quant_emb");

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

                    if (csvHelper.isEmptyRow(cols)) continue;

                    String transaction = csvHelper.col(cols, transactionIndex);
                    if (transaction == null || transaction.isBlank()) continue;

                    String rawDate = csvHelper.col(cols, dateIndex);
                    if (rawDate == null || rawDate.isBlank()) continue;

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
                    product.setPackQuantity(csvHelper.parseQuantity(csvHelper.col(cols, packQtyIndex)));

                    product = parserProduct.findOrCreateProduct(product);

                    if (!separationOperationRepository.existsById(transaction)) {
                        SeparationOperation op = new SeparationOperation();
                        op.setTransaction(transaction);
                        op.setOperation("MANUAL");
                        op.setUserName("manual");
                        separationOperationRepository.save(op);
                    }

                    SeparatedProduct sp = new SeparatedProduct();
                    sp.setOperation(csvHelper.col(cols, operationIndex));
                    sp.setTransaction(transaction);
                    sp.setStoreCode(csvHelper.parseQuantity(csvHelper.col(cols, storeCodeIndex)));
                    sp.setDate(csvHelper.parseDate(rawDate));
                    sp.setProductCode(product.getCode());
                    sp.setQuantity(csvHelper.parseDouble(csvHelper.col(cols, quantityIndex)));

                    separatedProducts.add(sp);
                }
            }

            separatedProductRepository.saveAll(separatedProducts);
            importJobService.success(jobId);

        } catch (Exception e) {
            importJobService.error(jobId, e.getMessage());
        }
    }
}