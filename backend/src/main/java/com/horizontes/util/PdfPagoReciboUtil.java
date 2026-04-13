package com.horizontes.util;

import com.horizontes.dao.PagoDAO;
import com.horizontes.model.Pago;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

public class PdfPagoReciboUtil {

    private static final DecimalFormat MONEY = new DecimalFormat("Q #,##0.00");
    private static final SimpleDateFormat DATE_TIME = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat DATE_ONLY = new SimpleDateFormat("yyyy-MM-dd");

    private PdfPagoReciboUtil() {
    }

    public static void exportPaymentReceipt(
            HttpServletResponse response,
            String fileName,
            PagoDAO.ComprobantePagoInfo info,
            List<Pago> pagos) throws IOException {

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");

        Document document = new Document(PageSize.A4, 40, 40, 40, 40);

        try {
            PdfWriter.getInstance(document, response.getOutputStream());
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
            Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD);
            Font keyFont = new Font(Font.HELVETICA, 11, Font.BOLD);
            Font valueFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
            Font headerFont = new Font(Font.HELVETICA, 10, Font.BOLD, Color.WHITE);
            Font bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

            document.add(new Paragraph("Comprobante de Pago", titleFont));
            document.add(new Paragraph(" "));
            document.add(buildLine("Numero de reservacion", info.numeroReservacion(), keyFont, valueFont));
            document.add(buildLine("Fecha de creacion de reservacion",
                    info.fechaCreacion() != null ? DATE_TIME.format(info.fechaCreacion()) : "", keyFont, valueFont));
            document.add(buildLine("Fecha de viaje",
                    info.fechaViaje() != null ? DATE_ONLY.format(info.fechaViaje()) : "", keyFont, valueFont));
            document.add(buildLine("Paquete turistico", info.nombrePaquete(), keyFont, valueFont));
            document.add(buildLine("Agente", info.agente(), keyFont, valueFont));
            document.add(buildLine("Estado actual", info.estadoReservacion(), keyFont, valueFont));
            document.add(buildLine("Costo total", MONEY.format(info.costoTotal()), keyFont, valueFont));
            document.add(buildLine("Total pagado", MONEY.format(info.totalPagado()), keyFont, valueFont));
            document.add(buildLine("Saldo pendiente", MONEY.format(info.saldoPendiente()), keyFont, valueFont));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Detalle de pagos registrados", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{1.2f, 2.2f, 2.2f, 1.8f});

            addHeaderCell(table, "ID Pago", headerFont);
            addHeaderCell(table, "Fecha", headerFont);
            addHeaderCell(table, "Metodo", headerFont);
            addHeaderCell(table, "Monto", headerFont);

            if (pagos == null || pagos.isEmpty()) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("No hay pagos registrados", bodyFont));
                emptyCell.setColspan(4);
                emptyCell.setPadding(8);
                table.addCell(emptyCell);
            } else {
                for (Pago pago : pagos) {
                    addBodyCell(table, String.valueOf(pago.getIdPago()), bodyFont);
                    addBodyCell(table,
                            pago.getFechaPago() != null ? DATE_ONLY.format(pago.getFechaPago()) : "",
                            bodyFont);
                    addBodyCell(table,
                            pago.getMetodoPago() != null ? pago.getMetodoPago().getNombre() : "",
                            bodyFont);
                    addBodyCell(table, MONEY.format(pago.getMonto()), bodyFont);
                }
            }

            document.add(table);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Este comprobante refleja que la reservacion ha sido cubierta totalmente.", valueFont));

        } catch (DocumentException e) {
            throw new IOException("Error al generar comprobante PDF", e);
        } finally {
            document.close();
        }
    }

    private static Paragraph buildLine(String key, String value, Font keyFont, Font valueFont) {
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Phrase(key + ": ", keyFont));
        paragraph.add(new Phrase(value != null ? value : "", valueFont));
        paragraph.setSpacingAfter(7f);
        return paragraph;
    }

    private static void addHeaderCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(new Color(52, 73, 94));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private static void addBodyCell(PdfPTable table, String text, Font font) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", font));
        cell.setPadding(5);
        table.addCell(cell);
    }
}