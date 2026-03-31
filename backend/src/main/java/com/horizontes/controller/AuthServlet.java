package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.UsuarioDAO;
import com.horizontes.model.Usuario;
import com.horizontes.util.BCryptUtil;
import com.horizontes.util.JsonResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();

        if (pathInfo == null) {
            sendNotFound(response);
            return;
        }

        switch (pathInfo) {
            case "/login" -> handleLogin(request, response);
            case "/logout" -> handleLogout(request, response);
            default -> sendNotFound(response);
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (body == null || !body.has("username") || !body.has("password")) {
                data.put("status", "error");
                data.put("message", "Username y password son obligatorios");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            String username = body.get("username").getAsString().trim();
            String password = body.get("password").getAsString();

            Usuario usuario = usuarioDAO.findByUsername(username);

            if (usuario == null) {
                data.put("status", "error");
                data.put("message", "Credenciales invalidas");
                JsonResponse.send(response, HttpServletResponse.SC_UNAUTHORIZED, data);
                return;
            }

            if (!usuario.isActivo()) {
                data.put("status", "error");
                data.put("message", "Usuario inactivo");
                JsonResponse.send(response, HttpServletResponse.SC_FORBIDDEN, data);
                return;
            }

            boolean validPassword = BCryptUtil.verifyPassword(password, usuario.getPasswordHash());

            if (!validPassword) {
                data.put("status", "error");
                data.put("message", "Credenciales invalidas");
                JsonResponse.send(response, HttpServletResponse.SC_UNAUTHORIZED, data);
                return;
            }

            HttpSession session = request.getSession(true);
            session.setAttribute("userId", usuario.getIdUsuario());
            session.setAttribute("username", usuario.getUsername());
            session.setAttribute("nombreCompleto", usuario.getNombreCompleto());
            session.setAttribute("rol", usuario.getRol().getNombre());

            Map<String, Object> userData = new HashMap<>();
            userData.put("id", usuario.getIdUsuario());
            userData.put("username", usuario.getUsername());
            userData.put("nombreCompleto", usuario.getNombreCompleto());
            userData.put("rol", usuario.getRol().getNombre());

            data.put("status", "ok");
            data.put("message", "Login exitoso");
            data.put("user", userData);

            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar usuario");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);

        } catch (Exception e) {
            data.put("status", "error");
            data.put("message", "Error al procesar login");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        data.put("status", "ok");
        data.put("message", "Logout exitoso");
        JsonResponse.send(response, HttpServletResponse.SC_OK, data);
    }

    private void sendNotFound(HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();
        data.put("status", "error");
        data.put("message", "Ruta no encontrada");
        JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
    }
}