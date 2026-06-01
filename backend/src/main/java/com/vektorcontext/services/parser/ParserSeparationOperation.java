package com.vektorcontext.services.parser;

import com.opencsv.CSVReader;
import com.vektorcontext.models.SeparationOperation;
import com.vektorcontext.repository.SeparationOperationRepository;
import com.vektorcontext.services.ImportJobService;
import com.vektorcontext.services.csv.CsvHelper;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ParserSeparationOperation {

    private final SeparationOperationRepository separationOperationRepository;
    private final CsvHelper csvHelper;
    private final ImportJobService importJobService;


    public ParserSeparationOperation(
            SeparationOperationRepository separationOperationRepository,
            CsvHelper csvHelper,
            ImportJobService importJobService
    ) {
        this.separationOperationRepository = separationOperationRepository;
        this.csvHelper = csvHelper;
        this.importJobService = importJobService;
    }

    @Async
    public void parseSeparationOperations(byte[] fileBytes, Long jobId) {

        try {

            List<SeparationOperation> operations = new ArrayList<>();

            try (CSVReader reader = csvHelper.buildReader(fileBytes)) {

                String[] header = reader.readNext();

                if (header == null) {
                    throw new RuntimeException("Arquivo vazio");
                }

                Map<String, Integer> headerIndex =
                        csvHelper.buildHeaderIndex(header);

                Integer operationIndex = csvHelper.headerIndex(
                        headerIndex,
                        "operacao", "operação", "operation"
                );

                Integer transactionIndex = csvHelper.headerIndex(
                        headerIndex,
                        "transacao", "transação", "transaction"
                );

                Integer dateIndex = csvHelper.headerIndex(
                        headerIndex,
                        "lcto", "data", "date"
                );

                Integer userIndex = csvHelper.headerIndex(
                        headerIndex,
                        "usuario_lcto", "usuario", "user"
                );

                String[] cols;

                while ((cols = reader.readNext()) != null) {

                    if (csvHelper.isEmptyRow(cols)) {
                        continue;
                    }

                    String operation =
                            csvHelper.col(cols, idx(operationIndex, 0));

                    String transaction =
                            csvHelper.col(cols, idx(transactionIndex, 1));

                    if (operation == null || transaction == null) {
                        continue;
                    }

                    SeparationOperation op =
                            new SeparationOperation();

                    op.setOperation(operation);
                    op.setTransaction(transaction);

                    String rawDate =
                            csvHelper.col(cols, idx(dateIndex, 10));

                    if (rawDate != null && !rawDate.isBlank()) {

                        op.setTime(
                                rawDate
                        );
                    }

                    op.setUserName(
                            csvHelper.col(cols, idx(userIndex, 15))
                    );

                    operations.add(op);
                }
            }

            separationOperationRepository.saveAll(operations);

            importJobService.success(jobId);

        } catch (Exception e) {

            importJobService.error(
                    jobId,
                    e.getMessage()
            );
        }
    }

    private int idx(Integer index, int fallback) {
        return index == null ? fallback : index;
    }
}