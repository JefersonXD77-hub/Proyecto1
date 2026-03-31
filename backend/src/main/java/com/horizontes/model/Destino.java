package com.horizontes.model;

public class Destino {

    private int idDestino;
    private String nombre;
    private String pais;
    private String descripcion;
    private String climaEpoca;
    private String urlImagen;
    private boolean activo;

    public Destino() {
    }

    public int getIdDestino() {
        return idDestino;
    }

    public void setIdDestino(int idDestino) {
        this.idDestino = idDestino;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getPais() {
        return pais;
    }

    public void setPais(String pais) {
        this.pais = pais;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getClimaEpoca() {
        return climaEpoca;
    }

    public void setClimaEpoca(String climaEpoca) {
        this.climaEpoca = climaEpoca;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
