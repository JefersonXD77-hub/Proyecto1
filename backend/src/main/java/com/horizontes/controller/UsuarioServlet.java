package com.horizontes.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.horizontes.dao.RolDAO;
import com.horizontes.dao.UsuarioDAO;
import com.horizontes.model.Rol;
import com.horizontes.model.Usuario;
import com.horizontes.util.BCryptUtil;
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

@WebServlet("/api/private/usuarios/*")
public class UsuarioServlet extends HttpServlet {

    private final Gson gson = new Gson();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final RolDAO rolDAO = new RolDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();
        String pathInfo = request.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/") || pathInfo.isBlank()) {
                List<Usuario> usuarios = usuarioDAO.findAll();
                data.put("status", "ok");
                data.put("data", usuarios);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            String cleanPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (cleanPath.equals("roles")) {
                List<Rol> roles = rolDAO.findAll();
                data.put("status", "ok");
                data.put("data", roles);
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
                return;
            }

            int id = extractId(pathInfo);
            Usuario usuario = usuarioDAO.findById(id);

            if (usuario == null) {
                data.put("status", "error");
                data.put("message", "Usuario no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            data.put("status", "ok");
            data.put("data", usuario);
            JsonResponse.send(response, HttpServletResponse.SC_OK, data);

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al consultar usuarios");
            data.put("detail", e.getMessage());
            JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Map<String, Object> data = new HashMap<>();

        try {
            JsonObject body = gson.fromJson(request.getReader(), JsonObject.class);

            if (!isValidCreateBody(body)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            String username = body.get("username").getAsString().trim();
            String password = body.get("password").getAsString();
            String nombreCompleto = body.get("nombreCompleto").getAsString().trim();
            String correo = body.has("correo") && !body.get("correo").isJsonNull()
                    ? body.get("correo").getAsString().trim()
                    : null;
            int idRol = body.get("idRol").getAsInt();

            if (password.length() < 6) {
                data.put("status", "error");
                data.put("message", "La password debe tener al menos 6 caracteres");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Usuario existente = usuarioDAO.findByUsername(username);
            if (existente != null) {
                data.put("status", "error");
                data.put("message", "El username ya existe");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Rol rol = rolDAO.findById(idRol);
            if (rol == null) {
                data.put("status", "error");
                data.put("message", "Rol no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setUsername(username);
            usuario.setPasswordHash(BCryptUtil.hashPassword(password));
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setCorreo(correo);
            usuario.setRol(rol);
            usuario.setActivo(body.has("activo") ? body.get("activo").getAsBoolean() : true);

            boolean created = usuarioDAO.insert(usuario);

            if (created) {
                data.put("status", "ok");
                data.put("message", "Usuario creado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_CREATED, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo crear el usuario");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al crear usuario");
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

            if (!isValidUpdateBody(body)) {
                data.put("status", "error");
                data.put("message", "Datos incompletos o invalidos");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Usuario existente = usuarioDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Usuario no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            String username = body.get("username").getAsString().trim();
            String nombreCompleto = body.get("nombreCompleto").getAsString().trim();
            String correo = body.has("correo") && !body.get("correo").isJsonNull()
                    ? body.get("correo").getAsString().trim()
                    : null;
            int idRol = body.get("idRol").getAsInt();
            boolean activo = body.has("activo") ? body.get("activo").getAsBoolean() : existente.isActivo();

            Usuario otro = usuarioDAO.findByUsername(username);
            if (otro != null && otro.getIdUsuario() != id) {
                data.put("status", "error");
                data.put("message", "El username ya existe");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Rol rol = rolDAO.findById(idRol);
            if (rol == null) {
                data.put("status", "error");
                data.put("message", "Rol no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                return;
            }

            Usuario usuario = new Usuario();
            usuario.setIdUsuario(id);
            usuario.setUsername(username);
            usuario.setNombreCompleto(nombreCompleto);
            usuario.setCorreo(correo);
            usuario.setRol(rol);
            usuario.setActivo(activo);

            boolean updatePassword = body.has("password")
                    && !body.get("password").isJsonNull()
                    && !body.get("password").getAsString().isBlank();

            if (updatePassword) {
                String nuevaPassword = body.get("password").getAsString();
                if (nuevaPassword.length() < 6) {
                    data.put("status", "error");
                    data.put("message", "La password debe tener al menos 6 caracteres");
                    JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);
                    return;
                }
                usuario.setPasswordHash(BCryptUtil.hashPassword(nuevaPassword));
            }

            boolean updated = usuarioDAO.update(usuario, updatePassword);

            if (updated) {
                data.put("status", "ok");
                data.put("message", "Usuario actualizado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo actualizar el usuario");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al actualizar usuario");
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

            Usuario existente = usuarioDAO.findById(id);
            if (existente == null) {
                data.put("status", "error");
                data.put("message", "Usuario no encontrado");
                JsonResponse.send(response, HttpServletResponse.SC_NOT_FOUND, data);
                return;
            }

            boolean deleted = usuarioDAO.softDelete(id);

            if (deleted) {
                data.put("status", "ok");
                data.put("message", "Usuario desactivado correctamente");
                JsonResponse.send(response, HttpServletResponse.SC_OK, data);
            } else {
                data.put("status", "error");
                data.put("message", "No se pudo desactivar el usuario");
                JsonResponse.send(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, data);
            }

        } catch (NumberFormatException e) {
            data.put("status", "error");
            data.put("message", "ID invalido");
            JsonResponse.send(response, HttpServletResponse.SC_BAD_REQUEST, data);

        } catch (SQLException e) {
            data.put("status", "error");
            data.put("message", "Error al desactivar usuario");
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

    private boolean isValidCreateBody(JsonObject body) {
        return body != null
                && body.has("username")
                && body.has("password")
                && body.has("nombreCompleto")
                && body.has("idRol");
    }

    private boolean isValidUpdateBody(JsonObject body) {
        return body != null
                && body.has("username")
                && body.has("nombreCompleto")
                && body.has("idRol");
    }
}