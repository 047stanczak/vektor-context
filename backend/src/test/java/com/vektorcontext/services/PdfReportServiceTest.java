package com.vektorcontext.services;

import com.vektorcontext.dto.DivergenceRecordResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PdfReportServiceTest {

    private final PdfReportService service = new PdfReportService();

    private DivergenceRecordResponse record(String tipo) {
        DivergenceRecordResponse r = new DivergenceRecordResponse();
        r.setId(1L);
        r.setDate(LocalDate.of(2026, 1, 1));
        r.setStoreCode(1);
        r.setProductCode(10);
        r.setProductDescription("Produto 10");
        r.setProductComplement("Compl");
        r.setTipo(tipo);
        r.setQuantity(2.0);
        r.setCurrentStock(5.0);
        r.setSeparatorName("Maria");
        return r;
    }

    @Test
    void generate_returnsNonEmptyPdfBytes() {
        byte[] pdf = service.generate(LocalDate.of(2026, 1, 1), List.of(record("FALTA")));

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        assertEquals("%PDF", new String(pdf, 0, 4));
    }

    @Test
    void generate_withEmptyRecords_returnsValidPdf() {
        byte[] pdf = service.generate(LocalDate.of(2026, 1, 1), List.of());

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generate_withDifferentTipos_doesNotThrow() {
        List<DivergenceRecordResponse> records = List.of(
                record("FALTA"), record("SOBRA"), record("SEM_NF"), record("OUTRO")
        );

        byte[] pdf = service.generate(LocalDate.of(2026, 1, 1), records);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generate_withNullOptionalFields_doesNotThrow() {
        DivergenceRecordResponse r = record(null);
        r.setProductDescription(null);
        r.setProductComplement(null);
        r.setSeparatorName(null);

        byte[] pdf = service.generate(LocalDate.of(2026, 1, 1), List.of(r));

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }
}
