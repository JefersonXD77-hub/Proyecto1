package com.horizontes.dao;

import com.horizontes.model.Cancelacion;
import com.horizontes.model.Reservacion;
import com.horizontes.model.Usuario;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.Optional;

public class CancelacionDAO {

    private static final String FIND_RESERVACION_CANCELABLE = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_viaje,
            r.costo_total,
            r.activo,
            er.id_estado_reservacion,
            er.nombre AS estado_nombre
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        WHERE r.id_reservacion = ?
        """;

    private static final String SUM_PAGOS = """
        SELECT COALESCE(SUM(monto), 0) AS total_pagado
        FROM pago
        WHERE id_reservacion = ?
        """;

    private static final String FIND_CANCELACION_BY_RESERVACION = """
        SELECT
            c.id_cancelacion,
            c.fecha_cancelacion,
            c.dias_anticipacion,
            c.porcentaje_reembolso,
            c.monto_pagado,
            c.monto_reembolsado,
            c.perdida_agencia,
            r.id_reservacion,
            r.numero_reservacion,
            u.id_usuario,
            u.username,
            u.nombre_completo
        FROM cancelacion c
        INNER JOIN reservacion r ON c.id_reservacion = r.id_reservacion
        INNER JOIN usuario u ON c.id_usuario_proceso = u.id_usuario
        WHERE c.id_reservacion = ?
        """;

    private static final String INSERT_CANCELACION = """
        INSERT INTO cancelacion
        (id_reservacion, fecha_cancelacion, dias_anticipacion, porcentaje_reembolso,
         monto_pagado, monto_reembolsado, perdida_agencia, id_usuario_proceso)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_RESERVACION_CANCELADA = """
        UPDATE reservacion
        SET id_estado_reservacion = 3
        WHERE id_reservacion = ?
        """;

    public ReservacionCancelacionInfo findReservacionInfo(int idReservacion) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_RESERVACION_CANCELABLE)) {

            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new ReservacionCancelacionInfo(
                            rs.getInt("id_reservacion"),
                            rs.getString("numero_reservacion"),
                            rs.getDate("fecha_viaje"),
                            rs.getDouble("costo_total"),
                            rs.getBoolean("activo"),
                            rs.getInt("id_estado_reservacion"),
                            rs.getString("estado_nombre")
                    );
                }
            }
        }

        return null;
    }

    public double getMontoPagado(int idReservacion) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SUM_PAGOS)) {

            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_pagado");
                }
            }
        }

        return 0;
    }

    public Cancelacion findByReservacionId(int idReservacion) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_CANCELACION_BY_RESERVACION)) {

            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Reservacion reservacion = new Reservacion();
                    reservacion.setIdReservacion(rs.getInt("id_reservacion"));
                    reservacion.setNumeroReservacion(rs.getString("numero_reservacion"));

                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));

                    Cancelacion cancelacion = new Cancelacion();
                    cancelacion.setIdCancelacion(rs.getInt("id_cancelacion"));
                    cancelacion.setFechaCancelacion(rs.getDate("fecha_cancelacion"));
                    cancelacion.setDiasAnticipacion(rs.getInt("dias_anticipacion"));
                    cancelacion.setPorcentajeReembolso(rs.getDouble("porcentaje_reembolso"));
                    cancelacion.setMontoPagado(rs.getDouble("monto_pagado"));
                    cancelacion.setMontoReembolsado(rs.getDouble("monto_reembolsado"));
                    cancelacion.setPerdidaAgencia(rs.getDouble("perdida_agencia"));
                    cancelacion.setReservacion(reservacion);
                    cancelacion.setUsuarioProceso(usuario);

                    return cancelacion;
                }
            }
        }

        return null;
    }

    public CancelacionResultado procesarCancelacion(Cancelacion cancelacion) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            try (PreparedStatement insertStmt = connection.prepareStatement(INSERT_CANCELACION)) {
                insertStmt.setInt(1, cancelacion.getReservacion().getIdReservacion());
                insertStmt.setDate(2, cancelacion.getFechaCancelacion());
                insertStmt.setInt(3, cancelacion.getDiasAnticipacion());
                insertStmt.setDouble(4, cancelacion.getPorcentajeReembolso());
                insertStmt.setDouble(5, cancelacion.getMontoPagado());
                insertStmt.setDouble(6, cancelacion.getMontoReembolsado());
                insertStmt.setDouble(7, cancelacion.getPerdidaAgencia());
                insertStmt.setInt(8, cancelacion.getUsuarioProceso().getIdUsuario());
                insertStmt.executeUpdate();
            }

            try (PreparedStatement updateStmt = connection.prepareStatement(UPDATE_RESERVACION_CANCELADA)) {
                updateStmt.setInt(1, cancelacion.getReservacion().getIdReservacion());
                updateStmt.executeUpdate();
            }

            connection.commit();

            return new CancelacionResultado(
                    cancelacion.getReservacion().getIdReservacion(),
                    cancelacion.getReservacion().getNumeroReservacion(),
                    cancelacion.getDiasAnticipacion(),
                    cancelacion.getPorcentajeReembolso(),
                    cancelacion.getMontoPagado(),
                    cancelacion.getMontoReembolsado(),
                    cancelacion.getPerdidaAgencia(),
                    "CANCELADA"
            );

        } catch (SQLException e) {
            if (connection != null) {
                connection.rollback();
            }
            throw e;

        } finally {
            if (connection != null) {
                connection.setAutoCommit(true);
                connection.close();
            }
        }
    }

    public record ReservacionCancelacionInfo(
            int idReservacion,
            String numeroReservacion,
            Date fechaViaje,
            double costoTotal,
            boolean activo,
            int idEstadoReservacion,
            String estadoNombre
    ) {}

    public record CancelacionResultado(
            int idReservacion,
            String numeroReservacion,
            int diasAnticipacion,
            double porcentajeReembolso,
            double montoPagado,
            double montoReembolsado,
            double perdidaAgencia,
            String estadoActual
    ) {}
}
