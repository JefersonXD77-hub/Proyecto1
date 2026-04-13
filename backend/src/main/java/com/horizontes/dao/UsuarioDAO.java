package com.horizontes.dao;

import com.horizontes.model.Rol;
import com.horizontes.model.Usuario;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private static final String FIND_ALL = """
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
        ORDER BY u.id_usuario ASC
        """;

    private static final String FIND_BY_ID = """
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
        WHERE u.id_usuario = ?
        """;

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

    private static final String INSERT = """
        INSERT INTO usuario (username, password_hash, nombre_completo, correo, id_rol, activo)
        VALUES (?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE_WITH_PASSWORD = """
        UPDATE usuario
        SET username = ?, password_hash = ?, nombre_completo = ?, correo = ?, id_rol = ?, activo = ?
        WHERE id_usuario = ?
        """;

    private static final String UPDATE_WITHOUT_PASSWORD = """
        UPDATE usuario
        SET username = ?, nombre_completo = ?, correo = ?, id_rol = ?, activo = ?
        WHERE id_usuario = ?
        """;

    private static final String SOFT_DELETE = """
        UPDATE usuario
        SET activo = FALSE
        WHERE id_usuario = ?
        """;

    public List<Usuario> findAll() throws SQLException {
        List<Usuario> usuarios = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
        }

        return usuarios;
    }

    public Usuario findById(int idUsuario) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, idUsuario);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }

        return null;
    }

    public Usuario findByUsername(String username) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_USERNAME)) {

            statement.setString(1, username);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUsuario(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(Usuario usuario) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setString(1, usuario.getUsername());
            statement.setString(2, usuario.getPasswordHash());
            statement.setString(3, usuario.getNombreCompleto());
            statement.setString(4, usuario.getCorreo());
            statement.setInt(5, usuario.getRol().getIdRol());
            statement.setBoolean(6, usuario.isActivo());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Usuario usuario, boolean updatePassword) throws SQLException {
        String sql = updatePassword ? UPDATE_WITH_PASSWORD : UPDATE_WITHOUT_PASSWORD;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            int index = 1;
            statement.setString(index++, usuario.getUsername());

            if (updatePassword) {
                statement.setString(index++, usuario.getPasswordHash());
            }

            statement.setString(index++, usuario.getNombreCompleto());
            statement.setString(index++, usuario.getCorreo());
            statement.setInt(index++, usuario.getRol().getIdRol());
            statement.setBoolean(index++, usuario.isActivo());
            statement.setInt(index, usuario.getIdUsuario());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean softDelete(int idUsuario) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SOFT_DELETE)) {

            statement.setInt(1, idUsuario);
            return statement.executeUpdate() > 0;
        }
    }

    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
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
