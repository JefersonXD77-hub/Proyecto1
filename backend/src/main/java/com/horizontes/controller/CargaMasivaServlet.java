package com.horizontes.controller;

import com.horizontes.service.CargaMasivaService;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/private/carga-masiva")
public class CargaMasivaServlet extends HttpServlet {

    private final CargaMasivaService cargaMasivaService = new CargaMasivaService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            String contenido = request.getReader().lines().reduce("", (a, b) -> a + b + "\n");

            if (contenido == null || contenido.isBlank()) {
                data.put("status", "error");
                data.put("message", "Debe enviar el contenido del archivo de carga");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            CargaMasivaService.ResultadoCarga resultado = cargaMasivaService.procesarContenido(contenido);

            data.put("status", "ok");
            data.put("message", "Carga procesada");
            data.put("resumen", resultado.getResumen());
            data.put("errores", resultado.getErrores());

            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (Exception e) {
            data.put("status", "error");
            data.put("message", "Error al procesar carga masiva");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }
}