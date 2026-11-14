package com.uth.exa1pa1199.data.comparativa_fernando;

public enum LibreriaComparativa {
    VOLLEY("Volley"),
    OKHTTP("OkHttp"),
    RETROFIT("Retrofit");

    private final String nombre;

    LibreriaComparativa(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }
}
