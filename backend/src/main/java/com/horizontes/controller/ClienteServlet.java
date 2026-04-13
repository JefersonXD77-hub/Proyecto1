package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.ClienteDAO;
import com.horizontes.model.Cliente;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/private/clientes/*")
public class ClienteServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                List<Cliente> clientes = clienteDAO.findAll();
                data.put("status", "ok");
                data.put("data", clientes);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (cleanPath.startsWith("buscar/")) {
                String dpiPasaporte = cleanPath.replace("buscar/", "").trim();
                Cliente cliente = clienteDAO.findByDpiPasaporte(dpiPasaporte);

                if (cliente == null) {
                    data.put("status", "error");
                    data.put("message", "Cliente no encontrado");
                    JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                    return;
                }

                data.put("status", "ok");
                data.put("data", cliente);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = Integer.parseInt(cleanPath);
            Cliente cliente = clienteDAO.findById(id);

            if (cliente == null) {
                data.put("status", "error");
                data.put("message", "Cliente no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", cliente);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "Parametro invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar clientes");
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

            Cliente cliente = buildClienteFromJson(body);
            cliente.setActivo(true);

            boolean created = clienteDAO.insert(cliente);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Cliente creado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear el cliente");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear cliente");
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

            Cliente existente = clienteDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Cliente no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            Cliente cliente = buildClienteFromJson(body);
            cliente.setIdCliente(id);
            cliente.setActivo(body.has("activo") ? body.get("activo").getAsBoolean() : existente.isActivo());

            boolean updated = clienteDAO.update(cliente);

            if (updated) {
                data.put("status", "ok");
                data.put("message", "Cliente actualizado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo actualizar el cliente");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al actualizar cliente");
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

            Cliente existente = clienteDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Cliente no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            boolean deleted = clienteDAO.softDelete(id);

            if (deleted) {
                data.put("status", "ok");
                data.put("message", "Cliente desactivado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo desactivar el cliente");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al desactivar cliente");
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
                && body.has("dpiPasaporte")
                && body.has("nombreCompleto")
                && body.has("fechaNacimiento")
                && body.has("telefono")
                && body.has("email")
                && body.has("nacionalidad");
    }

    private Cliente buildClienteFromJson(JsonObject body) {
        Cliente cliente = new Cliente();
        cliente.setDpiPasaporte(body.get("dpiPasaporte").getAsString().trim());
        cliente.setNombreCompleto(body.get("nombreCompleto").getAsString().trim());
        cliente.setFechaNacimiento(Date.valueOf(body.get("fechaNacimiento").getAsString()));
        cliente.setTelefono(body.get("telefono").getAsString().trim());
        cliente.setEmail(body.get("email").getAsString().trim());
        cliente.setNacionalidad(body.get("nacionalidad").getAsString().trim());
        return cliente;
    }
}
