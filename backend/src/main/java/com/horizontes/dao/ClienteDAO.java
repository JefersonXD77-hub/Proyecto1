package com.horizontes.dao;

import com.horizontes.model.Cliente;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClienteDAO {

    private static final String FIND_ALL = """
        SELECT id_cliente, dpi_pasaporte, nombre_completo, fecha_nacimiento, telefono, email, nacionalidad, activo
        FROM cliente
        ORDER BY id_cliente ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT id_cliente, dpi_pasaporte, nombre_completo, fecha_nacimiento, telefono, email, nacionalidad, activo
        FROM cliente
        WHERE id_cliente = ?
        """;

    private static final String FIND_BY_DPI_PASAPORTE = """
        SELECT id_cliente, dpi_pasaporte, nombre_completo, fecha_nacimiento, telefono, email, nacionalidad, activo
        FROM cliente
        WHERE dpi_pasaporte = ?
        """;

    private static final String INSERT = """
        INSERT INTO cliente (dpi_pasaporte, nombre_completo, fecha_nacimiento, telefono, email, nacionalidad, activo)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

    private static final String UPDATE = """
        UPDATE cliente
        SET dpi_pasaporte = ?, nombre_completo = ?, fecha_nacimiento = ?, telefono = ?, email = ?, nacionalidad = ?, activo = ?
        WHERE id_cliente = ?
        """;

    private static final String SOFT_DELETE = """
        UPDATE cliente
        SET activo = FALSE
        WHERE id_cliente = ?
        """;

    public List<Cliente> findAll() throws SQLException {
        List<Cliente> clientes = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
        }

        return clientes;
    }

    public Cliente findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        }

        return null;
    }

    public Cliente findByDpiPasaporte(String dpiPasaporte) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_DPI_PASAPORTE)) {

            statement.setString(1, dpiPasaporte);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToCliente(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(Cliente cliente) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setString(1, cliente.getDpiPasaporte());
            statement.setString(2, cliente.getNombreCompleto());
            statement.setDate(3, cliente.getFechaNacimiento());
            statement.setString(4, cliente.getTelefono());
            statement.setString(5, cliente.getEmail());
            statement.setString(6, cliente.getNacionalidad());
            statement.setBoolean(7, cliente.isActivo());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(Cliente cliente) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setString(1, cliente.getDpiPasaporte());
            statement.setString(2, cliente.getNombreCompleto());
            statement.setDate(3, cliente.getFechaNacimiento());
            statement.setString(4, cliente.getTelefono());
            statement.setString(5, cliente.getEmail());
            statement.setString(6, cliente.getNacionalidad());
            statement.setBoolean(7, cliente.isActivo());
            statement.setInt(8, cliente.getIdCliente());

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

    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getInt("id_cliente"));
        cliente.setDpiPasaporte(rs.getString("dpi_pasaporte"));
        cliente.setNombreCompleto(rs.getString("nombre_completo"));
        cliente.setFechaNacimiento(rs.getDate("fecha_nacimiento"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        cliente.setNacionalidad(rs.getString("nacionalidad"));
        cliente.setActivo(rs.getBoolean("activo"));
        return cliente;
    }
}