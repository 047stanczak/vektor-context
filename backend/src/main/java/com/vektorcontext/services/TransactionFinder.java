package com.vektorcontext.services;

import com.vektorcontext.models.SeparatedProduct;
import com.vektorcontext.models.SeparationOperation;
import com.vektorcontext.models.StockSnapshot;
import com.vektorcontext.repository.SeparatedProductRepository;
import com.vektorcontext.repository.StockSnapshotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class TransactionFinder {

    private final SeparatedProductRepository separatedProductRepository;
    private final StockSnapshotRepository stockSnapshotRepository;

    public TransactionFinder(SeparatedProductRepository separatedProductRepository,
                             StockSnapshotRepository stockSnapshotRepository) {
        this.separatedProductRepository = separatedProductRepository;
        this.stockSnapshotRepository = stockSnapshotRepository;
    }


    @Transactional(readOnly = true)
    public String findSeparatorName(Integer productCode, Integer storeCode) {
        Optional<SeparatedProduct> separated = separatedProductRepository
                .findTopByProductCodeAndStoreCodeOrderByDateDesc(productCode, storeCode);

        if (separated.isEmpty()) {
            return null;
        }

        SeparationOperation operation = separated.get().getSeparationOperation();

        if (operation == null) {
            return null;
        }

        return operation.getUserName();
    }

    @Transactional(readOnly = true)
    public LocalDate findSeparationDate(Integer productCode, Integer storeCode) {
        return separatedProductRepository
                .findTopByProductCodeAndStoreCodeOrderByDateDesc(productCode, storeCode)
                .map(SeparatedProduct::getDate)
                .orElse(null);
    }

    public Double findCurrentStock(Integer productCode) {
        return stockSnapshotRepository
                .findTopByProductCodeOrderByCapturedAtDesc(productCode)
                .map(StockSnapshot::getCurrentStock)
                .orElse(null);
    }
}