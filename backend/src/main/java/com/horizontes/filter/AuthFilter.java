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

        chain.doFilter(request, response);
    }
}