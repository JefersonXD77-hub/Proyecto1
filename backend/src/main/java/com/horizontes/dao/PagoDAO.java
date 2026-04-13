package com.horizontes.dao;

import com.horizontes.model.MetodoPago;
import com.horizontes.model.Pago;
import com.horizontes.model.Reservacion;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PagoDAO {

    private static final String FIND_ALL_BY_RESERVACION = """
        SELECT
            p.id_pago,
            p.monto,
            p.fecha_pago,
            r.id_reservacion,
            r.numero_reservacion,
            mp.id_metodo_pago,
            mp.nombre AS nombre_metodo
        FROM pago p
        INNER JOIN reservacion r ON p.id_reservacion = r.id_reservacion
        INNER JOIN metodo_pago mp ON p.id_metodo_pago = mp.id_metodo_pago
        WHERE p.id_reservacion = ?
        ORDER BY p.id_pago ASC
        """;

    private static final String INSERT_PAGO = """
        INSERT INTO pago (id_reservacion, monto, id_metodo_pago, fecha_pago)
        VALUES (?, ?, ?, ?)
        """;

    private static final String SUM_PAGOS = """
        SELECT COALESCE(SUM(monto), 0) AS total_pagado
        FROM pago
        WHERE id_reservacion = ?
        """;

    private static final String FIND_RESERVACION_BASICA = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.costo_total,
            r.activo,
            er.id_estado_reservacion,
            er.nombre AS estado_nombre
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        WHERE r.id_reservacion = ?
        """;

    private static final String UPDATE_ESTADO_CONFIRMADA = """
        UPDATE reservacion
        SET id_estado_reservacion = 2
        WHERE id_reservacion = ?
        """;

    private static final String FIND_COMPROBANTE_INFO = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_creacion,
            r.fecha_viaje,
            r.costo_total,
            er.nombre AS estado_reservacion,
            paq.nombre AS nombre_paquete,
            u.nombre_completo AS agente,
            COALESCE(SUM(p.monto), 0) AS total_pagado
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        INNER JOIN paquete_turistico paq ON r.id_paquete = paq.id_paquete
        INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
        LEFT JOIN pago p ON r.id_reservacion = p.id_reservacion
        WHERE r.id_reservacion = ?
        GROUP BY
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_creacion,
            r.fecha_viaje,
            r.costo_total,
            er.nombre,
            paq.nombre,
            u.nombre_completo
        """;

    public List<Pago> findAllByReservacion(int idReservacion) throws SQLException {
        List<Pago> pagos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_BY_RESERVACION)) {

            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }
        }

        return pagos;
    }

    public double getTotalPagado(int idReservacion) throws SQLException {
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

    public ReservacionPagoInfo findReservacionPagoInfo(int idReservacion) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_RESERVACION_BASICA)) {

            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new ReservacionPagoInfo(
                            rs.getInt("id_reservacion"),
                            rs.getString("numero_reservacion"),
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

    public PagoRegistroResultado registrarPago(Pago pago) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            ReservacionPagoInfo info = findReservacionPagoInfoTransactional(connection, pago.getReservacion().getIdReservacion());
            if (info == null) {
                connection.rollback();
                return null;
            }

            try (PreparedStatement statement = connection.prepareStatement(INSERT_PAGO)) {
                statement.setInt(1, pago.getReservacion().getIdReservacion());
                statement.setDouble(2, pago.getMonto());
                statement.setInt(3, pago.getMetodoPago().getIdMetodoPago());
                statement.setDate(4, pago.getFechaPago());
                statement.executeUpdate();
            }

            double totalPagado = getTotalPagadoTransactional(connection, pago.getReservacion().getIdReservacion());
            String estadoActual = info.estadoNombre();

            if (totalPagado >= info.costoTotal() && info.idEstadoReservacion() != 2) {
                try (PreparedStatement statement = connection.prepareStatement(UPDATE_ESTADO_CONFIRMADA)) {
                    statement.setInt(1, pago.getReservacion().getIdReservacion());
                    statement.executeUpdate();
                }
                estadoActual = "CONFIRMADA";
            }

            connection.commit();

            double saldoPendiente = Math.max(0, info.costoTotal() - totalPagado);

            return new PagoRegistroResultado(
                    info.idReservacion(),
                    info.numeroReservacion(),
                    info.costoTotal(),
                    totalPagado,
                    saldoPendiente,
                    estadoActual
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

    public ComprobantePagoInfo findComprobanteInfo(int idReservacion) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_COMPROBANTE_INFO)) {

            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    double costoTotal = rs.getDouble("costo_total");
                    double totalPagado = rs.getDouble("total_pagado");

                    return new ComprobantePagoInfo(
                            rs.getInt("id_reservacion"),
                            rs.getString("numero_reservacion"),
                            rs.getTimestamp("fecha_creacion"),
                            rs.getDate("fecha_viaje"),
                            rs.getString("nombre_paquete"),
                            rs.getString("agente"),
                            rs.getString("estado_reservacion"),
                            costoTotal,
                            totalPagado,
                            Math.max(0, costoTotal - totalPagado)
                    );
                }
            }
        }

        return null;
    }

    private ReservacionPagoInfo findReservacionPagoInfoTransactional(Connection connection, int idReservacion) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_RESERVACION_BASICA)) {
            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new ReservacionPagoInfo(
                            rs.getInt("id_reservacion"),
                            rs.getString("numero_reservacion"),
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

    private double getTotalPagadoTransactional(Connection connection, int idReservacion) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(SUM_PAGOS)) {
            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("total_pagado");
                }
            }
        }

        return 0;
    }

    private Pago mapResultSetToPago(ResultSet rs) throws SQLException {
        Reservacion reservacion = new Reservacion();
        reservacion.setIdReservacion(rs.getInt("id_reservacion"));
        reservacion.setNumeroReservacion(rs.getString("numero_reservacion"));

        MetodoPago metodoPago = new MetodoPago();
        metodoPago.setIdMetodoPago(rs.getInt("id_metodo_pago"));
        metodoPago.setNombre(rs.getString("nombre_metodo"));

        Pago pago = new Pago();
        pago.setIdPago(rs.getInt("id_pago"));
        pago.setMonto(rs.getDouble("monto"));
        pago.setFechaPago(rs.getDate("fecha_pago"));
        pago.setReservacion(reservacion);
        pago.setMetodoPago(metodoPago);

        return pago;
    }

    public record ReservacionPagoInfo(
            int idReservacion,
            String numeroReservacion,
            double costoTotal,
            boolean activo,
            int idEstadoReservacion,
            String estadoNombre
    ) {}

    public record PagoRegistroResultado(
            int idReservacion,
            String numeroReservacion,
            double costoTotal,
            double totalPagado,
            double saldoPendiente,
            String estadoActual
    ) {}

    public record ComprobantePagoInfo(
            int idReservacion,
            String numeroReservacion,
            Timestamp fechaCreacion,
            Date fechaViaje,
            String nombrePaquete,
            String agente,
            String estadoReservacion,
            double costoTotal,
            double totalPagado,
            double saldoPendiente
    ) {}
}