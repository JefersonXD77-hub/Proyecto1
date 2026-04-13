package com.horizontes.controller;

import com.horizontes.dao.ReporteDAO;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/private/reportes/*")
public class ReporteServlet extends HttpServlet {

    private final ReporteDAO reporteDAO = new ReporteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            data.put("status", "error");
            data.put("message", "Debe indicar el tipo de reporte");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
            return;
        }

        String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

        try {
            Date fechaInicio = parseDate(request.getParameter("fechaInicio"));
            Date fechaFin = parseDate(request.getParameter("fechaFin"));

            Object resultado;

            switch (cleanPath) {
                case "ventas" -> resultado = reporteDAO.reporteVentas(fechaInicio, fechaFin);
                case "cancelaciones" -> resultado = reporteDAO.reporteCancelaciones(fechaInicio, fechaFin);
                case "ganancias" -> resultado = reporteDAO.reporteGanancias(fechaInicio, fechaFin);
                case "agente-mas-ventas" -> resultado = reporteDAO.reporteAgenteMasVentas(fechaInicio, fechaFin);
                case "agente-mas-ganancias" -> resultado = reporteDAO.reporteAgenteMasGanancias(fechaInicio, fechaFin);
                case "paquete-mas-vendido" -> resultado = reporteDAO.reportePaqueteMasVendido(fechaInicio, fechaFin, false);
                case "paquete-menos-vendido" -> resultado = reporteDAO.reportePaqueteMasVendido(fechaInicio, fechaFin, true);
                case "ocupacion-destino" -> resultado = reporteDAO.reporteOcupacionPorDestino(fechaInicio, fechaFin);
                default -> {
                    data.put("status", "error");
                    data.put("message", "Reporte no soportado");
                    JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                    return;
                }
            }

            data.put("status", "ok");
            data.put("fechaInicio", fechaInicio);
            data.put("fechaFin", fechaFin);
            data.put("data", resultado);

            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (IllegalArgumentException e) {
            data.put("status", "error");
            data.put("message", "Formato de fecha invalido. Use YYYY-MM-DD");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al generar reporte");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    

    private Date parseDate(String value) {
        if (value == null || value.isBlank()) return null;
        return Date.valueOf(value);
    }
}