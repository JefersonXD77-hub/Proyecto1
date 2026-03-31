package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.DestinoDAO;
import com.horizontes.model.Destino;
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

@WebServlet("/api/private/destinos/*")
public class DestinoServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final DestinoDAO destinoDAO = new DestinoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Destino> destinos = destinoDAO.findAll();
                data.put("status", "ok");
                data.put("data", destinos);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = extractId(pathInfo);
            Destino destino = destinoDAO.findById(id);

            if (destino == null) {
                data.put("status", "error");
                data.put("message", "Destino no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", destino);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar destinos");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (!isValidDestinoBody(body, false)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Destino destino = buildDestinoFromJson(body);
            destino.setActivo(true);

            boolean created = destinoDAO.insert(destino);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Destino creado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear el destino");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear destino");
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
            String pathInfo = request.getPathInfo();
            int id = extractId(pathInfo);

            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (!isValidDestinoBody(body, true)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Destino destinoExistente = destinoDAO.findById(id);
            if (destinoExistente == null) {
                data.put("status", "error");
                data.put("message", "Destino no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            Destino destino = buildDestinoFromJson(body);
            destino.setIdDestino(id);
            destino.setActivo(body.has("activo") ? body.get("activo").getAsBoolean() : destinoExistente.isActivo());

            boolean updated = destinoDAO.update(destino);

            if (updated) {
                data.put("status", "ok");
                data.put("message", "Destino actualizado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo actualizar el destino");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al actualizar destino");
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
            String pathInfo = request.getPathInfo();
            int id = extractId(pathInfo);

            Destino destinoExistente = destinoDAO.findById(id);
            if (destinoExistente == null) {
                data.put("status", "error");
                data.put("message", "Destino no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            boolean deleted = destinoDAO.softDelete(id);

            if (deleted) {
                data.put("status", "ok");
                data.put("message", "Destino desactivado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo desactivar el destino");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al desactivar destino");
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

    private boolean isValidDestinoBody(JsonObject body, boolean allowActivo) {
        if (body == null) return false;

        return body.has("nombre")
                && body.has("pais")
                && body.has("descripcion")
                && body.has("climaEpoca")
                && body.has("urlImagen");
    }

    private Destino buildDestinoFromJson(JsonObject body) {
        Destino destino = new Destino();
        destino.setNombre(body.get("nombre").getAsString().trim());
        destino.setPais(body.get("pais").getAsString().trim());
        destino.setDescripcion(body.get("descripcion").getAsString().trim());
        destino.setClimaEpoca(body.get("climaEpoca").getAsString().trim());
        destino.setUrlImagen(body.get("urlImagen").getAsString().trim());
        return destino;
    }
}
