package com.horizontes.service;

import com.horizontes.util.BCryptUtil;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CargaMasivaService {

    private static final DateTimeFormatter INPUT_DATE =
            DateTimeFormatter.ofPattern("dd/MM/uuuu", Locale.ROOT).withResolverStyle(ResolverStyle.STRICT);

    public ResultadoCarga procesarContenido(String contenido) {
        ResultadoCarga resultado = new ResultadoCarga();

        String[] lineas = contenido.split("\\R");
        int numeroLinea = 0;

        for (String lineaOriginal : lineas) {
            numeroLinea++;
            String linea = lineaOriginal != null ? lineaOriginal.trim() : "";

            if (linea.isBlank()) {
                continue;
            }

            resultado.totalLineas++;

            try (Connection connection = DatabaseConnection.getConnection()) {
                connection.setAutoCommit(false);

                procesarLinea(connection, linea, numeroLinea, resultado);

                connection.commit();
                resultado.lineasExitosas++;

            } catch (Exception e) {
                resultado.lineasConError++;
                resultado.errores.add(buildError(numeroLinea, linea, e.getMessage()));
            }
        }

        return resultado;
    }

    private void procesarLinea(Connection connection, String linea, int numeroLinea, ResultadoCarga resultado) throws Exception {
        int idx = linea.indexOf('(');
        int last = linea.lastIndexOf(')');

        if (idx <= 0 || last <= idx) {
            throw new IllegalArgumentException("Formato invalido. Se esperaba INSTRUCCION(...)");
        }

        String instruccion = linea.substring(0, idx).trim().toUpperCase(Locale.ROOT);
        String parametros = linea.substring(idx + 1, last).trim();

        List<String> args = splitArguments(parametros);

        switch (instruccion) {
            case "USUARIO" -> procesarUsuario(connection, args, resultado);
            case "DESTINO" -> procesarDestino(connection, args, resultado);
            case "PROVEEDOR" -> procesarProveedor(connection, args, resultado);
            case "PAQUETE" -> procesarPaquete(connection, args, resultado);
            case "SERVICIO_PAQUETE" -> procesarServicioPaquete(connection, args, resultado);
            case "CLIENTE" -> procesarCliente(connection, args, resultado);
            case "RESERVACION" -> procesarReservacion(connection, args, resultado);
            case "PAGO" -> procesarPago(connection, args, resultado);
            default -> throw new IllegalArgumentException("Instruccion no soportada: " + instruccion);
        }
    }

    private void procesarUsuario(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 3) {
            throw new IllegalArgumentException("USUARIO requiere 3 parametros");
        }

        String username = unquote(args.get(0));
        String password = unquote(args.get(1));
        int tipo = parseInt(args.get(2), "TIPO invalido");

        if (username.isBlank()) throw new IllegalArgumentException("Username vacio");
        if (password.length() < 6) throw new IllegalArgumentException("La password debe tener al menos 6 caracteres");

        int idRol = switch (tipo) {
            case 1 -> 2; // ATENCION_CLIENTE
            case 2 -> 3; // OPERACIONES
            case 3 -> 1; // ADMINISTRADOR
            default -> throw new IllegalArgumentException("TIPO de usuario invalido");
        };

        if (existsByString(connection, "SELECT 1 FROM usuario WHERE username = ?", username)) {
            throw new IllegalArgumentException("Username duplicado: " + username);
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO usuario (username, password_hash, nombre_completo, correo, id_rol, activo)
                VALUES (?, ?, ?, ?, ?, TRUE)
                """)) {
            st.setString(1, username);
            st.setString(2, BCryptUtil.hashPassword(password));
            st.setString(3, username);
            st.setString(4, null);
            st.setInt(5, idRol);
            st.executeUpdate();
        }

        resultado.incrementar("usuarios");
    }

    private void procesarDestino(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 3) {
            throw new IllegalArgumentException("DESTINO requiere 3 parametros");
        }

        String nombre = unquote(args.get(0));
        String pais = unquote(args.get(1));
        String descripcion = unquote(args.get(2));

        if (existsByString(connection, "SELECT 1 FROM destino WHERE nombre = ?", nombre)) {
            throw new IllegalArgumentException("Destino duplicado: " + nombre);
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO destino (nombre, pais, descripcion, clima_epoca, url_imagen, activo)
                VALUES (?, ?, ?, '', NULL, TRUE)
                """)) {
            st.setString(1, nombre);
            st.setString(2, pais);
            st.setString(3, descripcion);
            st.executeUpdate();
        }

        resultado.incrementar("destinos");
    }

    private void procesarProveedor(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 3) {
            throw new IllegalArgumentException("PROVEEDOR requiere 3 parametros");
        }

        String nombre = unquote(args.get(0));
        int tipo = parseInt(args.get(1), "TIPO de proveedor invalido");
        String pais = unquote(args.get(2));

        int idTipoProveedor = switch (tipo) {
            case 1, 2, 3, 4, 5 -> tipo;
            default -> throw new IllegalArgumentException("TIPO de proveedor invalido");
        };

        if (existsByString(connection, "SELECT 1 FROM proveedor WHERE nombre = ?", nombre)) {
            throw new IllegalArgumentException("Proveedor duplicado: " + nombre);
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO proveedor (nombre, id_tipo_proveedor, pais_operacion, contacto, activo)
                VALUES (?, ?, ?, ?, TRUE)
                """)) {
            st.setString(1, nombre);
            st.setInt(2, idTipoProveedor);
            st.setString(3, pais);
            st.setString(4, "");
            st.executeUpdate();
        }

        resultado.incrementar("proveedores");
    }

    private void procesarPaquete(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 5) {
            throw new IllegalArgumentException("PAQUETE requiere 5 parametros");
        }

        String nombre = unquote(args.get(0));
        String nombreDestino = unquote(args.get(1));
        int duracion = parseInt(args.get(2), "DURACION invalida");
        double precio = parseDouble(args.get(3), "PRECIO invalido");
        int capacidad = parseInt(args.get(4), "CAPACIDAD invalida");

        if (duracion <= 0) throw new IllegalArgumentException("DURACION debe ser mayor a 0");
        if (precio < 0) throw new IllegalArgumentException("PRECIO invalido");
        if (capacidad <= 0) throw new IllegalArgumentException("CAPACIDAD debe ser mayor a 0");

        Integer idDestino = findIdByString(connection, "SELECT id_destino FROM destino WHERE nombre = ?", nombreDestino);
        if (idDestino == null) {
            throw new IllegalArgumentException("Destino no existe: " + nombreDestino);
        }

        if (existsByString(connection, "SELECT 1 FROM paquete_turistico WHERE nombre = ?", nombre)) {
            throw new IllegalArgumentException("Paquete duplicado: " + nombre);
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO paquete_turistico
                (nombre, id_destino, duracion_dias, descripcion, precio_venta, capacidad_maxima, activo)
                VALUES (?, ?, ?, ?, ?, ?, TRUE)
                """)) {
            st.setString(1, nombre);
            st.setInt(2, idDestino);
            st.setInt(3, duracion);
            st.setString(4, "");
            st.setDouble(5, precio);
            st.setInt(6, capacidad);
            st.executeUpdate();
        }

        resultado.incrementar("paquetes");
    }

    private void procesarServicioPaquete(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 4) {
            throw new IllegalArgumentException("SERVICIO_PAQUETE requiere 4 parametros");
        }

        String nombrePaquete = unquote(args.get(0));
        String nombreProveedor = unquote(args.get(1));
        String descripcion = unquote(args.get(2));
        double costo = parseDouble(args.get(3), "COSTO invalido");

        if (costo < 0) throw new IllegalArgumentException("COSTO invalido");

        Integer idPaquete = findIdByString(connection, "SELECT id_paquete FROM paquete_turistico WHERE nombre = ?", nombrePaquete);
        if (idPaquete == null) {
            throw new IllegalArgumentException("Paquete no existe: " + nombrePaquete);
        }

        Integer idProveedor = findIdByString(connection, "SELECT id_proveedor FROM proveedor WHERE nombre = ?", nombreProveedor);
        if (idProveedor == null) {
            throw new IllegalArgumentException("Proveedor no existe: " + nombreProveedor);
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO servicio_paquete (id_paquete, id_proveedor, descripcion, costo)
                VALUES (?, ?, ?, ?)
                """)) {
            st.setInt(1, idPaquete);
            st.setInt(2, idProveedor);
            st.setString(3, descripcion);
            st.setDouble(4, costo);
            st.executeUpdate();
        }

        resultado.incrementar("serviciosPaquete");
    }

    private void procesarCliente(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 6) {
            throw new IllegalArgumentException("CLIENTE requiere 6 parametros");
        }

        String dpi = unquote(args.get(0));
        String nombre = unquote(args.get(1));
        Date fechaNacimiento = parseInputDate(unquote(args.get(2)));
        String telefono = unquote(args.get(3));
        String email = unquote(args.get(4));
        String nacionalidad = unquote(args.get(5));

        if (existsByString(connection, "SELECT 1 FROM cliente WHERE dpi_pasaporte = ?", dpi)) {
            throw new IllegalArgumentException("Cliente duplicado por DPI/Pasaporte: " + dpi);
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO cliente (dpi_pasaporte, nombre_completo, fecha_nacimiento, telefono, email, nacionalidad, activo)
                VALUES (?, ?, ?, ?, ?, ?, TRUE)
                """)) {
            st.setString(1, dpi);
            st.setString(2, nombre);
            st.setDate(3, fechaNacimiento);
            st.setString(4, telefono);
            st.setString(5, email.isBlank() ? null : email);
            st.setString(6, nacionalidad);
            st.executeUpdate();
        }

        resultado.incrementar("clientes");
    }

    private void procesarReservacion(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 4) {
            throw new IllegalArgumentException("RESERVACION requiere 4 parametros");
        }

        String nombrePaquete = unquote(args.get(0));
        String username = unquoteOrRaw(args.get(1));
        Date fechaViaje = parseInputDate(unquote(args.get(2)));
        String pasajerosTexto = unquote(args.get(3));

        Integer idPaquete = findIdByString(connection, "SELECT id_paquete FROM paquete_turistico WHERE nombre = ?", nombrePaquete);
        if (idPaquete == null) {
            throw new IllegalArgumentException("Paquete no existe: " + nombrePaquete);
        }

        Integer idUsuario = findIdByString(connection, "SELECT id_usuario FROM usuario WHERE username = ?", username);
        if (idUsuario == null) {
            throw new IllegalArgumentException("Usuario no existe: " + username);
        }

        List<Integer> idClientes = new ArrayList<>();
        String[] dpis = pasajerosTexto.split("\\|");
        for (String dpi : dpis) {
            String dpiLimpio = dpi.trim();
            Integer idCliente = findIdByString(connection, "SELECT id_cliente FROM cliente WHERE dpi_pasaporte = ?", dpiLimpio);
            if (idCliente == null) {
                throw new IllegalArgumentException("Cliente no existe: " + dpiLimpio);
            }
            idClientes.add(idCliente);
        }

        if (idClientes.isEmpty()) {
            throw new IllegalArgumentException("La reservacion debe tener al menos un pasajero");
        }

        double precioVenta;
        int capacidadMaxima;
        try (PreparedStatement st = connection.prepareStatement("""
                SELECT precio_venta, capacidad_maxima
                FROM paquete_turistico
                WHERE id_paquete = ?
                """)) {
            st.setInt(1, idPaquete);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Paquete no encontrado");
                }
                precioVenta = rs.getDouble("precio_venta");
                capacidadMaxima = rs.getInt("capacidad_maxima");
            }
        }

        int cuposOcupados;
        try (PreparedStatement st = connection.prepareStatement("""
                SELECT COALESCE(SUM(r.cantidad_pasajeros), 0) AS cupos_ocupados
                FROM reservacion r
                INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
                WHERE r.id_paquete = ?
                  AND r.fecha_viaje = ?
                  AND r.activo = TRUE
                  AND er.nombre <> 'CANCELADA'
                """)) {
            st.setInt(1, idPaquete);
            st.setDate(2, fechaViaje);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                cuposOcupados = rs.getInt("cupos_ocupados");
            }
        }

        int cantidadPasajeros = idClientes.size();
        int cuposDisponibles = capacidadMaxima - cuposOcupados;
        if (cantidadPasajeros > cuposDisponibles) {
            throw new IllegalArgumentException("No hay cupo suficiente para la reservacion");
        }

        String numeroReservacion = generateNumeroReservacion();

        int idReservacion;
        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO reservacion
                (numero_reservacion, fecha_viaje, id_paquete, cantidad_pasajeros, id_agente_usuario, costo_total, id_estado_reservacion, activo)
                VALUES (?, ?, ?, ?, ?, ?, 1, TRUE)
                """, PreparedStatement.RETURN_GENERATED_KEYS)) {
            st.setString(1, numeroReservacion);
            st.setDate(2, fechaViaje);
            st.setInt(3, idPaquete);
            st.setInt(4, cantidadPasajeros);
            st.setInt(5, idUsuario);
            st.setDouble(6, precioVenta * cantidadPasajeros);
            st.executeUpdate();

            try (ResultSet keys = st.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalArgumentException("No se pudo obtener id de reservacion");
                }
                idReservacion = keys.getInt(1);
            }
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO reservacion_pasajero (id_reservacion, id_cliente)
                VALUES (?, ?)
                """)) {
            for (Integer idCliente : idClientes) {
                st.setInt(1, idReservacion);
                st.setInt(2, idCliente);
                st.addBatch();
            }
            st.executeBatch();
        }

        resultado.incrementar("reservaciones");
    }

    private void procesarPago(Connection connection, List<String> args, ResultadoCarga resultado) throws Exception {
        if (args.size() != 4) {
            throw new IllegalArgumentException("PAGO requiere 4 parametros");
        }

        String numeroReservacion = unquote(args.get(0));
        double monto = parseDouble(args.get(1), "MONTO invalido");
        int metodo = parseInt(args.get(2), "METODO invalido");
        Date fechaPago = parseInputDate(unquote(args.get(3)));

        if (monto <= 0) {
            throw new IllegalArgumentException("MONTO debe ser mayor a 0");
        }

        Integer idReservacion = findIdByString(connection, "SELECT id_reservacion FROM reservacion WHERE numero_reservacion = ?", numeroReservacion);
        if (idReservacion == null) {
            throw new IllegalArgumentException("Reservacion no existe: " + numeroReservacion);
        }

        Integer idMetodoPago = switch (metodo) {
            case 1, 2, 3 -> metodo;
            default -> throw new IllegalArgumentException("METODO invalido");
        };

        double costoTotal;
        int estadoActual;
        try (PreparedStatement st = connection.prepareStatement("""
                SELECT costo_total, id_estado_reservacion
                FROM reservacion
                WHERE id_reservacion = ?
                """)) {
            st.setInt(1, idReservacion);
            try (ResultSet rs = st.executeQuery()) {
                if (!rs.next()) {
                    throw new IllegalArgumentException("Reservacion no encontrada");
                }
                costoTotal = rs.getDouble("costo_total");
                estadoActual = rs.getInt("id_estado_reservacion");
            }
        }

        if (estadoActual == 3) {
            throw new IllegalArgumentException("No se puede pagar una reservacion cancelada");
        }

        try (PreparedStatement st = connection.prepareStatement("""
                INSERT INTO pago (id_reservacion, monto, id_metodo_pago, fecha_pago)
                VALUES (?, ?, ?, ?)
                """)) {
            st.setInt(1, idReservacion);
            st.setDouble(2, monto);
            st.setInt(3, idMetodoPago);
            st.setDate(4, fechaPago);
            st.executeUpdate();
        }

        double totalPagado;
        try (PreparedStatement st = connection.prepareStatement("""
                SELECT COALESCE(SUM(monto), 0) AS total_pagado
                FROM pago
                WHERE id_reservacion = ?
                """)) {
            st.setInt(1, idReservacion);
            try (ResultSet rs = st.executeQuery()) {
                rs.next();
                totalPagado = rs.getDouble("total_pagado");
            }
        }

        if (totalPagado >= costoTotal) {
            try (PreparedStatement st = connection.prepareStatement("""
                    UPDATE reservacion
                    SET id_estado_reservacion = 2
                    WHERE id_reservacion = ?
                    """)) {
                st.setInt(1, idReservacion);
                st.executeUpdate();
            }
        }

        resultado.incrementar("pagos");
    }

    private List<String> splitArguments(String input) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                current.append(c);
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }

        if (!current.isEmpty()) {
            result.add(current.toString().trim());
        }

        return result;
    }

    private String unquote(String value) {
        String trimmed = value.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length() >= 2) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String unquoteOrRaw(String value) {
        return unquote(value);
    }

    private int parseInt(String value, String message) {
        try {
            return Integer.parseInt(unquote(value));
        } catch (Exception e) {
            throw new IllegalArgumentException(message);
        }
    }

    private double parseDouble(String value, String message) {
        try {
            return Double.parseDouble(unquote(value));
        } catch (Exception e) {
            throw new IllegalArgumentException(message);
        }
    }

    private Date parseInputDate(String value) {
        try {
            LocalDate localDate = LocalDate.parse(value, INPUT_DATE);
            return Date.valueOf(localDate);
        } catch (Exception e) {
            throw new IllegalArgumentException("Fecha invalida: " + value + ". Use dd/mm/yyyy");
        }
    }

    private boolean existsByString(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, value);
            try (ResultSet rs = st.executeQuery()) {
                return rs.next();
            }
        }
    }

    private Integer findIdByString(Connection connection, String sql, String value) throws SQLException {
        try (PreparedStatement st = connection.prepareStatement(sql)) {
            st.setString(1, value);
            try (ResultSet rs = st.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return null;
    }

    private String generateNumeroReservacion() {
        return "RES-" + System.currentTimeMillis();
    }

    private Map<String, Object> buildError(int linea, String contenido, String mensaje) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("linea", linea);
        error.put("contenido", contenido);
        error.put("error", mensaje);
        return error;
    }

    public static class ResultadoCarga {
        private int totalLineas;
        private int lineasExitosas;
        private int lineasConError;
        private final Map<String, Integer> resumen = new LinkedHashMap<>();
        private final List<Map<String, Object>> errores = new ArrayList<>();

        public ResultadoCarga() {
            resumen.put("usuarios", 0);
            resumen.put("destinos", 0);
            resumen.put("proveedores", 0);
            resumen.put("paquetes", 0);
            resumen.put("serviciosPaquete", 0);
            resumen.put("clientes", 0);
            resumen.put("reservaciones", 0);
            resumen.put("pagos", 0);
        }

        public void incrementar(String clave) {
            resumen.put(clave, resumen.getOrDefault(clave, 0) + 1);
        }

        public Map<String, Object> getResumen() {
            Map<String, Object> data = new LinkedHashMap<>();
            data.put("totalLineasProcesadas", totalLineas);
            data.put("lineasExitosas", lineasExitosas);
            data.put("lineasConError", lineasConError);
            data.put("registrosInsertados", resumen);
            return data;
        }

        public List<Map<String, Object>> getErrores() {
            return errores;
        }
    }
}