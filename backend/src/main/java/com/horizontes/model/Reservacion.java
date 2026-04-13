package com.horizontes.model;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.List;

public class Reservacion {

    private int idReservacion;
    private String numeroReservacion;
    private Timestamp fechaCreacion;
    private Date fechaViaje;
    private PaqueteTuristico paquete;
    private Usuario agente;
    private int cantidadPasajeros;
    private double costoTotal;
    private String estado;
    private boolean activo;
    private List<Cliente> pasajeros;

    public Reservacion() {
    }

    public int getIdReservacion() {
        return idReservacion;
    }

    public void setIdReservacion(int idReservacion) {
        this.idReservacion = idReservacion;
    }

    public String getNumeroReservacion() {
        return numeroReservacion;
    }

    public void setNumeroReservacion(String numeroReservacion) {
        this.numeroReservacion = numeroReservacion;
    }

    public Timestamp getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Timestamp fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaViaje() {
        return fechaViaje;
    }

    public void setFechaViaje(Date fechaViaje) {
        this.fechaViaje = fechaViaje;
    }

    public PaqueteTuristico getPaquete() {
        return paquete;
    }

    public void setPaquete(PaqueteTuristico paquete) {
        this.paquete = paquete;
    }

    public Usuario getAgente() {
        return agente;
    }

    public void setAgente(Usuario agente) {
        this.agente = agente;
    }

    public int getCantidadPasajeros() {
        return cantidadPasajeros;
    }

    public void setCantidadPasajeros(int cantidadPasajeros) {
        this.cantidadPasajeros = cantidadPasajeros;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<Cliente> getPasajeros() {
        return pasajeros;
    }

    public void setPasajeros(List<Cliente> pasajeros) {
        this.pasajeros = pasajeros;
    }
}
