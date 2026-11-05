package com.uth.exa1pa1199.data.remote.dto;

/**
 * Elemento devuelto por GET /api/registros.
 * El id se declara como String para aceptar tanto un entero como un GUID (Gson convierte números a String).
 */
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
