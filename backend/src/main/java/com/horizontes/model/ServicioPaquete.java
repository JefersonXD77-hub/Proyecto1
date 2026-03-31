package com.horizontes.model;

public class ServicioPaquete {

    private int idServicioPaquete;
    private PaqueteTuristico paquete;
    private Proveedor proveedor;
    private String descripcion;
    private double costo;

    public ServicioPaquete() {
    }

    public int getIdServicioPaquete() {
        return idServicioPaquete;
    }

    public void setIdServicioPaquete(int idServicioPaquete) {
        this.idServicioPaquete = idServicioPaquete;
    }

    public PaqueteTuristico getPaquete() {
        return paquete;
    }

    public void setPaquete(PaqueteTuristico paquete) {
        this.paquete = paquete;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public void setProveedor(Proveedor proveedor) {
        this.proveedor = proveedor;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getCosto() {
        return costo;
    }

    public void setCosto(double costo) {
        this.costo = costo;
    }
}