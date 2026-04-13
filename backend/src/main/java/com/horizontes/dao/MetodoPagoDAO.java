package com.horizontes.dao;

import com.horizontes.model.MetodoPago;
import com.horizontes.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MetodoPagoDAO {

    private static final String FIND_ALL = """
        SELECT id_metodo_pago, nombre
        FROM metodo_pago
        ORDER BY id_metodo_pago ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT id_metodo_pago, nombre
        FROM metodo_pago
        WHERE id_metodo_pago = ?
        """;

    public List<MetodoPago> findAll() throws SQLException {
        List<MetodoPago> metodos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL);
             ResultSet rs = statement.executeQuery()) {

            while (rs.next()) {
                MetodoPago metodo = new MetodoPago();
                metodo.setIdMetodoPago(rs.getInt("id_metodo_pago"));
                metodo.setNombre(rs.getString("nombre"));
                metodos.add(metodo);
            }
        }

        return metodos;
    }

    public MetodoPago findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    MetodoPago metodo = new MetodoPago();
                    metodo.setIdMetodoPago(rs.getInt("id_metodo_pago"));
                    metodo.setNombre(rs.getString("nombre"));
                    return metodo;
                }
            }
        }

        return null;
    }
}