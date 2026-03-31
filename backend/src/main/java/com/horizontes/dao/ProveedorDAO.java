package com.horizontes.dao;

import com.horizontes.model.Proveedor;
import com.horizontes.model.TipoProveedor;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProveedorDAO {

    private static final String FIND_ALL = """
        SELECT 
            p.id_proveedor,
            p.nombre,
            p.pais_operacion,
            p.contacto,
            p.activo,
            tp.id_tipo_proveedor,
            tp.nombre AS nombre_tipo
        FROM proveedor p
        INNER JOIN tipo_proveedor tp ON p.id_tipo_proveedor = tp.id_tipo_proveedor
        ORDER BY p.id_proveedor ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT 
            p.id_proveedor,
            p.nombre,
            p.pais_operacion,
            p.contacto,
            p.activo,
            tp.id_tipo_proveedor,
            tp.nombre AS nombre_tipo
        FROM proveedor p
        INNER JOIN tipo_proveedor tp ON p.id_tipo_proveedor = tp.id_tipo_proveedor
        WHERE p.id_proveedor = ?
        """;

    private static final String INSERT = """
        INSERT INTO proveedor (nombre, id_tipo_proveedor, pais_operacion, contacto, activo)
        VALUES (?, ?, ?, ?, ?)
        """;

    private static final String UPDATE = """
        UPDATE proveedor
        SET nombre = ?, id_tipo_proveedor = ?, pais_operacion = ?, contacto = ?, activo = ?
        WHERE id_proveedor = ?
        """;

    private static final String SOFT_DELETE = """
        UPDATE proveedor
        SET activo = FALSE
        WHERE id_proveedor = ?
        """;

    public List<Proveedor> findAll() throws SQLException {
        List<Proveedor> proveedores = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                proveedores.add(mapResultSetToProveedor(rs));
            }
        }

        return proveedores;
    }

    public Proveedor findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToProveedor(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(Proveedor proveedor) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setString(1, proveedor.getNombre());
            statement.setInt(2, proveedor.getTipoProveedor().getIdTipoProveedor());
            statement.setString(3, proveedor.getPaisOperacion());
            statement.setString(4, proveedor.getContacto());
            statement.setBoolean(5, proveedor.isActivo());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Proveedor proveedor) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, proveedor.getNombre());
            statement.setInt(2, proveedor.getTipoProveedor().getIdTipoProveedor());
            statement.setString(3, proveedor.getPaisOperacion());
            statement.setString(4, proveedor.getContacto());
            statement.setBoolean(5, proveedor.isActivo());
            statement.setInt(6, proveedor.getIdProveedor());

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

    private Proveedor mapResultSetToProveedor(ResultSet rs) throws SQLException {
        TipoProveedor tipo = new TipoProveedor();
        tipo.setIdTipoProveedor(rs.getInt("id_tipo_proveedor"));
        tipo.setNombre(rs.getString("nombre_tipo"));

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(rs.getInt("id_proveedor"));
        proveedor.setNombre(rs.getString("nombre"));
        proveedor.setTipoProveedor(tipo);
        proveedor.setPaisOperacion(rs.getString("pais_operacion"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setActivo(rs.getBoolean("activo"));

        return proveedor;
    }
}