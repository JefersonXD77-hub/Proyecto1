package com.horizontes.model;

import java.sql.Date;

public class Cancelacion {

    private int idCancelacion;
    private Reservacion reservacion;
    private Date fechaCancelacion;
    private int diasAnticipacion;
    private double porcentajeReembolso;
    private double montoPagado;
    private double montoReembolsado;
    private double perdidaAgencia;
    private Usuario usuarioProceso;

    public Cancelacion() {
    }

    public int getIdCancelacion() {
        return idCancelacion;
    }

    public void setIdCancelacion(int idCancelacion) {
        this.idCancelacion = idCancelacion;
    }

    public Reservacion getReservacion() {
        return reservacion;
    }

    public void setReservacion(Reservacion reservacion) {
        this.reservacion = reservacion;
    }

    public Date getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(Date fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public int getDiasAnticipacion() {
        return diasAnticipacion;
    }

    public void setDiasAnticipacion(int diasAnticipacion) {
        this.diasAnticipacion = diasAnticipacion;
    }

    public double getPorcentajeReembolso() {
        return porcentajeReembolso;
    }

    public void setPorcentajeReembolso(double porcentajeReembolso) {
        this.porcentajeReembolso = porcentajeReembolso;
    }

    public double getMontoPagado() {
        return montoPagado;
    }

    public void setMontoPagado(double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public double getMontoReembolsado() {
        return montoReembolsado;
    }

    public void setMontoReembolsado(double montoReembolsado) {
        this.montoReembolsado = montoReembolsado;
    }

    public double getPerdidaAgencia() {
        return perdidaAgencia;
    }

    public void setPerdidaAgencia(double perdidaAgencia) {
        this.perdidaAgencia = perdidaAgencia;
    }

    public Usuario getUsuarioProceso() {
        return usuarioProceso;
    }

    public void setUsuarioProceso(Usuario usuarioProceso) {
        this.usuarioProceso = usuarioProceso;
    }
}
