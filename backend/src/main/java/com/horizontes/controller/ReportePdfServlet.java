package com.horizontes.controller;

import com.horizontes.dao.ReporteDAO;
import com.horizontes.util.PdfReportUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/private/reportes/pdf/*")
public class ReportePdfServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Debe indicar el tipo de reporte PDF");
            return;
        }

        String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        Date fechaInicio;
        Date fechaFin;

        try {
            fechaInicio = parseDate(request.getParameter("fechaInicio"));
            fechaFin = parseDate(request.getParameter("fechaFin"));
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Formato de fecha invalido. Use YYYY-MM-DD");
            return;
        }

        try {
            switch (cleanPath) {
                case "ventas" -> exportVentasPdf(response, fechaInicio, fechaFin);
                case "cancelaciones" -> exportCancelacionesPdf(response, fechaInicio, fechaFin);
                case "ocupacion-destino" -> exportOcupacionDestinoPdf(response, fechaInicio, fechaFin);
                case "ganancias" -> exportGananciasPdf(response, fechaInicio, fechaFin);
                case "agente-mas-ventas" -> exportAgenteMasVentasPdf(response, fechaInicio, fechaFin);
                case "agente-mas-ganancias" -> exportAgenteMasGananciasPdf(response, fechaInicio, fechaFin);
                case "paquete-mas-vendido" -> exportPaqueteMasVendidoPdf(response, fechaInicio, fechaFin);
                case "paquete-menos-vendido" -> exportPaqueteMenosVendidoPdf(response, fechaInicio, fechaFin);
                default -> response.sendError(HttpServletResponse.SC_NOT_FOUND, "Reporte PDF no soportado");
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al generar reporte PDF: " + e.getMessage());
        }
    }

    private void exportVentasPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        List<Map<String, Object>> rows = reporteDAO.reporteVentas(fechaInicio, fechaFin);

        List<String> headers = Arrays.asList(
                "id_reservacion",
                "numero_reservacion",
                "fecha_creacion",
                "fecha_viaje",
                "costo_total",
                "nombre_paquete",
                "agente",
                "pasajeros"
        );

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportTableReport(
                response,
                "reporte_ventas.pdf",
                "Reporte de Ventas",
                subtitle,
                headers,
                rows
        );
    }

    private void exportCancelacionesPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        List<Map<String, Object>> rows = reporteDAO.reporteCancelaciones(fechaInicio, fechaFin);

        List<String> headers = Arrays.asList(
                "id_cancelacion",
                "numero_reservacion",
                "fecha_cancelacion",
                "monto_reembolsado",
                "perdida_agencia"
        );

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportTableReport(
                response,
                "reporte_cancelaciones.pdf",
                "Reporte de Cancelaciones",
                subtitle,
                headers,
                rows
        );
    }

    private void exportOcupacionDestinoPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        List<Map<String, Object>> rows = reporteDAO.reporteOcupacionPorDestino(fechaInicio, fechaFin);

        List<String> headers = Arrays.asList(
                "destino",
                "cantidad_reservaciones"
        );

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportTableReport(
                response,
                "reporte_ocupacion_destino.pdf",
                "Reporte de Ocupacion por Destino",
                subtitle,
                headers,
                rows
        );
    }

    private void exportGananciasPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        Map<String, Object> values = reporteDAO.reporteGanancias(fechaInicio, fechaFin);

        Map<String, Object> orderedValues = new LinkedHashMap<>();
        orderedValues.put("Total Ganancias Brutas", values.get("totalGananciasBrutas"));
        orderedValues.put("Total Reembolsos", values.get("totalReembolsos"));
        orderedValues.put("Ganancia Neta", values.get("gananciaNeta"));

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportKeyValueReport(
                response,
                "reporte_ganancias.pdf",
                "Reporte de Ganancias",
                subtitle,
                orderedValues
        );
    }

    private void exportAgenteMasVentasPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        Map<String, Object> result = reporteDAO.reporteAgenteMasVentas(fechaInicio, fechaFin);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Agente", result.get("agente"));
        summary.put("Total de Ventas", result.get("totalVentas"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("reservaciones");

        List<String> headers = Arrays.asList(
                "numero_reservacion",
                "fecha_creacion",
                "costo_total",
                "nombre_paquete"
        );

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportSummaryAndTableReport(
                response,
                "reporte_agente_mas_ventas.pdf",
                "Reporte del Agente con Mas Ventas",
                subtitle,
                summary,
                "Detalle de Reservaciones",
                headers,
                rows
        );
    }

    private void exportAgenteMasGananciasPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        Map<String, Object> result = reporteDAO.reporteAgenteMasGanancias(fechaInicio, fechaFin);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Agente", result.get("agente"));
        summary.put("Total de Ganancia", result.get("totalGanancia"));

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportKeyValueReport(
                response,
                "reporte_agente_mas_ganancias.pdf",
                "Reporte del Agente con Mas Ganancias",
                subtitle,
                summary
        );
    }

    private void exportPaqueteMasVendidoPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        Map<String, Object> result = reporteDAO.reportePaqueteMasVendido(fechaInicio, fechaFin, false);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Paquete", result.get("paquete"));
        summary.put("Total de Reservaciones", result.get("totalReservaciones"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("reservaciones");

        List<String> headers = Arrays.asList(
                "numero_reservacion",
                "fecha_creacion",
                "costo_total"
        );

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportSummaryAndTableReport(
                response,
                "reporte_paquete_mas_vendido.pdf",
                "Reporte del Paquete Mas Vendido",
                subtitle,
                summary,
                "Detalle de Reservaciones",
                headers,
                rows
        );
    }

    private void exportPaqueteMenosVendidoPdf(HttpServletResponse response, Date fechaInicio, Date fechaFin)
            throws SQLException, IOException {

        Map<String, Object> result = reporteDAO.reportePaqueteMasVendido(fechaInicio, fechaFin, true);

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("Paquete", result.get("paquete"));
        summary.put("Total de Reservaciones", result.get("totalReservaciones"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rows = (List<Map<String, Object>>) result.get("reservaciones");

        List<String> headers = Arrays.asList(
                "numero_reservacion",
                "fecha_creacion",
                "costo_total"
        );

        String subtitle = buildSubtitle(fechaInicio, fechaFin);

        PdfReportUtil.exportSummaryAndTableReport(
                response,
                "reporte_paquete_menos_vendido.pdf",
                "Reporte del Paquete Menos Vendido",
                subtitle,
                summary,
                "Detalle de Reservaciones",
                headers,
                rows
        );
    }

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        return Date.valueOf(value);
    }

    private String buildSubtitle(Date fechaInicio, Date fechaFin) {
        if (fechaInicio == null && fechaFin == null) {
            return "Periodo: todos los registros";
        }
        if (fechaInicio != null && fechaFin != null) {
            return "Periodo: " + fechaInicio + " a " + fechaFin;
        }
        if (fechaInicio != null) {
            return "Periodo: desde " + fechaInicio;
        }
        return "Periodo: hasta " + fechaFin;
    }
}