package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.CancelacionDAO;
import com.horizontes.model.Cancelacion;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/private/cancelaciones/*")
public class CancelacionServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final CancelacionDAO cancelacionDAO = new CancelacionDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
                data.put("status", "error");
                data.put("message", "Debe indicar el id de la reservacion");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            int idReservacion = extractId(pathInfo);
            Cancelacion cancelacion = cancelacionDAO.findByReservacionId(idReservacion);

            if (cancelacion == null) {
                data.put("status", "error");
                data.put("message", "Cancelacion no encontrada para esa reservacion");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", cancelacion);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar cancelacion");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (body == null || !body.has("idReservacion")) {
                data.put("status", "error");
                data.put("message", "Debe enviar idReservacion");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            int idReservacion = body.get("idReservacion").getAsInt();
            CancelacionDAO.ReservacionCancelacionInfo info = cancelacionDAO.findReservacionInfo(idReservacion);

            if (info == null) {
                data.put("status", "error");
                data.put("message", "Reservacion no encontrada");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            if (!info.activo()) {
                data.put("status", "error");
                data.put("message", "La reservacion esta inactiva");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            if (!"PENDIENTE".equalsIgnoreCase(info.estadoNombre()) &&
                !"CONFIRMADA".equalsIgnoreCase(info.estadoNombre())) {
                data.put("status", "error");
                data.put("message", "Solo se puede cancelar una reservacion pendiente o confirmada");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            if (cancelacionDAO.findByReservacionId(idReservacion) != null) {
                data.put("status", "error");
                data.put("message", "La reservacion ya fue cancelada anteriormente");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            LocalDate hoy = LocalDate.now();
            LocalDate fechaViaje = info.fechaViaje().toLocalDate();
            long diasAnticipacion = ChronoUnit.DAYS.between(hoy, fechaViaje);

            if (diasAnticipacion < 7) {
                data.put("status", "error");
                data.put("message", "No se permite cancelar con menos de 7 dias de anticipacion");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            double montoPagado = cancelacionDAO.getMontoPagado(idReservacion);
            double porcentajeReembolso = calcularPorcentajeReembolso((int) diasAnticipacion);
            double montoReembolsado = montoPagado * (porcentajeReembolso / 100.0);
            double perdidaAgencia = montoPagado - montoReembolsado;

            HttpSession session = request.getSession(false);
            Integer userId = (Integer) session.getAttribute("userId");

            Reservacion reservacion = new Reservacion();
            reservacion.setIdReservacion(idReservacion);
            reservacion.setNumeroReservacion(info.numeroReservacion());

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(userId);

            Cancelacion cancelacion = new Cancelacion();
            cancelacion.setReservacion(reservacion);
            cancelacion.setFechaCancelacion(new Date(System.currentTimeMillis()));
            cancelacion.setDiasAnticipacion((int) diasAnticipacion);
            cancelacion.setPorcentajeReembolso(porcentajeReembolso);
            cancelacion.setMontoPagado(montoPagado);
            cancelacion.setMontoReembolsado(montoReembolsado);
            cancelacion.setPerdidaAgencia(perdidaAgencia);
            cancelacion.setUsuarioProceso(usuario);

            CancelacionDAO.CancelacionResultado resultado = cancelacionDAO.procesarCancelacion(cancelacion);

            data.put("status", "ok");
            data.put("message", "Cancelacion procesada correctamente");
            data.put("numeroReservacion", resultado.numeroReservacion());
            data.put("diasAnticipacion", resultado.diasAnticipacion());
            data.put("porcentajeReembolso", resultado.porcentajeReembolso());
            data.put("montoPagado", resultado.montoPagado());
            data.put("montoReembolsado", resultado.montoReembolsado());
            data.put("perdidaAgencia", resultado.perdidaAgencia());
            data.put("estadoActual", resultado.estadoActual());

            JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al procesar cancelacion");
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
        String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        return Integer.parseInt(cleanPath);
    }

    private double calcularPorcentajeReembolso(int diasAnticipacion) {
        if (diasAnticipacion > 30) {
            return 100.0;
        }
        if (diasAnticipacion >= 15) {
            return 70.0;
        }
        return 40.0;
    }
}
