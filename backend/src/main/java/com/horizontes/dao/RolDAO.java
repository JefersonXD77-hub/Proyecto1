package com.horizontes.dao;

import com.horizontes.model.Rol;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RolDAO {

    private static final String FIND_ALL = """
        SELECT id_rol, nombre
        FROM rol
        ORDER BY id_rol ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT id_rol, nombre
        FROM rol
        WHERE id_rol = ?
        """;

    public List<Rol> findAll() throws SQLException {
        List<Rol> roles = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                Rol rol = new Rol();
                rol.setIdRol(rs.getInt("id_rol"));
                rol.setNombre(rs.getString("nombre"));
                roles.add(rol);
            }
        }

        return roles;
    }

    public Rol findById(int idRol) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, idRol);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Rol rol = new Rol();
                    rol.setIdRol(rs.getInt("id_rol"));
                    rol.setNombre(rs.getString("nombre"));
                    return rol;
                }
            }
        }

        return null;
    }
}
