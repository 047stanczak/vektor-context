package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

import com.vektorcontext.dto.CountingItemDTO;
import com.vektorcontext.dto.CountingReportRequest;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.StockSnapshot;
import com.vektorcontext.repository.StockSnapshotRepository;
import com.vektorcontext.services.PdfCountingService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class CountingControllerTest {

    @Mock
    private StockSnapshotRepository repository;

    @Mock
    private PdfCountingService pdfCountingService;

    @InjectMocks
    private CountingController controller;

    private StockSnapshot createSnapshot(Integer productCode, String barcode, String description,
                                         String complement, String brand, Double stock,
                                         LocalDateTime capturedAt) {
        StockSnapshot s = new StockSnapshot();
        s.setProductCode(productCode);
        s.setCurrentStock(stock);
        s.setCapturedAt(capturedAt);
        Product p = new Product();
        p.setCode(productCode);
        p.setBarcode(barcode);
        p.setDescription(description);
        p.setComplement(complement);
        p.setBrand(brand);
        s.setProduct(p);
        return s;
    }

    @Test
    void brands_shouldReturnListOfBrands() {
        when(repository.findDistinctBrandsToday(LocalDate.now()))
                .thenReturn(List.of("Marca A", "Marca B"));

        ResponseEntity<List<String>> response = controller.brands();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsExactly("Marca A", "Marca B");
    }

    @Test
    void brands_shouldReturnEmptyListWhenNoBrands() {
        when(repository.findDistinctBrandsToday(LocalDate.now()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<String>> response = controller.brands();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void brands_shouldPropagateRepositoryException() {
        when(repository.findDistinctBrandsToday(any()))
                .thenThrow(new RuntimeException("erro banco"));

        assertThatThrownBy(() -> controller.brands())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("erro banco");
    }

    @Test
    void byBrand_shouldReturnDeduplicatedItemsSortedByDescription() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlier = now.minusHours(1);

        StockSnapshot s1 = createSnapshot(1, "7891000100101", "Produto A", "Comp A", "Marca X", 5.0, now);
        StockSnapshot s2 = createSnapshot(2, "7891000100102", "Produto B", null, "Marca X", 10.0, now);
        StockSnapshot s3Duplicado = createSnapshot(1, "7891000100101", "Produto A", "Comp A", "Marca X", 4.0, earlier);

        List<StockSnapshot> snapshots = List.of(s1, s2, s3Duplicado);

        when(repository.findByBrandToday("Marca X", LocalDate.now()))
                .thenReturn(snapshots);

        ResponseEntity<List<CountingItemDTO>> response = controller.byBrand("Marca X");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        List<CountingItemDTO> dtos = response.getBody();
        assertThat(dtos).hasSize(2);
        assertThat(dtos.get(0).getDescription()).isEqualTo("Produto A");
        assertThat(dtos.get(1).getDescription()).isEqualTo("Produto B");
        assertThat(dtos.get(0).getCurrentStock()).isEqualTo(5.0);
    }

    @Test
    void byBrand_shouldReturnEmptyListWhenNoSnapshots() {
        when(repository.findByBrandToday("Marca Z", LocalDate.now()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CountingItemDTO>> response = controller.byBrand("Marca Z");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void byProduct_shouldReturnItemsFromBrandWhenProductFound() {
        LocalDateTime now = LocalDateTime.now();
        StockSnapshot snapshot = createSnapshot(10, "7891000100200", "Desc", "Compl", "Marca Y", 5.0, now);

        when(repository.findTopByProductCodeOrderByCapturedAtDesc(10))
                .thenReturn(Optional.of(snapshot));
        when(repository.findByBrandToday("Marca Y", LocalDate.now()))
                .thenReturn(List.of(snapshot));

        ResponseEntity<List<CountingItemDTO>> response = controller.byProduct(10);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProductCode()).isEqualTo(10);
    }

    @Test
    void byProduct_shouldReturnEmptyListWhenProductNotFound() {
        when(repository.findTopByProductCodeOrderByCapturedAtDesc(99))
                .thenReturn(Optional.empty());

        ResponseEntity<List<CountingItemDTO>> response = controller.byProduct(99);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void byProduct_shouldReturnEmptyListWhenBrandIsNull() {
        StockSnapshot snapshot = createSnapshot(10, "7891000100200", "Desc", "Compl", null, 5.0, LocalDateTime.now());

        when(repository.findTopByProductCodeOrderByCapturedAtDesc(10))
                .thenReturn(Optional.of(snapshot));

        ResponseEntity<List<CountingItemDTO>> response = controller.byProduct(10);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
        verify(repository, never()).findByBrandToday(any(), any());
    }

    @Test
    void brandByProduct_shouldReturnBrandWhenProductExists() {
        StockSnapshot snapshot = createSnapshot(1, null, null, null, "Marca W", 0.0, LocalDateTime.now());
        when(repository.findTopByProductCodeOrderByCapturedAtDesc(1))
                .thenReturn(Optional.of(snapshot));

        ResponseEntity<String> response = controller.brandByProduct(1);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo("Marca W");
    }

    @Test
    void brandByProduct_shouldReturnEmptyStringWhenProductNotFound() {
        when(repository.findTopByProductCodeOrderByCapturedAtDesc(1))
                .thenReturn(Optional.empty());

        ResponseEntity<String> response = controller.brandByProduct(1);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void brandByProduct_shouldReturnNullWhenBrandIsNull() {
        StockSnapshot snapshot = createSnapshot(1, null, null, null, null, 0.0, LocalDateTime.now());
        when(repository.findTopByProductCodeOrderByCapturedAtDesc(1))
                .thenReturn(Optional.of(snapshot));

        ResponseEntity<String> response = controller.brandByProduct(1);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void search_shouldReturnResultsByBarcode() {
        LocalDateTime now = LocalDateTime.now();
        StockSnapshot s = createSnapshot(1, "7891000100300", "Desc", "Compl", "Marca", 10.0, now);

        when(repository.searchByBarcodeToday("7891000100300", LocalDate.now()))
                .thenReturn(List.of(s));

        ResponseEntity<List<CountingItemDTO>> response = controller.search("7891000100300");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getBarcode()).isEqualTo("7891000100300");
        verify(repository, never()).searchByProductCodeToday(anyInt(), any());
    }

    @Test
    void search_shouldFallbackToProductCodeWhenBarcodeNotFound() {
        LocalDateTime now = LocalDateTime.now();
        StockSnapshot s = createSnapshot(123, "7891000100400", "Desc", "Compl", "Marca", 10.0, now);

        when(repository.searchByBarcodeToday("123", LocalDate.now()))
                .thenReturn(Collections.emptyList());
        when(repository.searchByProductCodeToday(123, LocalDate.now()))
                .thenReturn(List.of(s));

        ResponseEntity<List<CountingItemDTO>> response = controller.search("123");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProductCode()).isEqualTo(123);
    }

    @Test
    void search_shouldReturnEmptyListWhenNoMatchAndNotNumeric() {
        when(repository.searchByBarcodeToday("abc", LocalDate.now()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<CountingItemDTO>> response = controller.search("abc");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
        verify(repository, never()).searchByProductCodeToday(anyInt(), any());
    }

    @Test
    void reportPdf_shouldReturnPdfWithHeaders() {
        CountingReportRequest request = new CountingReportRequest();
        request.setAuditedLabel("Label");
        request.setAuditType("Tipo");
        request.setAuditedAt("2025-01-01");
        request.setItems(List.of());

        byte[] pdfBytes = {1, 2, 3};
        when(pdfCountingService.generate(request)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = controller.reportPdf(request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("application/pdf");
        assertThat(response.getHeaders().getFirst("Content-Disposition"))
                .isEqualTo("inline; filename=\"contagem.pdf\"");
        assertThat(response.getBody()).isEqualTo(pdfBytes);
    }

    @Test
    void reportPdf_shouldPropagateServiceException() {
        CountingReportRequest request = new CountingReportRequest();
        when(pdfCountingService.generate(request)).thenThrow(new RuntimeException("pdf error"));

        assertThatThrownBy(() -> controller.reportPdf(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("pdf error");
    }

    @Test
    void departments_shouldReturnListOfDepartments() {
        when(repository.findDistinctDepartmentsToday(LocalDate.now()))
                .thenReturn(List.of("Dept A", "Dept B"));

        ResponseEntity<List<String>> response = controller.departments();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsExactly("Dept A", "Dept B");
    }

    @Test
    void departments_shouldReturnEmptyListWhenNoDepartments() {
        when(repository.findDistinctDepartmentsToday(LocalDate.now()))
                .thenReturn(Collections.emptyList());

        ResponseEntity<List<String>> response = controller.departments();

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void byDepartment_shouldReturnDeduplicatedItems() {
        LocalDateTime now = LocalDateTime.now();
        StockSnapshot s1 = createSnapshot(1, "7891000100101", "Item A", "Comp A", "Marca", 5.0, now);
        StockSnapshot s2 = createSnapshot(2, "7891000100102", "Item B", null, "Marca", 10.0, now);

        when(repository.findByDepartmentToday("Dept X", LocalDate.now()))
                .thenReturn(List.of(s1, s2));

        ResponseEntity<List<CountingItemDTO>> response = controller.byDepartment("Dept X");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(2);
    }
}