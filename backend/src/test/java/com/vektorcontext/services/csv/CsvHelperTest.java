package com.vektorcontext.services.csv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class CsvHelperTest {

    private CsvHelper csvHelper;

    @BeforeEach
    void setUp() {
        csvHelper = new CsvHelper();
    }

    @Test
    void parseDouble_normalValue() {
        assertEquals(1.5, csvHelper.parseDouble("1,5"));
    }

    @Test
    void parseDouble_withTextSuffix() {
        assertEquals(6.0, csvHelper.parseDouble("6 / UN"));
    }

    @Test
    void parseDouble_null() {
        assertNull(csvHelper.parseDouble(null));
    }

    @Test
    void parseDouble_blank() {
        assertNull(csvHelper.parseDouble("   "));
    }

    @Test
    void parseDouble_thousandSeparator() {
        assertEquals(1500.0, csvHelper.parseDouble("1.500,00"));
    }

    @Test
    void parseDate_shortFormat() {
        assertEquals(LocalDate.of(2025, 6, 1), csvHelper.parseDate("01/06/25"));
    }

    @Test
    void parseDate_longFormat() {
        assertEquals(LocalDate.of(2025, 6, 1), csvHelper.parseDate("01/06/2025"));
    }

    @Test
    void parseDate_isoFormat() {
        assertEquals(LocalDate.of(2025, 6, 1), csvHelper.parseDate("2025-06-01"));
    }

    @Test
    void parseDate_invalidFormat() {
        assertThrows(IllegalArgumentException.class, () -> csvHelper.parseDate("not-a-date"));
    }

    @Test
    void headerIndex_withAccents() {
        Map<String, Integer> index = csvHelper.buildHeaderIndex(new String[]{"Código", "Descrição"});
        assertEquals(0, csvHelper.headerIndex(index, "codigo"));
        assertEquals(1, csvHelper.headerIndex(index, "descricao"));
    }

    @Test
    void headerIndex_withSpacesAndUppercase() {
        Map<String, Integer> index = csvHelper.buildHeaderIndex(new String[]{"Nome Ult Forn"});
        assertEquals(0, csvHelper.headerIndex(index, "nome_ult_forn"));
    }

    @Test
    void headerIndex_notFound() {
        Map<String, Integer> index = csvHelper.buildHeaderIndex(new String[]{"Codigo"});
        assertNull(csvHelper.headerIndex(index, "barcode"));
    }
}