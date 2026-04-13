package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.PaqueteTuristicoDAO;
import com.horizontes.model.Destino;
import com.horizontes.model.PaqueteTuristico;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/private/paquetes/*")
public class PaqueteTuristicoServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final PaqueteTuristicoDAO paqueteDAO = new PaqueteTuristicoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
                List<PaqueteTuristico> paquetes = paqueteDAO.findAll();
                data.put("status", "ok");
                data.put("data", paquetes);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (cleanPath.equals("alta-demanda")) {
                List<Map<String, Object>> alertas = paqueteDAO.findPaquetesAltaDemanda();
                data.put("status", "ok");
                data.put("data", alertas);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = extractId(pathInfo);
            PaqueteTuristico paquete = paqueteDAO.findById(id);

            if (paquete == null) {
                data.put("status", "error");
                data.put("message", "Paquete no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", paquete);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar paquetes");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (!isValidBody(body)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            PaqueteTuristico paquete = buildPaqueteFromJson(body);
            paquete.setActivo(true);

            boolean created = paqueteDAO.insert(paquete);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Paquete creado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear el paquete");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear paquete");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);

        } catch (Exception e) {
            data.put("status", "error");
            data.put("message", "JSON invalido");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            int id = extractId(request.getPathInfo());
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (!isValidBody(body)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            PaqueteTuristico existente = paqueteDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Paquete no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            PaqueteTuristico paquete = buildPaqueteFromJson(body);
            paquete.setIdPaquete(id);
            paquete.setActivo(body.has("activo") ? body.get("activo").getAsBoolean() : existente.isActivo());

            boolean updated = paqueteDAO.update(paquete);

            if (updated) {
                data.put("status", "ok");
                data.put("message", "Paquete actualizado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo actualizar el paquete");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al actualizar paquete");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);

        } catch (Exception e) {
            data.put("status", "error");
            data.put("message", "JSON invalido");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            int id = extractId(request.getPathInfo());

            PaqueteTuristico existente = paqueteDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Paquete no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            boolean deleted = paqueteDAO.softDelete(id);

            if (deleted) {
                data.put("status", "ok");
                data.put("message", "Paquete desactivado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo desactivar el paquete");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al desactivar paquete");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    private int extractId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
            throw new NumberFormatException("ID no proporcionado");
        }
        String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return Integer.parseInt(cleanPath);
    }

    private boolean isValidBody(JsonObject body) {
        return body != null
                && body.has("nombre")
                && body.has("idDestino")
                && body.has("duracionDias")
                && body.has("descripcion")
                && body.has("precioVenta")
                && body.has("capacidadMaxima");
    }

    private PaqueteTuristico buildPaqueteFromJson(JsonObject body) {
        Destino destino = new Destino();
        destino.setIdDestino(body.get("idDestino").getAsInt());

        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setNombre(body.get("nombre").getAsString().trim());
        paquete.setDestino(destino);
        paquete.setDuracionDias(body.get("duracionDias").getAsInt());
        paquete.setDescripcion(body.get("descripcion").getAsString().trim());
        paquete.setPrecioVenta(body.get("precioVenta").getAsDouble());
        paquete.setCapacidadMaxima(body.get("capacidadMaxima").getAsInt());

        return paquete;
    }
}