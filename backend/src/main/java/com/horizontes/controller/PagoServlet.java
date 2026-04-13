package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.MetodoPagoDAO;
import com.horizontes.dao.PagoDAO;
import com.horizontes.model.MetodoPago;
import com.horizontes.model.Pago;
import com.horizontes.model.Reservacion;
import com.horizontes.util.JsonResponse;
import com.horizontes.util.PdfPagoReciboUtil;
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

@WebServlet("/api/private/pagos/*")
public class PagoServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final PagoDAO pagoDAO = new PagoDAO();
    private final MetodoPagoDAO metodoPagoDAO = new MetodoPagoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        Map<String, Object> data = new HashMap<>();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
                data.put("status", "error");
                data.put("message", "Debe indicar una ruta valida");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (cleanPath.startsWith("reservacion/")) {
                int idReservacion = Integer.parseInt(cleanPath.replace("reservacion/", ""));
                List<Pago> pagos = pagoDAO.findAllByReservacion(idReservacion);
                double totalPagado = pagoDAO.getTotalPagado(idReservacion);
                PagoDAO.ReservacionPagoInfo info = pagoDAO.findReservacionPagoInfo(idReservacion);

                if (info == null) {
                    data.put("status", "error");
                    data.put("message", "Reservacion no encontrada");
                    JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                    return;
                }

                Map<String, Object> resumen = new HashMap<>();
                resumen.put("numeroReservacion", info.numeroReservacion());
                resumen.put("costoTotal", info.costoTotal());
                resumen.put("totalPagado", totalPagado);
                resumen.put("saldoPendiente", Math.max(0, info.costoTotal() - totalPagado));
                resumen.put("estadoActual", info.estadoNombre());

                data.put("status", "ok");
                data.put("resumen", resumen);
                data.put("data", pagos);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            if (cleanPath.startsWith("comprobante/")) {
                int idReservacion = Integer.parseInt(cleanPath.replace("comprobante/", ""));

                PagoDAO.ComprobantePagoInfo info = pagoDAO.findComprobanteInfo(idReservacion);
                if (info == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Reservacion no encontrada");
                    return;
                }

                if (info.totalPagado() < info.costoTotal()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "El comprobante PDF solo esta disponible para pagos completos");
                    return;
                }

                List<Pago> pagos = pagoDAO.findAllByReservacion(idReservacion);

                PdfPagoReciboUtil.exportPaymentReceipt(
                        response,
                        "comprobante_pago_" + info.numeroReservacion() + ".pdf",
                        info,
                        pagos
                );
                return;
            }

            data.put("status", "error");
            data.put("message", "Ruta no soportada");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "Parametro invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar pagos");
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

            int idReservacion = body.get("idReservacion").getAsInt();
            int idMetodoPago = body.get("idMetodoPago").getAsInt();
            double monto = body.get("monto").getAsDouble();

            if (monto <= 0) {
                data.put("status", "error");
                data.put("message", "El monto debe ser mayor a cero");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            PagoDAO.ReservacionPagoInfo info = pagoDAO.findReservacionPagoInfo(idReservacion);
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

            if ("CANCELADA".equalsIgnoreCase(info.estadoNombre())) {
                data.put("status", "error");
                data.put("message", "No se puede registrar pago sobre una reservacion cancelada");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            MetodoPago metodoPago = metodoPagoDAO.findById(idMetodoPago);
            if (metodoPago == null) {
                data.put("status", "error");
                data.put("message", "Metodo de pago no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Pago pago = new Pago();

            Reservacion reservacion = new Reservacion();
            reservacion.setIdReservacion(idReservacion);

            pago.setReservacion(reservacion);
            pago.setMetodoPago(metodoPago);
            pago.setMonto(monto);
            pago.setFechaPago(body.has("fechaPago")
                    ? Date.valueOf(body.get("fechaPago").getAsString())
                    : new Date(System.currentTimeMillis()));

            PagoDAO.PagoRegistroResultado resultado = pagoDAO.registrarPago(pago);

            if (resultado == null) {
                data.put("status", "error");
                data.put("message", "No se pudo registrar el pago");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
                return;
            }

            boolean pagoCompleto = resultado.saldoPendiente() == 0;

            data.put("status", "ok");
            data.put("message", "Pago registrado correctamente");
            data.put("numeroReservacion", resultado.numeroReservacion());
            data.put("costoTotal", resultado.costoTotal());
            data.put("totalPagado", resultado.totalPagado());
            data.put("saldoPendiente", resultado.saldoPendiente());
            data.put("estadoActual", resultado.estadoActual());
            data.put("pagoCompleto", pagoCompleto);
            data.put("comprobanteDisponible", pagoCompleto);
            data.put("comprobanteUrl", pagoCompleto
                    ? request.getContextPath() + "/api/private/pagos/comprobante/" + resultado.idReservacion()
                    : null);

            JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al registrar pago");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);

        } catch (Exception e) {
            data.put("status", "error");
            data.put("message", "JSON invalido");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
        }
    }

    private boolean isValidBody(JsonObject body) {
        return body != null
                && body.has("idReservacion")
                && body.has("idMetodoPago")
                && body.has("monto");
    }
}