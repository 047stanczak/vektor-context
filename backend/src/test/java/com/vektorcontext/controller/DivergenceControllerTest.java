package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.vektorcontext.api.ApiResponse;
import com.vektorcontext.dto.DivergenceQueryResponse;
import com.vektorcontext.dto.DivergenceRankingItem;
import com.vektorcontext.dto.DivergenceRecordRequest;
import com.vektorcontext.dto.DivergenceRecordResponse;
import com.vektorcontext.exception.DivergenceNotFoundException;
import com.vektorcontext.exception.ProductNotFoundException;
import com.vektorcontext.repository.DivergenceRecordRepository;
import com.vektorcontext.services.DivergenceService;
import com.vektorcontext.services.PdfReportService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class DivergenceControllerTest {

    @Mock
    private DivergenceService divergenceService;

    @Mock
    private PdfReportService pdfReportService;

    @Mock
    private DivergenceRecordRepository divergenceRecordRepository;

    @InjectMocks
    private DivergenceController controller;

    @Test
    void query_withProductCode_shouldReturnProduct() {
        Integer productCode = 123;
        Integer storeCode = 1;
        DivergenceQueryResponse expected = new DivergenceQueryResponse();
        expected.setProductCode(productCode);
        when(divergenceService.queryByProductCode(productCode, storeCode)).thenReturn(expected);

        ResponseEntity<ApiResponse<DivergenceQueryResponse>> response =
                controller.query(productCode, null, storeCode);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        ApiResponse<DivergenceQueryResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isTrue();
        assertThat(body.data()).isEqualTo(expected);
        verify(divergenceService).queryByProductCode(productCode, storeCode);
        verify(divergenceService, never()).queryByBarcode(any(), any());
    }

    @Test
    void query_withBarcode_shouldReturnProduct() {
        String barcode = "7891000100101";
        Integer storeCode = 1;
        DivergenceQueryResponse expected = new DivergenceQueryResponse();
        expected.setBarcode(barcode);
        when(divergenceService.queryByBarcode(barcode, storeCode)).thenReturn(expected);

        ResponseEntity<ApiResponse<DivergenceQueryResponse>> response =
                controller.query(null, barcode, storeCode);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data()).isEqualTo(expected);
        verify(divergenceService).queryByBarcode(barcode, storeCode);
    }

    @Test
    void query_withBothNull_shouldReturnBadRequest() {
        ResponseEntity<ApiResponse<DivergenceQueryResponse>> response =
                controller.query(null, null, 1);

        assertThat(response.getStatusCodeValue()).isEqualTo(400);
        ApiResponse<DivergenceQueryResponse> body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.success()).isFalse();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.message()).contains("productCode");
    }

    @Test
    void query_whenProductNotFound_shouldPropagateException() {
        when(divergenceService.queryByProductCode(999, 1))
                .thenThrow(new ProductNotFoundException("Produto não encontrado"));

        assertThatThrownBy(() -> controller.query(999, null, 1))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("Produto não encontrado");
    }

    @Test
    void saveAll_shouldSaveAndReturnSavedRecords() {
        DivergenceRecordRequest req = new DivergenceRecordRequest();
        List<DivergenceRecordRequest> requests = List.of(req);
        DivergenceRecordResponse resp = new DivergenceRecordResponse();
        resp.setId(1L);
        when(divergenceService.saveAll(requests)).thenReturn(List.of(resp));

        ResponseEntity<ApiResponse<List<DivergenceRecordResponse>>> response =
                controller.saveAll(requests);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data()).hasSize(1);
        assertThat(response.getBody().data().get(0).getId()).isEqualTo(1L);
    }

    @Test
    void saveAll_shouldPropagateServiceException() {
        List<DivergenceRecordRequest> requests = List.of(new DivergenceRecordRequest());
        when(divergenceService.saveAll(requests)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> controller.saveAll(requests))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");
    }

    @Test
    void findByDate_shouldReturnRecords() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        DivergenceRecordResponse rec = new DivergenceRecordResponse();
        rec.setId(10L);
        when(divergenceService.findByDate(date)).thenReturn(List.of(rec));

        ResponseEntity<ApiResponse<List<DivergenceRecordResponse>>> response =
                controller.findByDate(date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data()).hasSize(1);
    }

    @Test
    void findByDate_shouldReturnEmptyList() {
        LocalDate date = LocalDate.of(2025, 1, 15);
        when(divergenceService.findByDate(date)).thenReturn(List.of());

        ResponseEntity<ApiResponse<List<DivergenceRecordResponse>>> response =
                controller.findByDate(date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data()).isEmpty();
    }

    @Test
    void reportPdf_shouldReturnPdfBytesWithHeaders() {
        LocalDate date = LocalDate.of(2025, 2, 20);
        DivergenceRecordResponse rec = new DivergenceRecordResponse();
        rec.setId(1L);
        List<DivergenceRecordResponse> records = List.of(rec);
        byte[] pdfBytes = {4, 5, 6};

        when(divergenceService.findByDateForReport(date)).thenReturn(records);
        when(pdfReportService.generate(date, records)).thenReturn(pdfBytes);

        ResponseEntity<byte[]> response = controller.reportPdf(date);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getContentType()).isEqualTo(MediaType.APPLICATION_PDF);
        assertThat(response.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION))
                .contains("inline; filename=\"divergencias-2025-02-20.pdf\"");
        assertThat(response.getBody()).isEqualTo(pdfBytes);
    }

    @Test
    void reportPdf_whenNoRecords_shouldPropagateException() {
        LocalDate date = LocalDate.of(2025, 2, 20);
        when(divergenceService.findByDateForReport(date))
                .thenThrow(new DivergenceNotFoundException("Nenhuma divergência encontrada"));

        assertThatThrownBy(() -> controller.reportPdf(date))
                .isInstanceOf(DivergenceNotFoundException.class)
                .hasMessageContaining("Nenhuma divergência");
    }

    @Test
    void update_shouldUpdateAndReturnRecord() {
        Long id = 1L;
        DivergenceRecordRequest request = new DivergenceRecordRequest();
        DivergenceRecordResponse updated = new DivergenceRecordResponse();
        updated.setId(id);
        when(divergenceService.update(id, request)).thenReturn(updated);

        ResponseEntity<ApiResponse<DivergenceRecordResponse>> response =
                controller.update(id, request);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().data().getId()).isEqualTo(id);
    }

    @Test
    void update_whenNotFound_shouldPropagateException() {
        Long id = 99L;
        DivergenceRecordRequest request = new DivergenceRecordRequest();
        when(divergenceService.update(id, request))
                .thenThrow(new DivergenceNotFoundException("Divergência não encontrada"));

        assertThatThrownBy(() -> controller.update(id, request))
                .isInstanceOf(DivergenceNotFoundException.class);
    }

    @Test
    void delete_shouldCallServiceDelete() {
        Long id = 1L;

        ResponseEntity<ApiResponse<Void>> response = controller.delete(id);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().message()).contains("excluída");
        verify(divergenceService).delete(id);
    }

    @Test
    void delete_whenNotFound_shouldPropagateException() {
        Long id = 99L;
        doThrow(new DivergenceNotFoundException("Divergência não encontrada"))
                .when(divergenceService).delete(id);

        assertThatThrownBy(() -> controller.delete(id))
                .isInstanceOf(DivergenceNotFoundException.class);
    }

    @Test
    void rankBySeparator_shouldReturnRanking() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        List<DivergenceRankingItem> items = List.of(
                new DivergenceRankingItem("Separador A", 10L)
        );
        when(divergenceRecordRepository.rankBySeparator(from, to)).thenReturn(items);

        ResponseEntity<List<DivergenceRankingItem>> response =
                controller.rankBySeparator(from, to);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getName()).isEqualTo("Separador A");
    }

    @Test
    void rankByProduct_shouldReturnRanking() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);
        List<DivergenceRankingItem> items = List.of(
                new DivergenceRankingItem("Produto X", 25L)
        );
        when(divergenceRecordRepository.rankByProduct(from, to)).thenReturn(items);

        ResponseEntity<List<DivergenceRankingItem>> response =
                controller.rankByProduct(from, to);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody().get(0).getTotal()).isEqualTo(25L);
    }
}