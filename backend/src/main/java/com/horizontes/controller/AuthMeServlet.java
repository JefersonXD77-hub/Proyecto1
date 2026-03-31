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

@WebServlet("/api/auth/me")
public class AuthMeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            data.put("status", "error");
            data.put("message", "No hay sesion activa");
            JsonResponse.send(response, HttpServletResponse.SC_UNAUTHORIZED, data);
            return;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", session.getAttribute("userId"));
        userData.put("username", session.getAttribute("username"));
        userData.put("nombreCompleto", session.getAttribute("nombreCompleto"));
        userData.put("rol", session.getAttribute("rol"));

        data.put("status", "ok");
        data.put("user", userData);

        JsonResponse.send(response, HttpServletResponse.SC_OK, data);
    }
}
