package com.vektorcontext.services;

import com.vektorcontext.dto.CountingItemDTO;
import com.vektorcontext.dto.CountingReportRequest;
import com.vektorcontext.models.Product;
import com.vektorcontext.models.StockSnapshot;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfCountingServiceTest {

    private final PdfCountingService service = new PdfCountingService();

    private CountingItemDTO item(Integer code, Double stock) {
        Product product = new Product();
        product.setCode(code);
        product.setBarcode("789" + code);
        product.setDescription("Produto " + code);
        product.setComplement("Compl");

        StockSnapshot snapshot = new StockSnapshot();
        snapshot.setProductCode(code);
        snapshot.setCurrentStock(stock);
        snapshot.setProduct(product);

        return CountingItemDTO.from(snapshot);
    }

    @Test
    void generate_returnsNonEmptyPdfBytes() {
        CountingReportRequest request = new CountingReportRequest();
        request.setAuditedLabel("Marca X");
        request.setAuditType("brand");
        request.setAuditedAt("01/01/2026 10:00");
        request.setItems(List.of(item(10, 5.0), item(11, 3.5)));

        byte[] pdf = service.generate(request);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    void generate_withEmptyItems_returnsValidPdf() {
        CountingReportRequest request = new CountingReportRequest();
        request.setAuditedLabel("Marca Y");
        request.setAuditType("brand");
        request.setAuditedAt("01/01/2026 10:00");
        request.setItems(List.of());

        byte[] pdf = service.generate(request);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generate_withNullOptionalFields_doesNotThrow() {
        CountingReportRequest request = new CountingReportRequest();
        request.setAuditedLabel(null);
        request.setAuditType(null);
        request.setAuditedAt(null);
        request.setItems(List.of(item(10, null)));

        byte[] pdf = service.generate(request);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
