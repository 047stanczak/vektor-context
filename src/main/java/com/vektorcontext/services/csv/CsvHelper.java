package com.vektorcontext.services.csv;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class CsvHelper {

    public CSVReader buildReader(byte[] fileBytes) throws Exception {
        CSVParser parser = new CSVParserBuilder()
                .withSeparator('|')
                .withIgnoreQuotations(true)
                .build();

        return new CSVReaderBuilder(
                new BufferedReader(
                        new InputStreamReader(
                                new ByteArrayInputStream(fileBytes),
                                "ISO-8859-1"
                        )
                )
        )
                .withCSVParser(parser)
                .build();
    }

    public Map<String, Integer> buildHeaderIndex(String[] header) {
        Map<String, Integer> indexMap = new HashMap<>();
        for (int i = 0; i < header.length; i++) {
            String normalized = normalizeHeader(header[i]);
            if (normalized == null || normalized.isBlank()) {
                continue;
            }
            indexMap.putIfAbsent(normalized, i);
        }
        return indexMap;
    }

    public Integer headerIndex(Map<String, Integer> headerIndex, String... names) {
        for (String name : names) {
            Integer index = headerIndex.get(normalizeHeader(name));
            if (index != null) {
                return index;
            }
        }
        return null;
    }

    public boolean isEmptyRow(String[] cols) {
        if (cols == null || cols.length == 0) {
            return true;
        }
        return Arrays.stream(cols)
                .allMatch(col -> col == null || col.trim().isEmpty());
    }

    public String col(String[] cols, int index) {
        if (index < 0 || index >= cols.length) {
            return null;
        }

        String val = cols[index].trim();
        return val.isEmpty() ? null : val;
    }

    public String col(String[] cols, Integer index) {
        if (index == null) {
            return null;
        }
        return col(cols, index.intValue());
    }

    public Integer parseQuantity(String value) {
        Double parsed = parseDouble(value);
        return parsed == null ? null : parsed.intValue();
    }

    public Double parseDouble(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value.trim()
                .replace(".", "")
                .replace(",", ".");

        return Double.parseDouble(cleaned);
    }

    public LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        String cleaned = value.trim();
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                DateTimeFormatter.ofPattern("d/M/yy"),
                DateTimeFormatter.ofPattern("dd/MM/yy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ISO_LOCAL_DATE
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                return LocalDate.parse(cleaned, formatter);
            } catch (DateTimeParseException ignored) {
            }
        }

        throw new IllegalArgumentException("Formato de data inválido: " + value);
    }

    private String normalizeHeader(String header) {
        if (header == null) {
            return null;
        }

        String normalized = Normalizer.normalize(header, Normalizer.Form.NFD);
        normalized = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        normalized = normalized.toLowerCase(Locale.ROOT);
        normalized = normalized.replaceAll("[^a-z0-9]+", "_");
        normalized = normalized.replaceAll("^_+|_+$", "");
        normalized = normalized.replaceAll("_+", "_");
        return normalized;
    }
}