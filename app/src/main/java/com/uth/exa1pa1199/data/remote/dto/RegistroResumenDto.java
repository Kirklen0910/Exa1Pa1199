package com.uth.exa1pa1199.data.remote.dto;

public class RegistroResumenDto {
    private String id;
    private String titulo;
    private String descripcion;
    private String fechaCreacion;
    private boolean tieneFoto;

    public String getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFechaCreacion() { return fechaCreacion; }
    public boolean isTieneFoto() { return tieneFoto; }
}
