package com.uth.exa1pa1199.data.comparativa_fernando;

/**
 * Endpoints que se pueden consultar desde la pantalla de laboratorio.
 * Las dos rutas de prueba existen solo en Development en la API.
 */
public enum RutaComparativa {
    REGISTROS("api/registros", "GET /api/registros"),
    DEMORA("api/pruebas/demora", "GET /api/pruebas/demora (30 s)"),
    ERROR_500("api/pruebas/error", "GET /api/pruebas/error (500)");

    private final String ruta;
    private final String descripcion;

    RutaComparativa(String ruta, String descripcion) {
        this.ruta = ruta;
        this.descripcion = descripcion;
    }

    public String getRuta() {
        return ruta;
    }

    public String getDescripcion() {
        return descripcion;
    }
}
