package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.ProveedorDAO;
import com.horizontes.model.Proveedor;
import com.horizontes.model.TipoProveedor;
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

@WebServlet("/api/private/proveedores/*")
public class ProveedorServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ProveedorDAO proveedorDAO = new ProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Proveedor> proveedores = proveedorDAO.findAll();
                data.put("status", "ok");
                data.put("data", proveedores);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = extractId(pathInfo);
            Proveedor proveedor = proveedorDAO.findById(id);

            if (proveedor == null) {
                data.put("status", "error");
                data.put("message", "Proveedor no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", proveedor);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar proveedores");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (!isValidProveedorBody(body)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Proveedor proveedor = buildProveedorFromJson(body);
            proveedor.setActivo(true);

            boolean created = proveedorDAO.insert(proveedor);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Proveedor creado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear el proveedor");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear proveedor");
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

            if (!isValidProveedorBody(body)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Proveedor existente = proveedorDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Proveedor no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            Proveedor proveedor = buildProveedorFromJson(body);
            proveedor.setIdProveedor(id);
            proveedor.setActivo(body.has("activo") ? body.get("activo").getAsBoolean() : existente.isActivo());

            boolean updated = proveedorDAO.update(proveedor);

            if (updated) {
                data.put("status", "ok");
                data.put("message", "Proveedor actualizado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo actualizar el proveedor");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al actualizar proveedor");
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

            Proveedor existente = proveedorDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Proveedor no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            boolean deleted = proveedorDAO.softDelete(id);

            if (deleted) {
                data.put("status", "ok");
                data.put("message", "Proveedor desactivado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo desactivar el proveedor");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al desactivar proveedor");
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

    private boolean isValidProveedorBody(JsonObject body) {
        return body != null
                && body.has("nombre")
                && body.has("idTipoProveedor")
                && body.has("paisOperacion")
                && body.has("contacto");
    }

    private Proveedor buildProveedorFromJson(JsonObject body) {
        TipoProveedor tipo = new TipoProveedor();
        tipo.setIdTipoProveedor(body.get("idTipoProveedor").getAsInt());

        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(body.get("nombre").getAsString().trim());
        proveedor.setTipoProveedor(tipo);
        proveedor.setPaisOperacion(body.get("paisOperacion").getAsString().trim());
        proveedor.setContacto(body.get("contacto").getAsString().trim());

        return proveedor;
    }
}
