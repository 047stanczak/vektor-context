package com.vektorcontext.services;

import com.vektorcontext.dto.DivergenceQueryResponse;
import com.vektorcontext.dto.DivergenceRecordRequest;
import com.vektorcontext.dto.DivergenceRecordResponse;
import com.vektorcontext.exception.DivergenceNotFoundException;
import com.vektorcontext.exception.ProductNotFoundException;
import com.vektorcontext.models.DivergenceRecord;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.StockSnapshot;
import com.vektorcontext.repository.DivergenceRecordRepository;
import com.vektorcontext.repository.ProductRepository;
import com.vektorcontext.repository.StockSnapshotRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DivergenceServiceTest {

    @Mock
    private DivergenceRecordRepository divergenceRecordRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockSnapshotRepository stockSnapshotRepository;

    @Mock
    private TransactionFinder transactionFinder;

    @InjectMocks
    private DivergenceService divergenceService;

    private Product product(Integer code) {
        Product p = new Product();
        p.setCode(code);
        p.setDescription("Produto " + code);
        p.setComplement("Compl");
        p.setBarcode("789" + code);
        return p;
    }

    @Test
    void queryByProductCode_returnsResponse() {
        when(productRepository.findById(10)).thenReturn(Optional.of(product(10)));
        when(transactionFinder.findSeparatorName(10, 1)).thenReturn("Maria");
        when(transactionFinder.findCurrentStock(10)).thenReturn(5.0);
        when(transactionFinder.findSeparationDate(10, 1)).thenReturn(LocalDate.of(2026, 1, 1));

        DivergenceQueryResponse result = divergenceService.queryByProductCode(10, 1);

        assertEquals(10, result.getProductCode());
        assertEquals("Maria", result.getSeparatorName());
        assertEquals(5.0, result.getCurrentStock());
    }

    @Test
    void queryByProductCode_notFound_throwsException() {
        when(productRepository.findById(10)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> divergenceService.queryByProductCode(10, 1));
    }

    @Test
    void queryByBarcode_returnsResponse() {
        when(productRepository.findByBarcode("78910")).thenReturn(Optional.of(product(10)));
        when(transactionFinder.findSeparatorName(any(), any())).thenReturn(null);
        when(transactionFinder.findCurrentStock(any())).thenReturn(null);
        when(transactionFinder.findSeparationDate(any(), any())).thenReturn(null);

        DivergenceQueryResponse result = divergenceService.queryByBarcode("78910", 1);

        assertEquals(10, result.getProductCode());
    }

    @Test
    void queryByBarcode_notFound_throwsException() {
        when(productRepository.findByBarcode("000")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> divergenceService.queryByBarcode("000", 1));
    }

    @Test
    void saveAll_savesAndEnrichesRecords() {
        DivergenceRecordRequest req = new DivergenceRecordRequest();
        req.setDate(LocalDate.of(2026, 1, 1));
        req.setStoreCode(1);
        req.setProductCode(10);
        req.setTipo("FALTA");
        req.setQuantity(2.0);
        req.setSeparatorName("Maria");

        DivergenceRecord saved = new DivergenceRecord();
        saved.setId(1L);
        saved.setProductCode(10);

        when(divergenceRecordRepository.saveAll(any())).thenReturn(List.of(saved));
        when(productRepository.findAllById(any())).thenReturn(List.of(product(10)));

        List<DivergenceRecordResponse> result = divergenceService.saveAll(List.of(req));

        assertEquals(1, result.size());
        assertEquals("Produto 10", result.get(0).getProductDescription());
    }

    @Test
    void findByDate_returnsEnrichedRecords() {
        DivergenceRecord record = new DivergenceRecord();
        record.setProductCode(10);
        when(divergenceRecordRepository.findByDate(any())).thenReturn(List.of(record));
        when(productRepository.findAllById(any())).thenReturn(List.of(product(10)));

        List<DivergenceRecordResponse> result = divergenceService.findByDate(LocalDate.of(2026, 1, 1));

        assertEquals(1, result.size());
    }

    @Test
    void findById_returnsRecord() {
        DivergenceRecord record = new DivergenceRecord();
        when(divergenceRecordRepository.findById(1L)).thenReturn(Optional.of(record));

        assertEquals(record, divergenceService.findById(1L));
    }

    @Test
    void findById_notFound_throwsException() {
        when(divergenceRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DivergenceNotFoundException.class, () -> divergenceService.findById(1L));
    }

    @Test
    void update_updatesAndReturnsRecord() {
        DivergenceRecord record = new DivergenceRecord();
        record.setProductCode(10);
        when(divergenceRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(divergenceRecordRepository.save(record)).thenReturn(record);
        when(productRepository.findAllById(any())).thenReturn(List.of(product(10)));

        DivergenceRecordRequest req = new DivergenceRecordRequest();
        req.setDate(LocalDate.of(2026, 2, 1));
        req.setStoreCode(2);
        req.setProductCode(10);
        req.setTipo("SOBRA");
        req.setQuantity(3.0);
        req.setSeparatorName("Joao");

        DivergenceRecordResponse result = divergenceService.update(1L, req);

        assertEquals("SOBRA", result.getTipo());
        assertEquals(2, result.getStoreCode());
    }

    @Test
    void update_notFound_throwsException() {
        when(divergenceRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DivergenceNotFoundException.class,
                () -> divergenceService.update(1L, new DivergenceRecordRequest()));
    }

    @Test
    void delete_callsRepositoryDelete() {
        when(divergenceRecordRepository.findById(1L)).thenReturn(Optional.of(new DivergenceRecord()));

        divergenceService.delete(1L);

        verify(divergenceRecordRepository).deleteById(1L);
    }

    @Test
    void delete_notFound_throwsException() {
        when(divergenceRecordRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(DivergenceNotFoundException.class, () -> divergenceService.delete(1L));
        verify(divergenceRecordRepository, never()).deleteById(any());
    }

    @Test
    void findByDateForReport_returnsEnrichedWithStockSnapshot() {
        LocalDate date = LocalDate.of(2026, 1, 1);
        DivergenceRecord record = new DivergenceRecord();
        record.setProductCode(10);
        when(divergenceRecordRepository.findByDate(date)).thenReturn(List.of(record));
        when(productRepository.findAllById(any())).thenReturn(List.of(product(10)));

        StockSnapshot snapshot = new StockSnapshot();
        snapshot.setCurrentStock(8.0);
        when(stockSnapshotRepository.findByProductCodeAndDate(10, date)).thenReturn(Optional.of(snapshot));

        List<DivergenceRecordResponse> result = divergenceService.findByDateForReport(date);

        assertEquals(8.0, result.get(0).getCurrentStock());
    }

    @Test
    void findByDateForReport_empty_throwsException() {
        when(divergenceRecordRepository.findByDate(any())).thenReturn(List.of());

        assertThrows(DivergenceNotFoundException.class,
                () -> divergenceService.findByDateForReport(LocalDate.of(2026, 1, 1)));
    }
}
