package com.horizontes.dao;

import com.horizontes.model.PaqueteTuristico;
import com.horizontes.model.Proveedor;
import com.horizontes.model.ServicioPaquete;
import com.horizontes.model.TipoProveedor;
import com.horizontes.util.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServicioPaqueteDAO {

    private static final String FIND_ALL_BY_PAQUETE = """
        SELECT 
            sp.id_servicio_paquete,
            sp.descripcion,
            sp.costo,
            p.id_paquete,
            p.nombre AS nombre_paquete,
            pr.id_proveedor,
            pr.nombre AS nombre_proveedor,
            pr.pais_operacion,
            pr.contacto,
            pr.activo,
            tp.id_tipo_proveedor,
            tp.nombre AS nombre_tipo
        FROM servicio_paquete sp
        INNER JOIN paquete_turistico p ON sp.id_paquete = p.id_paquete
        INNER JOIN proveedor pr ON sp.id_proveedor = pr.id_proveedor
        INNER JOIN tipo_proveedor tp ON pr.id_tipo_proveedor = tp.id_tipo_proveedor
        WHERE sp.id_paquete = ?
        ORDER BY sp.id_servicio_paquete ASC
        """;

    private static final String FIND_BY_ID = """
        SELECT 
            sp.id_servicio_paquete,
            sp.descripcion,
            sp.costo,
            p.id_paquete,
            p.nombre AS nombre_paquete,
            pr.id_proveedor,
            pr.nombre AS nombre_proveedor,
            pr.pais_operacion,
            pr.contacto,
            pr.activo,
            tp.id_tipo_proveedor,
            tp.nombre AS nombre_tipo
        FROM servicio_paquete sp
        INNER JOIN paquete_turistico p ON sp.id_paquete = p.id_paquete
        INNER JOIN proveedor pr ON sp.id_proveedor = pr.id_proveedor
        INNER JOIN tipo_proveedor tp ON pr.id_tipo_proveedor = tp.id_tipo_proveedor
        WHERE sp.id_servicio_paquete = ?
        """;

    private static final String INSERT = """
        INSERT INTO servicio_paquete (id_paquete, id_proveedor, descripcion, costo)
        VALUES (?, ?, ?, ?)
        """;

    private static final String UPDATE = """
        UPDATE servicio_paquete
        SET id_paquete = ?, id_proveedor = ?, descripcion = ?, costo = ?
        WHERE id_servicio_paquete = ?
        """;

    private static final String DELETE = """
        DELETE FROM servicio_paquete
        WHERE id_servicio_paquete = ?
        """;

    private static final String RESUMEN_COSTOS = """
        SELECT
            p.id_paquete,
            p.nombre,
            p.precio_venta,
            COALESCE(SUM(sp.costo), 0) AS costo_total,
            (p.precio_venta - COALESCE(SUM(sp.costo), 0)) AS ganancia_bruta
        FROM paquete_turistico p
        LEFT JOIN servicio_paquete sp ON p.id_paquete = sp.id_paquete
        WHERE p.id_paquete = ?
        GROUP BY p.id_paquete, p.nombre, p.precio_venta
        """;

    public List<ServicioPaquete> findAllByPaquete(int idPaquete) throws SQLException {
        List<ServicioPaquete> servicios = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ALL_BY_PAQUETE)) {

            statement.setInt(1, idPaquete);

            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    servicios.add(mapResultSetToServicio(rs));
                }
            }
        }

        return servicios;
    }

    public ServicioPaquete findById(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_BY_ID)) {

            statement.setInt(1, id);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToServicio(rs);
                }
            }
        }

        return null;
    }

    public boolean insert(ServicioPaquete servicio) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(INSERT)) {

            statement.setInt(1, servicio.getPaquete().getIdPaquete());
            statement.setInt(2, servicio.getProveedor().getIdProveedor());
            statement.setString(3, servicio.getDescripcion());
            statement.setDouble(4, servicio.getCosto());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean update(ServicioPaquete servicio) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(UPDATE)) {

            statement.setInt(1, servicio.getPaquete().getIdPaquete());
            statement.setInt(2, servicio.getProveedor().getIdProveedor());
            statement.setString(3, servicio.getDescripcion());
            statement.setDouble(4, servicio.getCosto());
            statement.setInt(5, servicio.getIdServicioPaquete());

            return statement.executeUpdate() > 0;
        }
    }

    public boolean delete(int id) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(DELETE)) {

            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        }
    }

    public ResumenCostosDTO getResumenCostos(int idPaquete) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(RESUMEN_COSTOS)) {

            statement.setInt(1, idPaquete);

            try (ResultSet rs = statement.executeQuery()) {
                if (rs.next()) {
                    return new ResumenCostosDTO(
                            rs.getInt("id_paquete"),
                            rs.getString("nombre"),
                            rs.getDouble("precio_venta"),
                            rs.getDouble("costo_total"),
                            rs.getDouble("ganancia_bruta")
                    );
                }
            }
        }

        return null;
    }

    private ServicioPaquete mapResultSetToServicio(ResultSet rs) throws SQLException {
        PaqueteTuristico paquete = new PaqueteTuristico();
        paquete.setIdPaquete(rs.getInt("id_paquete"));
        paquete.setNombre(rs.getString("nombre_paquete"));

        TipoProveedor tipo = new TipoProveedor();
        tipo.setIdTipoProveedor(rs.getInt("id_tipo_proveedor"));
        tipo.setNombre(rs.getString("nombre_tipo"));

        Proveedor proveedor = new Proveedor();
        proveedor.setIdProveedor(rs.getInt("id_proveedor"));
        proveedor.setNombre(rs.getString("nombre_proveedor"));
        proveedor.setPaisOperacion(rs.getString("pais_operacion"));
        proveedor.setContacto(rs.getString("contacto"));
        proveedor.setActivo(rs.getBoolean("activo"));
        proveedor.setTipoProveedor(tipo);

        ServicioPaquete servicio = new ServicioPaquete();
        servicio.setIdServicioPaquete(rs.getInt("id_servicio_paquete"));
        servicio.setPaquete(paquete);
        servicio.setProveedor(proveedor);
        servicio.setDescripcion(rs.getString("descripcion"));
        servicio.setCosto(rs.getDouble("costo"));

        return servicio;
    }

    public static class ResumenCostosDTO {
        private final int idPaquete;
        private final String nombrePaquete;
        private final double precioVenta;
        private final double costoTotal;
        private final double gananciaBruta;

        public ResumenCostosDTO(int idPaquete, String nombrePaquete, double precioVenta, double costoTotal, double gananciaBruta) {
            this.idPaquete = idPaquete;
            this.nombrePaquete = nombrePaquete;
            this.precioVenta = precioVenta;
            this.costoTotal = costoTotal;
            this.gananciaBruta = gananciaBruta;
        }

        public int getIdPaquete() {
            return idPaquete;
        }

        public String getNombrePaquete() {
            return nombrePaquete;
        }

        public double getPrecioVenta() {
            return precioVenta;
        }

        public double getCostoTotal() {
            return costoTotal;
        }

        public double getGananciaBruta() {
            return gananciaBruta;
        }
    }
}