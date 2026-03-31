package com.horizontes.util;

import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public class JsonResponse {

    private static final Gson gson = new Gson();

    private JsonResponse() {
    }

    public static void send(HttpServletResponse response, int status, Map<String, Object> data) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(gson.toJson(data));
    }
}