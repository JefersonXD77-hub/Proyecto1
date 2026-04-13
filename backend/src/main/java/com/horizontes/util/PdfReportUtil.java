package com.horizontes.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletResponse;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class PdfReportUtil {

    private PdfReportUtil() {
    }

    public static void exportTableReport(
            HttpServletResponse response,
            String fileName,
            String title,
            String subtitle,
            List<String> headers,
            List<Map<String, Object>> rows) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            document.add(new Paragraph(title, titleFont));
            if (subtitle != null && !subtitle.isBlank()) {
                document.add(new Paragraph(subtitle, subtitleFont));
            }
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(52, 73, 94));
                cell.setPadding(6);
                table.addCell(cell);
            }

            if (rows == null || rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay datos para mostrar", bodyFont));
                emptyCell.setColspan(headers.size());
                emptyCell.setPadding(8);
                table.addCell(emptyCell);
            } else {
                for (Map<String, Object> row : rows) {
                    for (String header : headers) {
                        Object value = row.get(header);
                        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value.toString() : "", bodyFont));
                        cell.setPadding(5);
                        table.addCell(cell);
                    }
                }
            }

            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Error al generar PDF", e);
        } finally {
            document.close();
        }
    }

    public static void exportKeyValueReport(
            HttpServletResponse response,
            String fileName,
            String title,
            String subtitle,
            Map<String, Object> values) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font keyFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL);

            document.add(new Paragraph(title, titleFont));
            if (subtitle != null && !subtitle.isBlank()) {
                document.add(new Paragraph(subtitle, subtitleFont));
            }
            document.add(new Paragraph(" "));

            if (values == null || values.isEmpty()) {
                document.add(new Paragraph("No hay datos para mostrar", valueFont));
            } else {
                for (Map.Entry<String, Object> entry : values.entrySet()) {
                    Paragraph paragraph = new Paragraph();
                    paragraph.add(new Phrase(entry.getKey() + ": ", keyFont));
                    paragraph.add(new Phrase(entry.getValue() != null ? entry.getValue().toString() : "", valueFont));
                    paragraph.setSpacingAfter(8f);
                    document.add(paragraph);
                }
            }

        } catch (DocumentException e) {
            throw new IOException("Error al generar PDF", e);
        } finally {
            document.close();
        }
    }

    public static void exportSummaryAndTableReport(
            HttpServletResponse response,
            String fileName,
            String title,
            String subtitle,
            Map<String, Object> summaryValues,
            String detailTitle,
            List<String> headers,
            List<Map<String, Object>> rows) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        Document document = new Document(PageSize.A4.rotate(), 20, 20, 20, 20);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font subtitleFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
            Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font keyFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 9, Font.NORMAL);

            document.add(new Paragraph(title, titleFont));
            if (subtitle != null && !subtitle.isBlank()) {
                document.add(new Paragraph(subtitle, subtitleFont));
            }
            document.add(new Paragraph(" "));

            if (summaryValues != null && !summaryValues.isEmpty()) {
                document.add(new Paragraph("Resumen", sectionFont));
                document.add(new Paragraph(" "));

                for (Map.Entry<String, Object> entry : summaryValues.entrySet()) {
                    Paragraph paragraph = new Paragraph();
                    paragraph.add(new Phrase(entry.getKey() + ": ", keyFont));
                    paragraph.add(new Phrase(entry.getValue() != null ? entry.getValue().toString() : "", valueFont));
                    paragraph.setSpacingAfter(8f);
                    document.add(paragraph);
                }

                document.add(new Paragraph(" "));
            }

            if (detailTitle != null && !detailTitle.isBlank()) {
                document.add(new Paragraph(detailTitle, sectionFont));
                document.add(new Paragraph(" "));
            }

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(new Color(52, 73, 94));
                cell.setPadding(6);
                table.addCell(cell);
            }

            if (rows == null || rows.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay datos para mostrar", bodyFont));
                emptyCell.setColspan(headers.size());
                emptyCell.setPadding(8);
                table.addCell(emptyCell);
            } else {
                for (Map<String, Object> row : rows) {
                    for (String header : headers) {
                        Object value = row.get(header);
                        PdfPCell cell = new PdfPCell(new Phrase(value != null ? value.toString() : "", bodyFont));
                        cell.setPadding(5);
                        table.addCell(cell);
                    }
                }
            }

            document.add(table);

        } catch (DocumentException e) {
            throw new IOException("Error al generar PDF", e);
        } finally {
            document.close();
        }
    }
}