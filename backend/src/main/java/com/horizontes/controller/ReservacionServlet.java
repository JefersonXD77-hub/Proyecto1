package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.horizontes.dao.ClienteDAO;
import com.horizontes.dao.ReservacionDAO;
import com.horizontes.model.Cliente;
import com.horizontes.model.PaqueteTuristico;
import com.horizontes.model.Reservacion;
import com.horizontes.model.Usuario;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/api/private/reservaciones/*")
public class ReservacionServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final ReservacionDAO reservacionDAO = new ReservacionDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
                List<Reservacion> reservaciones = reservacionDAO.findAll();
                data.put("status", "ok");
                data.put("data", reservaciones);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (cleanPath.equals("hoy")) {
                List<Reservacion> reservaciones = reservacionDAO.findReservacionesDelDia();
                data.put("status", "ok");
                data.put("data", reservaciones);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            if (cleanPath.equals("disponibles")) {
                String fechaViajeStr = request.getParameter("fechaViaje");
                String idDestinoStr = request.getParameter("idDestino");

                if (fechaViajeStr == null || fechaViajeStr.isBlank() || idDestinoStr == null || idDestinoStr.isBlank()) {
                    data.put("status", "error");
                    data.put("message", "Debe enviar fechaViaje e idDestino");
                    JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                    return;
                }

                Date fechaViaje = Date.valueOf(fechaViajeStr);
                int idDestino = Integer.parseInt(idDestinoStr);

                List<Map<String, Object>> disponibles = reservacionDAO.findDisponiblesByFechaDestino(fechaViaje, idDestino);

                data.put("status", "ok");
                data.put("fechaViaje", fechaViaje);
                data.put("idDestino", idDestino);
                data.put("data", disponibles);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            if (cleanPath.startsWith("cliente/")) {
                int idCliente = Integer.parseInt(cleanPath.replace("cliente/", ""));

                Cliente cliente = clienteDAO.findById(idCliente);
                if (cliente == null) {
                    data.put("status", "error");
                    data.put("message", "Cliente no encontrado");
                    JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                    return;
                }

                List<Reservacion> reservaciones = reservacionDAO.findByCliente(idCliente);

                data.put("status", "ok");
                data.put("cliente", cliente);
                data.put("data", reservaciones);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = extractId(pathInfo);
            Reservacion reservacion = reservacionDAO.findById(id);

            if (reservacion == null) {
                data.put("status", "error");
                data.put("message", "Reservacion no encontrada");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", reservacion);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "Parametro invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (IllegalArgumentException e) {
            data.put("status", "error");
            data.put("message", "Formato de fecha invalido. Use YYYY-MM-DD");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar reservaciones");
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

            int idPaquete = body.get("idPaquete").getAsInt();
            JsonArray pasajerosJson = body.getAsJsonArray("pasajeros");

            if (pasajerosJson == null || pasajerosJson.size() == 0) {
                data.put("status", "error");
                data.put("message", "Debe enviar al menos un pasajero");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            PaqueteTuristico paquete = reservacionDAO.findPaqueteBasicoById(idPaquete);
            if (paquete == null || !paquete.isActivo()) {
                data.put("status", "error");
                data.put("message", "Paquete no encontrado o inactivo");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            List<Cliente> pasajeros = new ArrayList<>();
            for (int i = 0; i < pasajerosJson.size(); i++) {
                int idCliente = pasajerosJson.get(i).getAsInt();
                Cliente cliente = clienteDAO.findById(idCliente);

                if (cliente == null || !cliente.isActivo()) {
                    data.put("status", "error");
                    data.put("message", "Uno o mas pasajeros no existen o estan inactivos");
                    JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                    return;
                }

                pasajeros.add(cliente);
            }

            int cantidadPasajeros = pasajeros.size();
            Date fechaViaje = Date.valueOf(body.get("fechaViaje").getAsString());

            int cuposOcupados = reservacionDAO.getCuposOcupados(idPaquete, fechaViaje);
            int cuposDisponibles = paquete.getCapacidadMaxima() - cuposOcupados;

            if (cantidadPasajeros > cuposDisponibles) {
                data.put("status", "error");
                data.put("message", "No hay cupo suficiente para la fecha seleccionada");
                data.put("capacidadMaxima", paquete.getCapacidadMaxima());
                data.put("cuposOcupados", cuposOcupados);
                data.put("cuposDisponibles", Math.max(0, cuposDisponibles));
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            HttpSession session = request.getSession(false);
            Integer userId = (Integer) session.getAttribute("userId");

            Usuario agente = new Usuario();
            agente.setIdUsuario(userId);

            Reservacion reservacion = new Reservacion();
            reservacion.setNumeroReservacion(generateNumeroReservacion());
            reservacion.setFechaViaje(fechaViaje);
            reservacion.setPaquete(paquete);
            reservacion.setAgente(agente);
            reservacion.setCantidadPasajeros(cantidadPasajeros);
            reservacion.setCostoTotal(paquete.getPrecioVenta() * cantidadPasajeros);
            reservacion.setEstado("PENDIENTE");
            reservacion.setActivo(true);
            reservacion.setPasajeros(pasajeros);

            boolean created = reservacionDAO.createReservacion(reservacion);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Reservacion creada correctamente");
                data.put("numeroReservacion", reservacion.getNumeroReservacion());
                data.put("costoTotal", reservacion.getCostoTotal());
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear la reservacion por falta de cupo o error interno");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear reservacion");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);

        } catch (Exception e) {
            data.put("status", "error");
            data.put("message", "JSON invalido");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
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
                && body.has("fechaViaje")
                && body.has("pasajeros");
    }

    private String generateNumeroReservacion() {
        return "RES-" + System.currentTimeMillis();
    }
}