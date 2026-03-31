package com.horizontes.controller;

import com.horizontes.util.DatabaseConnection;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/test-db")
public class TestDbServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try (Connection connection = DatabaseConnection.getConnection()) {
            data.put("status", "ok");
            data.put("message", "Conexion a base de datos exitosa");
            data.put("database", connection.getCatalog());

            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "No se pudo conectar a la base de datos");
            data.put("detail", e.getMessage());

            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }
}
