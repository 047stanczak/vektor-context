package com.vektorcontext.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.pdf.draw.LineSeparator;
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

    private static final Color COLOR_HEADER_BG   = new Color(22, 33, 47);     
    private static final Color COLOR_HEADER_TEXT  = Color.WHITE;
    private static final Color COLOR_ROW_ALT      = new Color(238, 244, 250); 
    private static final Color COLOR_FALTA        = new Color(192, 57, 43);   
    private static final Color COLOR_SOBRA        = new Color(39, 174, 96); 
    private static final Color COLOR_SEM_NF       = new Color(255, 153, 0);
    private static final Color COLOR_BORDER       = new Color(204, 204, 204);
    private static final Color COLOR_TEXT_MUTED   = new Color(107, 114, 128);

    public byte[] generate(LocalDate date, List<DivergenceRecordResponse> records) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4.rotate(), 30, 30, 30, 30);
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            writer.setPageEvent(new PageHeader(date));
            document.open();

            addTitle(document, date, records);
            document.add(new Chunk("\n"));
            addTable(document, records);
            addSignatureArea(document);

            document.close();
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF: " + e.getMessage(), e);
        }
    }

    private void addTitle(Document document, LocalDate date, List<DivergenceRecordResponse> records) throws DocumentException {
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13, COLOR_HEADER_BG);
        Font subFont   = FontFactory.getFont(FontFactory.HELVETICA, 9, COLOR_TEXT_MUTED);

        document.add(new Paragraph("DOCUMENTO DE CONFERÊNCIA DE ESTOQUE - LISTA CONSOLIDADA DO DIA", titleFont));

        Paragraph sub = new Paragraph(
            String.format("Data: %s   •   Ocorrências: %d • Itens divergentes: %d", 
                date.format(DATE_FORMAT), 
                records.size(),
                records.size()),
            subFont
        );
        sub.setSpacingAfter(8);
        document.add(sub);

        LineSeparator line = new LineSeparator();
        line.setPercentage(100);
        document.add(new Chunk(line));
        document.add(new Chunk("\n"));
    }

    private void addTable(Document document, List<DivergenceRecordResponse> records) throws DocumentException {
        float[] widths = {0.8f, 1.1f, 0.8f, 1.6f, 3.5f, 2.3f, 1.6f, 2.2f, 1.3f, 1.3f};
        PdfPTable table = new PdfPTable(widths);
        table.setWidthPercentage(100);
        table.setSpacingBefore(6);
        table.setSpacingAfter(12);

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
        String[] headers = {"Aut.", "Tipo", "Qtd", "Cód. Int.", "Nome", "Complemento", "Loja", "Separador", "Est. Sist.", "Est. Fís."};
        Font font = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, COLOR_HEADER_TEXT);
        
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, font));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setPadding(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBorderColor(COLOR_HEADER_BG);
            cell.setBorderWidth(0.3f);
            table.addCell(cell);
        }
    }

    private void addDataRow(PdfPTable table, DivergenceRecordResponse r, Color bg) {
        Font font      = FontFactory.getFont(FontFactory.HELVETICA, 7.5f, Color.BLACK);
        Font tipoFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 7.5f, tipoColor(r.getTipo()));

        addCell(table, "[ ]", font, bg, Element.ALIGN_CENTER);
        
        PdfPCell cellTipo = new PdfPCell(new Phrase(nvl(r.getTipo()).toUpperCase(), tipoFont));
        cellTipo.setBackgroundColor(bg);
        cellTipo.setPadding(3);
        cellTipo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTipo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellTipo.setBorderColor(COLOR_BORDER);
        cellTipo.setBorderWidth(0.3f);
        table.addCell(cellTipo);

        addCell(table, formatNum(r.getQuantity()), font, bg, Element.ALIGN_CENTER);

        addCell(table, nvl(String.valueOf(r.getProductCode())), font, bg, Element.ALIGN_CENTER);

        addCell(table, nvl(r.getProductDescription()), font, bg, Element.ALIGN_LEFT);
        
        addCell(table, nvl(r.getProductComplement()), font, bg, Element.ALIGN_LEFT);

        addCell(table, String.valueOf(r.getStoreCode()), font, bg, Element.ALIGN_CENTER);

        addCell(table, nvl(r.getSeparatorName()), font, bg, Element.ALIGN_LEFT);

        addCell(table, formatNum(r.getCurrentStock()), font, bg, Element.ALIGN_CENTER);

        addCell(table, "", font, bg, Element.ALIGN_CENTER);
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
        
        Font sigFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, COLOR_HEADER_BG);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.BLACK);

        Paragraph sig = new Paragraph("Assinatura do Gerente/Coordenador", sigFont);
        sig.setAlignment(Element.ALIGN_CENTER);
        sig.setSpacingBefore(12);
        sig.setSpacingAfter(12);
        document.add(sig);

        Paragraph sigLine = new Paragraph("_________________________________________________", normalFont);
        sigLine.setAlignment(Element.ALIGN_CENTER);
        sigLine.setSpacingAfter(8);
        document.add(sigLine);

        Paragraph dateLabel = new Paragraph("Data: ___/___/______", normalFont);
        dateLabel.setAlignment(Element.ALIGN_CENTER);
        dateLabel.setSpacingAfter(20);
        document.add(dateLabel);

        Paragraph obsLabel = new Paragraph("Observações:", sigFont);
        obsLabel.setSpacingBefore(8);
        obsLabel.setSpacingAfter(4);
        document.add(obsLabel);
        
        Paragraph obsLine1 = new Paragraph("_________________________________________________________________________________", normalFont);
        obsLine1.setSpacingAfter(4);
        document.add(obsLine1);
        
        Paragraph obsLine2 = new Paragraph("_________________________________________________________________________________", normalFont);
        obsLine2.setSpacingAfter(4);
        document.add(obsLine2);
    }

    private Color tipoColor(String tipo) {
        if (tipo == null) return Color.BLACK;
        return switch (tipo.toUpperCase()) {
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

        PageHeader(LocalDate reportDate) {
            this.reportDate = reportDate;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();
                cb.saveState();
                
                BaseFont bf = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                cb.setFontAndSize(bf, 8);
                cb.setColorFill(new Color(107, 114, 128)); 

                String text = "Sistema de Divergências CD";
                cb.beginText();
                cb.setTextMatrix(document.leftMargin(), document.bottomMargin() - 12);
                cb.showText(text);
                cb.endText();

                cb.beginText();
                cb.setTextMatrix(document.rightMargin() - 40, document.bottomMargin() - 12);
                cb.showText("Página " + writer.getPageNumber());
                cb.endText();

                cb.restoreState();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}