package com.vektorcontext.controller;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.vektorcontext.dto.SeparationProductDTO;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.SeparationProduct;
import com.vektorcontext.repository.SeparationProductRepository;
import com.vektorcontext.services.TransactionFinder;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class SeparationProductControllerTest {

    @Mock
    private SeparationProductRepository repository;

    @Mock
    private TransactionFinder transactionFinder;

    @InjectMocks
    private SeparationProductController controller;

    private SeparationProduct createSeparationProduct(Long id, Integer productCode, String description,
                                                      String complement, String storeCode, Double quantity) {
        SeparationProduct sp = new SeparationProduct();
        sp.setId(id);
        sp.setProductCode(productCode);
        sp.setStoreCode(storeCode);
        sp.setQuantity(quantity);
        Product product = new Product();
        product.setCode(productCode);
        product.setDescription(description);
        product.setComplement(complement);
        sp.setProduct(product);
        return sp;
    }

    @Test
    void oldPending_success_returnsList() {
        SeparationProduct sp1 = createSeparationProduct(1L, 100, "Desc A", "Comp A", "01", 5.0);
        SeparationProduct sp2 = createSeparationProduct(2L, 200, "Desc B", "Comp B", "02", 10.0);
        when(repository.findOldPending(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(sp1, sp2));

        ResponseEntity<List<SeparationProductDTO>> response = controller.oldPending(15);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        List<SeparationProductDTO> body = response.getBody();
        assertThat(body).hasSize(2);
        assertThat(body.get(0).getProductCode()).isEqualTo(100);
        assertThat(body.get(0).getProductDescription()).isEqualTo("Desc A");
        assertThat(body.get(0).getProductComplement()).isEqualTo("Comp A");
        assertThat(body.get(1).getProductCode()).isEqualTo(200);
    }

    @Test
    void oldPending_empty_returnsEmptyList() {
        when(repository.findOldPending(any(), any())).thenReturn(List.of());

        ResponseEntity<List<SeparationProductDTO>> response = controller.oldPending(15);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void oldPending_exception_propagates() {
        when(repository.findOldPending(any(), any())).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> controller.oldPending(15))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("DB error");
    }

    @Test
    void oldPendingWithStock_success_returnsList() {
        SeparationProduct sp = createSeparationProduct(1L, 100, "Desc", "Comp", "01", 5.0);
        when(repository.findOldPendingWithStock(any(LocalDate.class), any(LocalDate.class), any(LocalDateTime.class)))
                .thenReturn(List.of(sp));

        ResponseEntity<List<SeparationProductDTO>> response = controller.oldPendingWithStock(10);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void oldPendingNoStock_success_returnsList() {
        SeparationProduct sp = createSeparationProduct(1L, 100, "Desc", "Comp", "01", 5.0);
        when(repository.findOldPendingNoStock(any(LocalDate.class), any(LocalDate.class), any(LocalDateTime.class)))
                .thenReturn(List.of(sp));

        ResponseEntity<List<SeparationProductDTO>> response = controller.oldPendingNoStock(20);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void pendingByBarcode_success_returnsList() {
        SeparationProduct sp1 = createSeparationProduct(1L, 100, "Item 1", "Comp 1", "01", 3.0);
        SeparationProduct sp2 = createSeparationProduct(2L, 200, "Item 2", "Comp 2", "02", 7.0);
        when(repository.findPendingByBarcode(eq("7891234567890"), any(LocalDate.class)))
                .thenReturn(List.of(sp1, sp2));

        ResponseEntity<List<SeparationProductDTO>> response =
                controller.pendingByBarcode("7891234567890");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        List<SeparationProductDTO> dtos = response.getBody();
        assertThat(dtos).hasSize(2);
    }

    @Test
    void pendingByBarcode_empty_returnsEmptyList() {
        when(repository.findPendingByBarcode(anyString(), any())).thenReturn(List.of());

        ResponseEntity<List<SeparationProductDTO>> response =
                controller.pendingByBarcode("notfound");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void pendingByCode_success_integerCode() {
        SeparationProduct sp = createSeparationProduct(1L, 123, "Prod 123", null, "A1", 2.0);
        when(repository.findPendingByProductCode(eq(123), any(LocalDate.class)))
                .thenReturn(List.of(sp));

        ResponseEntity<List<SeparationProductDTO>> response =
                controller.pendingByCode("123");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getProductCode()).isEqualTo(123);
        verify(repository, never()).findPendingByBarcode(anyString(), any());
    }

    @Test
    void pendingByCode_fallbackToBarcode_whenNotInteger() {
        SeparationProduct sp = createSeparationProduct(1L, 999, "Barcode Product", "C", "S1", 1.0);
        when(repository.findPendingByBarcode(eq("ABC123"), any(LocalDate.class)))
                .thenReturn(List.of(sp));

        ResponseEntity<List<SeparationProductDTO>> response =
                controller.pendingByCode("ABC123");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).hasSize(1);
        verify(repository, never()).findPendingByProductCode(anyInt(), any());
    }

    @Test
    void pendingByCode_empty_returnsEmptyList() {
        when(repository.findPendingByProductCode(eq(404), any())).thenReturn(List.of());

        ResponseEntity<List<SeparationProductDTO>> response =
                controller.pendingByCode("404");

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void pendingByCode_exception_propagates() {
        when(repository.findPendingByProductCode(anyInt(), any()))
                .thenThrow(new RuntimeException("Query failed"));

        assertThatThrownBy(() -> controller.pendingByCode("1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Query failed");
    }
}