package com.horizontes.dao;

import com.horizontes.model.Destino;
import com.horizontes.model.PaqueteTuristico;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PaqueteTuristicoDAO {

    private static final String FIND_ALL = """
        SELECT 
            p.id_paquete,
            p.nombre,
            p.duracion_dias,
            p.descripcion,
            p.precio_venta,
            p.capacidad_maxima,
            p.activo,
            d.id_destino,
            d.nombre AS nombre_destino,
            d.pais,
            d.descripcion AS descripcion_destino,
            d.clima_epoca,
            d.url_imagen,
            d.activo AS destino_activo
        FROM paquete_turistico p
        INNER JOIN destino d ON p.id_destino = d.id_destino
        ORDER BY p.id_paquete ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT 
            p.id_paquete,
            p.nombre,
            p.duracion_dias,
            p.descripcion,
            p.precio_venta,
            p.capacidad_maxima,
            p.activo,
            d.id_destino,
            d.nombre AS nombre_destino,
            d.pais,
            d.descripcion AS descripcion_destino,
            d.clima_epoca,
            d.url_imagen,
            d.activo AS destino_activo
        FROM paquete_turistico p
        INNER JOIN destino d ON p.id_destino = d.id_destino
        WHERE p.id_paquete = ?
        """;

    private static final String INSERT = """
        INSERT INTO paquete_turistico
        (nombre, id_destino, duracion_dias, descripcion, precio_venta, capacidad_maxima, activo)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE = """
        UPDATE paquete_turistico
        SET nombre = ?, id_destino = ?, duracion_dias = ?, descripcion = ?, precio_venta = ?, capacidad_maxima = ?, activo = ?
        WHERE id_paquete = ?
        """;

    private static final String SOFT_DELETE = """
        UPDATE paquete_turistico
        SET activo = FALSE
        WHERE id_paquete = ?
        """;

    public List<PaqueteTuristico> findAll() throws SQLException {
        List<PaqueteTuristico> paquetes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                paquetes.add(mapResultSetToPaquete(rs));
            }
        }

        return paquetes;
    }

    public PaqueteTuristico findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToPaquete(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(PaqueteTuristico paquete) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setString(1, paquete.getNombre());
            statement.setInt(2, paquete.getDestino().getIdDestino());
            statement.setInt(3, paquete.getDuracionDias());
            statement.setString(4, paquete.getDescripcion());
            statement.setDouble(5, paquete.getPrecioVenta());
            statement.setInt(6, paquete.getCapacidadMaxima());
            statement.setBoolean(7, paquete.isActivo());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(PaqueteTuristico paquete) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, paquete.getNombre());
            statement.setInt(2, paquete.getDestino().getIdDestino());
            statement.setInt(3, paquete.getDuracionDias());
            statement.setString(4, paquete.getDescripcion());
            statement.setDouble(5, paquete.getPrecioVenta());
            statement.setInt(6, paquete.getCapacidadMaxima());
            statement.setBoolean(7, paquete.isActivo());
            statement.setInt(8, paquete.getIdPaquete());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SOFT_DELETE)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    private PaqueteTuristico mapResultSetToPaquete(ResultSet rs) throws SQLException {
        Destino destino = new Destino();
        destino.setIdDestino(rs.getInt("id_destino"));
        destino.setNombre(rs.getString("nombre_destino"));
        destino.setPais(rs.getString("pais"));
        destino.setDescripcion(rs.getString("descripcion_destino"));
        destino.setClimaEpoca(rs.getString("clima_epoca"));
        destino.setUrlImagen(rs.getString("url_imagen"));
        destino.setActivo(rs.getBoolean("destino_activo"));

        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setIdPaquete(rs.getInt("id_paquete"));
        paquete.setNombre(rs.getString("nombre"));
        paquete.setDestino(destino);
        paquete.setDuracionDias(rs.getInt("duracion_dias"));
        paquete.setDescripcion(rs.getString("descripcion"));
        paquete.setPrecioVenta(rs.getDouble("precio_venta"));
        paquete.setCapacidadMaxima(rs.getInt("capacidad_maxima"));
        paquete.setActivo(rs.getBoolean("activo"));

        return paquete;
    }
}
