package com.horizontes.dao;

import com.horizontes.model.Cliente;
import com.horizontes.model.PaqueteTuristico;
import com.horizontes.model.Reservacion;
import com.horizontes.model.Usuario;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReservacionDAO {

    private static final String FIND_ALL = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_creacion,
            r.fecha_viaje,
            r.cantidad_pasajeros,
            r.costo_total,
            r.activo,
            er.nombre AS estado_nombre,
            p.id_paquete,
            p.nombre AS nombre_paquete,
            u.id_usuario,
            u.username,
            u.nombre_completo
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
        INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
        ORDER BY r.id_reservacion DESC
        """;

    private static final String FIND_BY_ID = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_creacion,
            r.fecha_viaje,
            r.cantidad_pasajeros,
            r.costo_total,
            r.activo,
            er.nombre AS estado_nombre,
            p.id_paquete,
            p.nombre AS nombre_paquete,
            u.id_usuario,
            u.username,
            u.nombre_completo
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
        INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
        WHERE r.id_reservacion = ?
        """;

    private static final String FIND_BY_CLIENTE = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_creacion,
            r.fecha_viaje,
            r.cantidad_pasajeros,
            r.costo_total,
            r.activo,
            er.nombre AS estado_nombre,
            p.id_paquete,
            p.nombre AS nombre_paquete,
            u.id_usuario,
            u.username,
            u.nombre_completo
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
        INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
        INNER JOIN reservacion_pasajero rp ON r.id_reservacion = rp.id_reservacion
        WHERE rp.id_cliente = ?
        ORDER BY r.fecha_creacion DESC, r.id_reservacion DESC
        """;

    private static final String FIND_DEL_DIA = """
        SELECT
            r.id_reservacion,
            r.numero_reservacion,
            r.fecha_creacion,
            r.fecha_viaje,
            r.cantidad_pasajeros,
            r.costo_total,
            r.activo,
            er.nombre AS estado_nombre,
            p.id_paquete,
            p.nombre AS nombre_paquete,
            u.id_usuario,
            u.username,
            u.nombre_completo
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        INNER JOIN paquete_turistico p ON r.id_paquete = p.id_paquete
        INNER JOIN usuario u ON r.id_agente_usuario = u.id_usuario
        WHERE DATE(r.fecha_creacion) = CURDATE()
        ORDER BY r.fecha_creacion DESC, r.id_reservacion DESC
        """;

    private static final String FIND_PASAJEROS_BY_RESERVACION = """
        SELECT
            c.id_cliente,
            c.dpi_pasaporte,
            c.nombre_completo,
            c.fecha_nacimiento,
            c.telefono,
            c.email,
            c.nacionalidad,
            c.activo
        FROM reservacion_pasajero rp
        INNER JOIN cliente c ON rp.id_cliente = c.id_cliente
        WHERE rp.id_reservacion = ?
        ORDER BY c.id_cliente ASC
        """;

    private static final String FIND_PAQUETE_PRECIO = """
        SELECT id_paquete, nombre, precio_venta, capacidad_maxima, activo
        FROM paquete_turistico
        WHERE id_paquete = ?
        """;

    private static final String FIND_DISPONIBLES_BY_FECHA_DESTINO = """
        SELECT
            p.id_paquete,
            p.nombre AS nombre_paquete,
            d.id_destino,
            d.nombre AS nombre_destino,
            p.precio_venta,
            p.capacidad_maxima,
            COALESCE(SUM(
                CASE
                    WHEN r.id_reservacion IS NOT NULL
                         AND r.activo = TRUE
                         AND er.nombre <> 'CANCELADA'
                    THEN r.cantidad_pasajeros
                    ELSE 0
                END
            ), 0) AS cupos_ocupados,
            (p.capacidad_maxima - COALESCE(SUM(
                CASE
                    WHEN r.id_reservacion IS NOT NULL
                         AND r.activo = TRUE
                         AND er.nombre <> 'CANCELADA'
                    THEN r.cantidad_pasajeros
                    ELSE 0
                END
            ), 0)) AS cupos_disponibles
        FROM paquete_turistico p
        INNER JOIN destino d ON p.id_destino = d.id_destino
        LEFT JOIN reservacion r
            ON r.id_paquete = p.id_paquete
           AND r.fecha_viaje = ?
        LEFT JOIN estado_reservacion er
            ON r.id_estado_reservacion = er.id_estado_reservacion
        WHERE p.activo = TRUE
          AND d.activo = TRUE
          AND d.id_destino = ?
        GROUP BY
            p.id_paquete,
            p.nombre,
            d.id_destino,
            d.nombre,
            p.precio_venta,
            p.capacidad_maxima
        ORDER BY p.nombre ASC
        """;

    private static final String FIND_CUPOS_OCUPADOS_BY_PAQUETE_FECHA = """
        SELECT COALESCE(SUM(r.cantidad_pasajeros), 0) AS cupos_ocupados
        FROM reservacion r
        INNER JOIN estado_reservacion er ON r.id_estado_reservacion = er.id_estado_reservacion
        WHERE r.id_paquete = ?
          AND r.fecha_viaje = ?
          AND r.activo = TRUE
          AND er.nombre <> 'CANCELADA'
        """;

    private static final String INSERT_RESERVACION = """
        INSERT INTO reservacion
        (numero_reservacion, fecha_viaje, id_paquete, cantidad_pasajeros, id_agente_usuario, costo_total, id_estado_reservacion, activo)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String INSERT_RESERVACION_PASAJERO = """
        INSERT INTO reservacion_pasajero (id_reservacion, id_cliente)
        VALUES (?, ?)
        """;

    public List<Reservacion> findAll() throws SQLException {
        List<Reservacion> reservaciones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Reservacion reservacion = mapResultSetToReservacion(rs);
                reservacion.setPasajeros(findPasajerosByReservacion(connection, reservacion.getIdReservacion()));
                reservaciones.add(reservacion);
            }
        }

        return reservaciones;
    }

    public Reservacion findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Reservacion reservacion = mapResultSetToReservacion(rs);
                    reservacion.setPasajeros(findPasajerosByReservacion(connection, id));
                    return reservacion;
                }
            }
        }

        return null;
    }

    public List<Reservacion> findByCliente(int idCliente) throws SQLException {
        List<Reservacion> reservaciones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_CLIENTE)) {

            statement.setInt(1, idCliente);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Reservacion reservacion = mapResultSetToReservacion(rs);
                    reservacion.setPasajeros(findPasajerosByReservacion(connection, reservacion.getIdReservacion()));
                    reservaciones.add(reservacion);
                }
            }
        }

        return reservaciones;
    }

    public List<Reservacion> findReservacionesDelDia() throws SQLException {
        List<Reservacion> reservaciones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_DEL_DIA);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Reservacion reservacion = mapResultSetToReservacion(rs);
                reservacion.setPasajeros(findPasajerosByReservacion(connection, reservacion.getIdReservacion()));
                reservaciones.add(reservacion);
            }
        }

        return reservaciones;
    }

    public List<Map<String, Object>> findDisponiblesByFechaDestino(Date fechaViaje, int idDestino) throws SQLException {
        List<Map<String, Object>> disponibles = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_DISPONIBLES_BY_FECHA_DESTINO)) {

            statement.setDate(1, fechaViaje);
            statement.setInt(2, idDestino);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    int cuposOcupados = rs.getInt("cupos_ocupados");
                    int cuposDisponibles = rs.getInt("cupos_disponibles");

                    row.put("idPaquete", rs.getInt("id_paquete"));
                    row.put("nombrePaquete", rs.getString("nombre_paquete"));
                    row.put("idDestino", rs.getInt("id_destino"));
                    row.put("nombreDestino", rs.getString("nombre_destino"));
                    row.put("fechaViaje", fechaViaje);
                    row.put("precioVenta", rs.getDouble("precio_venta"));
                    row.put("capacidadMaxima", rs.getInt("capacidad_maxima"));
                    row.put("cuposOcupados", cuposOcupados);
                    row.put("cuposDisponibles", Math.max(0, cuposDisponibles));
                    row.put("disponible", cuposDisponibles > 0);

                    disponibles.add(row);
                }
            }
        }

        return disponibles;
    }

    public int getCuposOcupados(int idPaquete, Date fechaViaje) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_CUPOS_OCUPADOS_BY_PAQUETE_FECHA)) {

            statement.setInt(1, idPaquete);
            statement.setDate(2, fechaViaje);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cupos_ocupados");
                }
            }
        }

        return 0;
    }

    public PaqueteTuristico findPaqueteBasicoById(int idPaquete) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_PAQUETE_PRECIO)) {

            statement.setInt(1, idPaquete);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    PaqueteTuristico paquete = new PaqueteTuristico();
                    paquete.setIdPaquete(rs.getInt("id_paquete"));
                    paquete.setNombre(rs.getString("nombre"));
                    paquete.setPrecioVenta(rs.getDouble("precio_venta"));
                    paquete.setCapacidadMaxima(rs.getInt("capacidad_maxima"));
                    paquete.setActivo(rs.getBoolean("activo"));
                    return paquete;
                }
            }
        }

        return null;
    }

    public boolean createReservacion(Reservacion reservacion) throws SQLException {
        Connection connection = null;

        try {
            connection = DatabaseConnection.getConnection();
            connection.setAutoCommit(false);

            int cuposOcupados = getCuposOcupadosTransactional(
                    connection,
                    reservacion.getPaquete().getIdPaquete(),
                    reservacion.getFechaViaje()
            );

            int cuposDisponibles = reservacion.getPaquete().getCapacidadMaxima() - cuposOcupados;
            if (reservacion.getCantidadPasajeros() > cuposDisponibles) {
                connection.rollback();
                return false;
            }

            int reservacionId;

            try (PreparedStatement statement = connection.prepareStatement(INSERT_RESERVACION, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, reservacion.getNumeroReservacion());
                statement.setDate(2, reservacion.getFechaViaje());
                statement.setInt(3, reservacion.getPaquete().getIdPaquete());
                statement.setInt(4, reservacion.getCantidadPasajeros());
                statement.setInt(5, reservacion.getAgente().getIdUsuario());
                statement.setDouble(6, reservacion.getCostoTotal());
                statement.setInt(7, 1);
                statement.setBoolean(8, true);

                int rows = statement.executeUpdate();
                if (rows == 0) {
                    connection.rollback();
                    return false;
                }

                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        reservacionId = keys.getInt(1);
                    } else {
                        connection.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(INSERT_RESERVACION_PASAJERO)) {
                for (Cliente pasajero : reservacion.getPasajeros()) {
                    statement.setInt(1, reservacionId);
                    statement.setInt(2, pasajero.getIdCliente());
                    statement.addBatch();
                }
                statement.executeBatch();
            }

            connection.commit();
            return true;

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

    private int getCuposOcupadosTransactional(Connection connection, int idPaquete, Date fechaViaje) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(FIND_CUPOS_OCUPADOS_BY_PAQUETE_FECHA)) {
            statement.setInt(1, idPaquete);
            statement.setDate(2, fechaViaje);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("cupos_ocupados");
                }
            }
        }
        return 0;
    }

    private List<Cliente> findPasajerosByReservacion(Connection connection, int idReservacion) throws SQLException {
        List<Cliente> pasajeros = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(FIND_PASAJEROS_BY_RESERVACION)) {
            statement.setInt(1, idReservacion);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdCliente(rs.getInt("id_cliente"));
                    cliente.setDpiPasaporte(rs.getString("dpi_pasaporte"));
                    cliente.setNombreCompleto(rs.getString("nombre_completo"));
                    cliente.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
                    cliente.setTelefono(rs.getString("telefono"));
                    cliente.setEmail(rs.getString("email"));
                    cliente.setNacionalidad(rs.getString("nacionalidad"));
                    cliente.setActivo(rs.getBoolean("activo"));
                    pasajeros.add(cliente);
                }
            }
        }

        return pasajeros;
    }

    private Reservacion mapResultSetToReservacion(ResultSet rs) throws SQLException {
        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setIdPaquete(rs.getInt("id_paquete"));
        paquete.setNombre(rs.getString("nombre_paquete"));

        Usuario agente = new Usuario();
        agente.setIdUsuario(rs.getInt("id_usuario"));
        agente.setUsername(rs.getString("username"));
        agente.setNombreCompleto(rs.getString("nombre_completo"));

        Reservacion reservacion = new Reservacion();
        reservacion.setIdReservacion(rs.getInt("id_reservacion"));
        reservacion.setNumeroReservacion(rs.getString("numero_reservacion"));
        reservacion.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
        reservacion.setFechaViaje(rs.getDate("fecha_viaje"));
        reservacion.setCantidadPasajeros(rs.getInt("cantidad_pasajeros"));
        reservacion.setCostoTotal(rs.getDouble("costo_total"));
        reservacion.setEstado(rs.getString("estado_nombre"));
        reservacion.setActivo(rs.getBoolean("activo"));
        reservacion.setPaquete(paquete);
        reservacion.setAgente(agente);

        return reservacion;
    }
}