package com.horizontes.controller;

import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/private/test")
public class ProtectedTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        HttpSession session = request.getSession(false);

        data.put("status", "ok");
        data.put("message", "Ruta protegida accesible");
        data.put("userId", session.getAttribute("userId"));
        data.put("username", session.getAttribute("username"));
        data.put("rol", session.getAttribute("rol"));

        JsonResponse.send(response, HttpServletResponse.SC_OK, data);
    }
}