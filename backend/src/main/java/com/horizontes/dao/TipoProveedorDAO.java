package com.horizontes.dao;

import com.horizontes.model.TipoProveedor;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TipoProveedorDAO {

    private static final String FIND_ALL = """
        SELECT id_tipo_proveedor, nombre
        FROM tipo_proveedor
        ORDER BY id_tipo_proveedor ASC
        """;

    public List<TipoProveedor> findAll() throws SQLException {
        List<TipoProveedor> tipos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                TipoProveedor tipo = new TipoProveedor();
                tipo.setIdTipoProveedor(rs.getInt("id_tipo_proveedor"));
                tipo.setNombre(rs.getString("nombre"));
                tipos.add(tipo);
            }
        }

        return tipos;
    }
}