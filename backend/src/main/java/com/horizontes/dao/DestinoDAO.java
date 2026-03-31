package com.horizontes.dao;

import com.horizontes.model.Destino;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DestinoDAO {

    private static final String FIND_ALL = """
        SELECT id_destino, nombre, pais, descripcion, clima_epoca, url_imagen, activo
        FROM destino
        ORDER BY id_destino ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT id_destino, nombre, pais, descripcion, clima_epoca, url_imagen, activo
        FROM destino
        WHERE id_destino = ?
        """;

    private static final String INSERT = """
        INSERT INTO destino (nombre, pais, descripcion, clima_epoca, url_imagen, activo)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE = """
        UPDATE destino
        SET nombre = ?, pais = ?, descripcion = ?, clima_epoca = ?, url_imagen = ?, activo = ?
        WHERE id_destino = ?
        """;

    private static final String SOFT_DELETE = """
        UPDATE destino
        SET activo = FALSE
        WHERE id_destino = ?
        """;

    public List<Destino> findAll() throws SQLException {
        List<Destino> destinos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                destinos.add(mapResultSetToDestino(rs));
            }
        }

        return destinos;
    }

    public Destino findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToDestino(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(Destino destino) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setString(1, destino.getNombre());
            statement.setString(2, destino.getPais());
            statement.setString(3, destino.getDescripcion());
            statement.setString(4, destino.getClimaEpoca());
            statement.setString(5, destino.getUrlImagen());
            statement.setBoolean(6, destino.isActivo());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Destino destino) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, destino.getNombre());
            statement.setString(2, destino.getPais());
            statement.setString(3, destino.getDescripcion());
            statement.setString(4, destino.getClimaEpoca());
            statement.setString(5, destino.getUrlImagen());
            statement.setBoolean(6, destino.isActivo());
            statement.setInt(7, destino.getIdDestino());

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

    private Destino mapResultSetToDestino(ResultSet rs) throws SQLException {
        Destino destino = new Destino();
        destino.setIdDestino(rs.getInt("id_destino"));
        destino.setNombre(rs.getString("nombre"));
        destino.setPais(rs.getString("pais"));
        destino.setDescripcion(rs.getString("descripcion"));
        destino.setClimaEpoca(rs.getString("clima_epoca"));
        destino.setUrlImagen(rs.getString("url_imagen"));
        destino.setActivo(rs.getBoolean("activo"));
        return destino;
    }
}
