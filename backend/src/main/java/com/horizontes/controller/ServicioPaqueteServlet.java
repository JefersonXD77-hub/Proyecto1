package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.ServicioPaqueteDAO;
import com.horizontes.model.PaqueteTuristico;
import com.horizontes.model.Proveedor;
import com.horizontes.model.ServicioPaquete;
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

@WebServlet("/api/private/servicios-paquete/*")
public class ServicioPaqueteServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ServicioPaqueteDAO servicioDAO = new ServicioPaqueteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                data.put("status", "error");
                data.put("message", "Debe indicar una ruta valida");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (cleanPath.startsWith("paquete/")) {
                int idPaquete = Integer.parseInt(cleanPath.replace("paquete/", ""));
                List<ServicioPaquete> servicios = servicioDAO.findAllByPaquete(idPaquete);

                data.put("status", "ok");
                data.put("data", servicios);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            if (cleanPath.startsWith("resumen/")) {
                int idPaquete = Integer.parseInt(cleanPath.replace("resumen/", ""));
                ServicioPaqueteDAO.ResumenCostosDTO resumen = servicioDAO.getResumenCostos(idPaquete);

                if (resumen == null) {
                    data.put("status", "error");
                    data.put("message", "Paquete no encontrado");
                    JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                    return;
                }

                data.put("status", "ok");
                data.put("data", resumen);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = Integer.parseInt(cleanPath);
            ServicioPaquete servicio = servicioDAO.findById(id);

            if (servicio == null) {
                data.put("status", "error");
                data.put("message", "Servicio del paquete no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", servicio);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "Parametro invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar servicios del paquete");
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

            ServicioPaquete servicio = buildServicioFromJson(body);
            boolean created = servicioDAO.insert(servicio);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Servicio del paquete creado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear el servicio del paquete");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear servicio del paquete");
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

            ServicioPaquete existente = servicioDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Servicio del paquete no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            ServicioPaquete servicio = buildServicioFromJson(body);
            servicio.setIdServicioPaquete(id);

            boolean updated = servicioDAO.update(servicio);

            if (updated) {
                data.put("status", "ok");
                data.put("message", "Servicio del paquete actualizado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo actualizar el servicio del paquete");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al actualizar servicio del paquete");
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

            ServicioPaquete existente = servicioDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Servicio del paquete no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            boolean deleted = servicioDAO.delete(id);

            if (deleted) {
                data.put("status", "ok");
                data.put("message", "Servicio del paquete eliminado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo eliminar el servicio del paquete");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al eliminar servicio del paquete");
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
                && body.has("idPaquete")
                && body.has("idProveedor")
                && body.has("descripcion")
                && body.has("costo");
    }

    private ServicioPaquete buildServicioFromJson(JsonObject body) {
        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setIdPaquete(body.get("idPaquete").getAsInt());

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(body.get("idProveedor").getAsInt());

        ServicioPaquete servicio = new ServicioPaquete();
        servicio.setPaquete(paquete);
        servicio.setProveedor(proveedor);
        servicio.setDescripcion(body.get("descripcion").getAsString().trim());
        servicio.setCosto(body.get("costo").getAsDouble());

        return servicio;
    }
}