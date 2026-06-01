package com.vektorcontext.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.vektorcontext.dto.DivergenceRecordResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfReportService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private static final Color COLOR_HEADER_BG   = new Color(30, 30, 30);
    private static final Color COLOR_HEADER_TEXT  = Color.WHITE;
    private static final Color COLOR_ROW_ALT      = new Color(245, 245, 245);
    private static final Color COLOR_FALTA        = new Color(220, 53, 69);
    private static final Color COLOR_SOBRA        = new Color(25, 135, 84);
    private static final Color COLOR_SEM_NF       = new Color(255, 153, 0);
    private static final Color COLOR_BORDER       = new Color(200, 200, 200);

    public byte[] generate(LocalDate date, List<DivergenceRecordResponse> records) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 24, 24, 36, 36);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PageHeader(date));
            document.open();

            addTitle(document, date, records.size());
            document.add(Chunk.NEWLINE);
            addTable(document, records);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private void addTitle(Document document, LocalDate date, int total) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLACK);
        Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 11, new Color(80, 80, 80));

        document.add(new Paragraph("Relatório de Divergências", titleFont));

        Paragraph sub = new Paragraph(date.format(DATE_FORMAT) + "   —   " + total + " registro(s)", subFont);
        sub.setSpacingAfter(4);
        document.add(sub);
    }

    private void addTable(Document document, List<DivergenceRecordResponse> records) throws DocumentException {
        float[] widths = {5, 8, 28, 14, 8, 8, 8, 21};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4);

        addHeaderRow(table);

        boolean alt = false;
        for (DivergenceRecordResponse r : records) {
            Color rowBg = alt ? COLOR_ROW_ALT : Color.WHITE;
            addDataRow(table, r, rowBg);
            alt = !alt;
        }

        document.add(table);
    }

    private void addHeaderRow(PdfPTable table) {
        String[] headers = {"Loja", "Código", "Descrição", "Complemento", "Tipo", "Qtde", "Estoque", "Separador"};
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_HEADER_TEXT);
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setPadding(6);
            cell.setBorderColor(COLOR_HEADER_BG);
            table.addCell(cell);
        }
    }

    private void addDataRow(PdfPTable table, DivergenceRecordResponse r, Color bg) {
        Font font      = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);
        Font tipoFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8, tipoColor(r.getTipo()));

        addCell(table, String.valueOf(r.getStoreCode()), font, bg, Element.ALIGN_CENTER);
        addCell(table, String.valueOf(r.getProductCode()), font, bg, Element.ALIGN_CENTER);
        addCell(table, nvl(r.getProductDescription()), font, bg, Element.ALIGN_LEFT);
        addCell(table, nvl(r.getProductComplement()), font, bg, Element.ALIGN_LEFT);
        addCell(table, nvl(r.getTipo()), tipoFont, bg, Element.ALIGN_CENTER);
        addCell(table, formatNum(r.getQuantity()), font, bg, Element.ALIGN_CENTER);
        addCell(table, formatNum(r.getCurrentStock()), font, bg, Element.ALIGN_CENTER);
        addCell(table, nvl(r.getSeparatorName()), font, bg, Element.ALIGN_LEFT);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(5);
        cell.setHorizontalAlignment(align);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.3f);
        table.addCell(cell);
    }

    private Color tipoColor(String tipo) {
        if (tipo == null) return Color.BLACK;
        return switch (tipo) {
            case "FALTA"  -> COLOR_FALTA;
            case "SOBRA"  -> COLOR_SOBRA;
            case "SEM_NF" -> COLOR_SEM_NF;
            default       -> Color.BLACK;
        };
    }

    private String nvl(String value) {
        return value == null ? "" : value;
    }

    private String formatNum(Double value) {
        if (value == null) return "—";
        if (value == value.longValue()) return String.valueOf(value.longValue());
        return String.format("%.2f", value).replace(".", ",");
    }

    private static class PageHeader extends PdfPageEventHelper {
        private final LocalDate reportDate;
        private PdfTemplate total;
        private BaseFont bf;

        PageHeader(LocalDate reportDate) {
            this.reportDate = reportDate;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            try {
                bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                total = writer.getDirectContent().createTemplate(30, 16);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            cb.saveState();
            cb.setFontAndSize(bf, 8);
            cb.setColorFill(new Color(120, 120, 120));

            String text = "VektorContext  —  " + reportDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            cb.beginText();
            cb.setTextMatrix(document.left(), document.bottom() - 12);
            cb.showText(text);
            cb.endText();

            cb.beginText();
            cb.setTextMatrix(document.right() - 60, document.bottom() - 12);
            cb.showText("Página " + writer.getPageNumber());
            cb.endText();

            cb.restoreState();
        }
    }
}