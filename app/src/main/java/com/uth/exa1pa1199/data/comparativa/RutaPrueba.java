package com.uth.exa1pa1199.data.comparativa;

public enum RutaPrueba {
    REGISTROS("api/registros"),
    DEMORA("api/pruebas/demora"),
    ERROR_SERVIDOR("api/pruebas/error");

    private final String ruta;

    RutaPrueba(String ruta) {
        this.ruta = ruta;
    }

    public String getRuta() { return ruta; }
}
