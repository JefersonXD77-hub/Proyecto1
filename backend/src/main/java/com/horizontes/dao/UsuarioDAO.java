package com.horizontes.dao;

import com.horizontes.model.Rol;
import com.horizontes.model.Usuario;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {

    private static final String FIND_BY_USERNAME = """
        SELECT 
            u.id_usuario,
            u.username,
            u.password_hash,
            u.nombre_completo,
            u.correo,
            u.activo,
            u.fecha_creacion,
            r.id_rol,
            r.nombre AS nombre_rol
        FROM usuario u
        INNER JOIN rol r ON u.id_rol = r.id_rol
        WHERE u.username = ?
        """;

    public Usuario findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    Rol rol = new Rol();
                    rol.setIdRol(rs.getInt("id_rol"));
                    rol.setNombre(rs.getString("nombre_rol"));

                    Usuario usuario = new Usuario();
                    usuario.setIdUsuario(rs.getInt("id_usuario"));
                    usuario.setUsername(rs.getString("username"));
                    usuario.setPasswordHash(rs.getString("password_hash"));
                    usuario.setNombreCompleto(rs.getString("nombre_completo"));
                    usuario.setCorreo(rs.getString("correo"));
                    usuario.setActivo(rs.getBoolean("activo"));
                    usuario.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
                    usuario.setRol(rol);

                    return usuario;
                }
            }
        }

        return null;
    }
}
