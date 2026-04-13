package com.horizontes.filter;

import com.horizontes.util.JsonResponse;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebFilter("/api/private/*")
public class AuthFilter extends HttpFilter implements Filter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("userId") == null) {
            Map<String, Object> data = new HashMap<>();
            data.put("status", "error");
            data.put("message", "Acceso no autorizado. Debe iniciar sesion.");
            JsonResponse.send(response, HttpServletResponse.SC_UNAUTHORIZED, data);
            return;
        }

        String rol = (String) session.getAttribute("rol");
        if (rol == null || rol.isBlank()) {
            Map<String, Object> data = new HashMap<>();
            data.put("status", "error");
            data.put("message", "Sesion invalida. Rol no disponible.");
            JsonResponse.send(response, HttpServletResponse.SC_FORBIDDEN, data);
            return;
        }

        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());
        String method = request.getMethod();

        if (hasAccess(rol, path, method)) {
            chain.doFilter(request, response);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("status", "error");
        data.put("message", "No tiene permisos para acceder a este recurso.");
        data.put("rol", rol);
        JsonResponse.send(response, HttpServletResponse.SC_FORBIDDEN, data);
    }

    private boolean hasAccess(String rol, String path, String method) {
        if ("ADMINISTRADOR".equalsIgnoreCase(rol)) {
            return true;
        }

        if ("ATENCION_CLIENTE".equalsIgnoreCase(rol)) {
            return isAtencionClientePath(path, method);
        }

        if ("OPERACIONES".equalsIgnoreCase(rol)) {
            return isOperacionesPath(path);
        }

        return false;
    }

    private boolean isAtencionClientePath(String path, String method) {
        if (path.startsWith("/api/private/clientes")
                || path.startsWith("/api/private/reservaciones")
                || path.startsWith("/api/private/pagos")
                || path.startsWith("/api/private/cancelaciones")
                || path.startsWith("/api/private/metodos-pago")) {
            return true;
        }

        // Permitir solo lectura de paquetes para crear reservaciones
        if (path.startsWith("/api/private/paquetes") && "GET".equalsIgnoreCase(method)) {
            return true;
        }

        return false;
    }

    private boolean isOperacionesPath(String path) {
        return path.startsWith("/api/private/destinos")
                || path.startsWith("/api/private/proveedores")
                || path.startsWith("/api/private/tipos-proveedor")
                || path.startsWith("/api/private/paquetes")
                || path.startsWith("/api/private/servicios-paquete");
    }
}