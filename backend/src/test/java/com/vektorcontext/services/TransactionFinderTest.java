package com.vektorcontext.services;

import com.vektorcontext.models.SeparatedProduct;
import com.vektorcontext.models.SeparationOperation;
import com.vektorcontext.models.StockSnapshot;
import com.vektorcontext.repository.SeparatedProductRepository;
import com.vektorcontext.repository.StockSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionFinderTest {

    @Mock
    private SeparatedProductRepository separatedProductRepository;

    @Mock
    private StockSnapshotRepository stockSnapshotRepository;

    @InjectMocks
    private TransactionFinder transactionFinder;

    @Test
    void findSeparatorName_returnsUserName() {
        SeparationOperation operation = new SeparationOperation();
        operation.setUserName("Joao");
        SeparatedProduct separated = new SeparatedProduct();
        separated.setSeparationOperation(operation);
        when(separatedProductRepository.findTopByProductCodeAndStoreCodeOrderByDateDesc(10, 1))
                .thenReturn(Optional.of(separated));

        String result = transactionFinder.findSeparatorName(10, 1);

        assertEquals("Joao", result);
    }

    @Test
    void findSeparatorName_noSeparatedProduct_returnsNull() {
        when(separatedProductRepository.findTopByProductCodeAndStoreCodeOrderByDateDesc(10, 1))
                .thenReturn(Optional.empty());

        assertNull(transactionFinder.findSeparatorName(10, 1));
    }

    @Test
    void findSeparatorName_noOperation_returnsNull() {
        SeparatedProduct separated = new SeparatedProduct();
        separated.setSeparationOperation(null);
        when(separatedProductRepository.findTopByProductCodeAndStoreCodeOrderByDateDesc(10, 1))
                .thenReturn(Optional.of(separated));

        assertNull(transactionFinder.findSeparatorName(10, 1));
    }

    @Test
    void findSeparationDate_returnsDate() {
        LocalDate date = LocalDate.of(2026, 1, 10);
        SeparatedProduct separated = new SeparatedProduct();
        separated.setDate(date);
        when(separatedProductRepository.findTopByProductCodeAndStoreCodeOrderByDateDesc(10, 1))
                .thenReturn(Optional.of(separated));

        assertEquals(date, transactionFinder.findSeparationDate(10, 1));
    }

    @Test
    void findSeparationDate_notFound_returnsNull() {
        when(separatedProductRepository.findTopByProductCodeAndStoreCodeOrderByDateDesc(10, 1))
                .thenReturn(Optional.empty());

        assertNull(transactionFinder.findSeparationDate(10, 1));
    }

    @Test
    void findCurrentStock_returnsStock() {
        StockSnapshot snapshot = new StockSnapshot();
        snapshot.setCurrentStock(15.0);
        when(stockSnapshotRepository.findTopByProductCodeOrderByCapturedAtDesc(10))
                .thenReturn(Optional.of(snapshot));

        assertEquals(15.0, transactionFinder.findCurrentStock(10));
    }

    @Test
    void findCurrentStock_notFound_returnsNull() {
        when(stockSnapshotRepository.findTopByProductCodeOrderByCapturedAtDesc(10))
                .thenReturn(Optional.empty());

        assertNull(transactionFinder.findCurrentStock(10));
    }
}
