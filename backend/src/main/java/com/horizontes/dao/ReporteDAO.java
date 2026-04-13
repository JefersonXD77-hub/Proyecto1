package com.horizontes.dao;

import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReporteDAO {

    public List<Map<String, Object>> reporteVentas(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = """
            SELECT
                r.id_reservacion,
                r.numero_reservacion,
                r.fecha_creacion,
                r.fecha_viaje,
                r.costo_total,
                p.nombre AS nombre_paquete,
                u.nombre_completo AS agente,
                GROUP_CONCAT(c.nombre_completo SEPARATOR ', ') AS pasajeros
            FROM reservacion r
            INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
            INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
            INNER JOIN reservacion_pasajero rp ON r.id_reservacion = rp.id_reservacion
            INNER JOIN cliente c ON rp.id_cliente = c.id_cliente
            INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
            WHERE er.nombre = 'CONFIRMADA'
            %s
            GROUP BY r.id_reservacion, r.numero_reservacion, r.fecha_creacion, r.fecha_viaje, r.costo_total, p.nombre, u.nombre_completo
            ORDER BY r.fecha_creacion DESC
            """;

        return executeQueryWithDateFilter(sql, fechaInicio, fechaFin, "r.fecha_creacion");
    }

    public List<Map<String, Object>> reporteCancelaciones(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = """
            SELECT
                c.id_cancelacion,
                r.numero_reservacion,
                c.fecha_cancelacion,
                c.monto_reembolsado,
                c.perdida_agencia
            FROM cancelacion c
            INNER JOIN reservacion r ON c.id_reservacion = r.id_reservacion
            WHERE 1=1
            %s
            ORDER BY c.fecha_cancelacion DESC
            """;

        return executeQueryWithDateFilter(sql, fechaInicio, fechaFin, "c.fecha_cancelacion");
    }

    public Map<String, Object> reporteGanancias(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = """
            SELECT
                COALESCE(SUM(v.ganancia_bruta_reservacion), 0) AS total_ganancias_brutas,
                COALESCE((SELECT SUM(c.monto_reembolsado)
                          FROM cancelacion c WHERE 1=1 %s), 0) AS total_reembolsos
            FROM (
                SELECT
                    r.id_reservacion,
                    ((p.precio_venta - COALESCE(costos.costo_total_paquete, 0)) * r.cantidad_pasajeros) AS ganancia_bruta_reservacion
                FROM reservacion r
                INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
                INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
                LEFT JOIN (
                    SELECT id_paquete, COALESCE(SUM(costo), 0) AS costo_total_paquete
                    FROM servicio_paquete
                    GROUP BY id_paquete
                ) costos ON p.id_paquete = costos.id_paquete
                WHERE er.nombre = 'CONFIRMADA'
                %s
            ) v
            """;

        Map<String, Object> result = new HashMap<>();

        String filterCancelaciones = buildDateFilter("c.fecha_cancelacion", fechaInicio, fechaFin);
        String filterReservaciones = buildDateFilter("r.fecha_creacion", fechaInicio, fechaFin);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(
                     String.format(sql, filterCancelaciones, filterReservaciones))) {

            int index = 1;
            index = setDateFilterParameters(statement, index, fechaInicio, fechaFin, filterCancelaciones);
            setDateFilterParameters(statement, index, fechaInicio, fechaFin, filterReservaciones);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    double brutas = rs.getDouble("total_ganancias_brutas");
                    double reembolsos = rs.getDouble("total_reembolsos");
                    result.put("totalGananciasBrutas", brutas);
                    result.put("totalReembolsos", reembolsos);
                    result.put("gananciaNeta", brutas - reembolsos);
                }
            }
        }

        return result;
    }

    public Map<String, Object> reporteAgenteMasVentas(Date fechaInicio, Date fechaFin) throws SQLException {
        String topSql = """
            SELECT
                u.id_usuario,
                u.nombre_completo AS agente,
                COUNT(r.id_reservacion) AS total_ventas
            FROM reservacion r
            INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
            INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
            WHERE er.nombre = 'CONFIRMADA'
            %s
            GROUP BY u.id_usuario, u.nombre_completo
            ORDER BY total_ventas DESC
            LIMIT 1
            """;

        String detalleSql = """
            SELECT
                r.numero_reservacion,
                r.fecha_creacion,
                r.costo_total,
                p.nombre AS nombre_paquete
            FROM reservacion r
            INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
            INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
            WHERE er.nombre = 'CONFIRMADA'
              AND r.id_agente_usuario = ?
            %s
            ORDER BY r.fecha_creacion DESC
            """;

        Map<String, Object> result = new HashMap<>();
        String filter = buildDateFilter("r.fecha_creacion", fechaInicio, fechaFin);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement topStmt = connection.prepareStatement(String.format(topSql, filter))) {

            setDateFilterParameters(topStmt, 1, fechaInicio, fechaFin, filter);

            try (ResultSet rs = topStmt.executeQuery()) {
                if (!rs.next()) return result;

                int idUsuario = rs.getInt("id_usuario");
                result.put("agente", rs.getString("agente"));
                result.put("totalVentas", rs.getInt("total_ventas"));

                try (PreparedStatement detalleStmt = connection.prepareStatement(String.format(detalleSql, filter))) {
                    int idx = 1;
                    detalleStmt.setInt(idx++, idUsuario);
                    setDateFilterParameters(detalleStmt, idx, fechaInicio, fechaFin, filter);
                    result.put("reservaciones", readRows(detalleStmt.executeQuery()));
                }
            }
        }

        return result;
    }

    public Map<String, Object> reporteAgenteMasGanancias(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = """
            SELECT
                u.id_usuario,
                u.nombre_completo AS agente,
                COALESCE(SUM((p.precio_venta - COALESCE(costos.costo_total_paquete, 0)) * r.cantidad_pasajeros), 0) AS total_ganancia
            FROM reservacion r
            INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
            INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
            INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
            LEFT JOIN (
                SELECT id_paquete, COALESCE(SUM(costo), 0) AS costo_total_paquete
                FROM servicio_paquete
                GROUP BY id_paquete
            ) costos ON p.id_paquete = costos.id_paquete
            WHERE er.nombre = 'CONFIRMADA'
            %s
            GROUP BY u.id_usuario, u.nombre_completo
            ORDER BY total_ganancia DESC
            LIMIT 1
            """;

        String filter = buildDateFilter("r.fecha_creacion", fechaInicio, fechaFin);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(String.format(sql, filter))) {

            setDateFilterParameters(statement, 1, fechaInicio, fechaFin, filter);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Map<String, Object> result = new HashMap<>();
                    result.put("agente", rs.getString("agente"));
                    result.put("totalGanancia", rs.getDouble("total_ganancia"));
                    return result;
                }
            }
        }

        return new HashMap<>();
    }

    public Map<String, Object> reportePaqueteMasVendido(Date fechaInicio, Date fechaFin, boolean asc) throws SQLException {
        String topSql = """
            SELECT
                p.id_paquete,
                p.nombre AS paquete,
                COUNT(r.id_reservacion) AS total_reservaciones
            FROM reservacion r
            INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
            WHERE 1=1
            %s
            GROUP BY p.id_paquete, p.nombre
            ORDER BY total_reservaciones %s
            LIMIT 1
            """;

        String detalleSql = """
            SELECT
                r.numero_reservacion,
                r.fecha_creacion,
                r.costo_total
            FROM reservacion r
            WHERE r.id_paquete = ?
            %s
            ORDER BY r.fecha_creacion DESC
            """;

        Map<String, Object> result = new HashMap<>();
        String filter = buildDateFilter("r.fecha_creacion", fechaInicio, fechaFin);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement topStmt = connection.prepareStatement(String.format(topSql, filter, asc ? "ASC" : "DESC"))) {

            setDateFilterParameters(topStmt, 1, fechaInicio, fechaFin, filter);

            try (ResultSet rs = topStmt.executeQuery()) {
                if (!rs.next()) return result;

                int idPaquete = rs.getInt("id_paquete");
                result.put("paquete", rs.getString("paquete"));
                result.put("totalReservaciones", rs.getInt("total_reservaciones"));

                try (PreparedStatement detalleStmt = connection.prepareStatement(String.format(detalleSql, filter))) {
                    int idx = 1;
                    detalleStmt.setInt(idx++, idPaquete);
                    setDateFilterParameters(detalleStmt, idx, fechaInicio, fechaFin, filter);
                    result.put("reservaciones", readRows(detalleStmt.executeQuery()));
                }
            }
        }

        return result;
    }

    public List<Map<String, Object>> reporteOcupacionPorDestino(Date fechaInicio, Date fechaFin) throws SQLException {
        String sql = """
            SELECT
                d.nombre AS destino,
                COUNT(r.id_reservacion) AS cantidad_reservaciones
            FROM reservacion r
            INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
            INNER JOIN destino d ON p.id_destino = d.id_destino
            WHERE 1=1
            %s
            GROUP BY d.nombre
            ORDER BY cantidad_reservaciones DESC
            """;

        return executeQueryWithDateFilter(sql, fechaInicio, fechaFin, "r.fecha_creacion");
    }

    private List<Map<String, Object>> executeQueryWithDateFilter(String sqlTemplate, Date fechaInicio, Date fechaFin, String dateColumn) throws SQLException {
        String filter = buildDateFilter(dateColumn, fechaInicio, fechaFin);
        String sql = String.format(sqlTemplate, filter);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            setDateFilterParameters(statement, 1, fechaInicio, fechaFin, filter);

            try (ResultSet rs = statement.executeQuery()) {
                return readRows(rs);
            }
        }
    }

    private String buildDateFilter(String column, Date fechaInicio, Date fechaFin) {
        if (fechaInicio != null && fechaFin != null) return " AND " + column + " BETWEEN ? AND ? ";
        if (fechaInicio != null) return " AND " + column + " >= ? ";
        if (fechaFin != null) return " AND " + column + " <= ? ";
        return "";
    }

    private int setDateFilterParameters(PreparedStatement statement, int startIndex, Date fechaInicio, Date fechaFin, String filter) throws SQLException {
        int index = startIndex;
        if (filter.contains("BETWEEN ? AND ?")) {
            statement.setDate(index++, fechaInicio);
            statement.setDate(index++, fechaFin);
        } else if (filter.contains(">= ?")) {
            statement.setDate(index++, fechaInicio);
        } else if (filter.contains("<= ?")) {
            statement.setDate(index++, fechaFin);
        }
        return index;
    }

    private List<Map<String, Object>> readRows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columns; i++) {
                row.put(meta.getColumnLabel(i), rs.getObject(i));
            }
            rows.add(row);
        }

        return rows;
    }
}
