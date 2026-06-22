package com.vektorcontext.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.vektorcontext.dto.CountingItemDTO;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
public class PdfCountingService {

    private static final Color COLOR_HEADER_BG  = new Color(22, 33, 47);
    private static final Color COLOR_HEADER_TEXT = Color.WHITE;
    private static final Color COLOR_ROW_ALT    = new Color(238, 244, 250);
    private static final Color COLOR_BORDER     = new Color(204, 204, 204);
    private static final Color COLOR_TEXT_MUTED = new Color(107, 114, 128);

    public byte[] generate(String brand, List<CountingItemDTO> items) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, baos);
            writer.setPageEvent(new PageFooter());
            document.open();
            addTitle(document, brand, items.size());
            document.add(new Chunk("\n"));
            addTable(document, items);
            addSignatureArea(document);
            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private void addTitle(Document document, String brand, int total) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_HEADER_BG);
        Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_MUTED);
        document.add(new Paragraph("CONTAGEM - " + brand.toUpperCase(), titleFont));
        Paragraph sub = new Paragraph("Produtos: " + total, subFont);
        sub.setSpacingAfter(8);
        document.add(sub);
        LineSeparator line = new LineSeparator();
        line.setPercentage(100);
        document.add(new Chunk(line));
        document.add(new Chunk("\n"));
    }

    private void addTable(Document document, List<CountingItemDTO> items) throws DocumentException {
        float[] widths = {1.0f, 2.0f, 3.5f, 2.0f, 1.2f, 1.2f};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);
        table.setSpacingAfter(12);

        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_HEADER_TEXT);
        for (String h : new String[]{"Código", "Barcode", "Descrição", "Complemento", "Est. Sis.", "Est. Fís."}) {
            PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setPadding(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderColor(COLOR_HEADER_BG);
            cell.setBorderWidth(0.3f);
            table.addCell(cell);
        }

        Font font = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);
        boolean alt = false;
        for (CountingItemDTO item : items) {
            Color bg = alt ? COLOR_ROW_ALT : Color.WHITE;
            addCell(table, String.valueOf(item.getProductCode()), font, bg, Element.ALIGN_CENTER);
            addCell(table, nvl(item.getBarcode()), font, bg, Element.ALIGN_CENTER);
            addCell(table, nvl(item.getDescription()), font, bg, Element.ALIGN_LEFT);
            addCell(table, nvl(item.getComplement()), font, bg, Element.ALIGN_LEFT);
            addCell(table, formatNum(item.getCurrentStock()), font, bg, Element.ALIGN_CENTER);
            addCell(table, "", font, bg, Element.ALIGN_CENTER);
            alt = !alt;
        }
        document.add(table);
    }

    private void addCell(PdfPTable table, String text, Font font, Color bg, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(3);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(COLOR_BORDER);
        cell.setBorderWidth(0.3f);
        table.addCell(cell);
    }

    private void addSignatureArea(Document document) throws DocumentException {
        document.add(new Chunk("\n\n"));
        Font sigFont    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_HEADER_BG);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

        Paragraph dateLabel = new Paragraph("Data: ___/___/______", normalFont);
        dateLabel.setSpacingAfter(20);
        document.add(dateLabel);

        Paragraph obsLabel = new Paragraph("Observações:", sigFont);
        obsLabel.setSpacingBefore(8);
        obsLabel.setSpacingAfter(4);
        document.add(obsLabel);
        for (int i = 0; i < 3; i++) {
            Paragraph line = new Paragraph("___________________________________________________________________________", normalFont);
            line.setSpacingAfter(4);
            document.add(line);
        }

        document.add(new Chunk("\n"));
        Paragraph sig = new Paragraph("Assinatura", sigFont);
        sig.setSpacingBefore(12);
        sig.setSpacingAfter(4);
        document.add(sig);
        document.add(new Paragraph("___________________________________________________________________________", normalFont));
    }

    private String nvl(String value) { return value == null ? "" : value; }

    private String formatNum(Double value) {
        if (value == null) return "—";
        if (value == value.longValue()) return String.valueOf(value.longValue());
        return String.format("%.2f", value).replace(".", ",");
    }

    private static class PageFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                cb.setFontAndSize(bf, 8);
                cb.setColorFill(new Color(107, 114, 128));
                cb.beginText();
                cb.setTextMatrix(document.leftMargin(), document.bottomMargin() - 12);
                cb.showText("VektorContext");
                cb.endText();
                cb.beginText();
                cb.setTextMatrix(document.rightMargin() - 40, document.bottomMargin() - 12);
                cb.showText("Página " + writer.getPageNumber());
                cb.endText();
                cb.restoreState();
            } catch (Exception e) { throw new RuntimeException(e); }
        }
    }
}