package com.uth.exa1pa1199.data.remote.dto;

public class RegistroRequestDto {
    private final String titulo;
    private final String descripcion;
    private final String fotoBase64;

    public RegistroRequestDto(String titulo, String descripcion, String fotoBase64) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.fotoBase64 = fotoBase64;
    }

    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getFotoBase64() { return fotoBase64; }
}
