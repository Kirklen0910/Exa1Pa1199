package com.uth.exa1pa1199.data.remote.dto;

/** Respuesta de GET /api/registros/{id} (y de POST): incluye la foto en Base64. */
public class RegistroDetalleDto extends RegistroResumenDto {
    private String fotoBase64;

    public String getFotoBase64() { return fotoBase64; }
}
