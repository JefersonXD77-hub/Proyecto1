package com.horizontes.controller;

import com.horizontes.dao.TipoProveedorDAO;
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

@WebServlet("/api/private/tipos-proveedor")
public class TipoProveedorServlet extends HttpServlet {

    private final TipoProveedorDAO tipoProveedorDAO = new TipoProveedorDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            List<TipoProveedor> tipos = tipoProveedorDAO.findAll();
            data.put("status", "ok");
            data.put("data", tipos);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar tipos de proveedor");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }
}
