package com.uth.exa1pa1199.data.comparativa;

public enum Libreria {
    VOLLEY("Volley"),
    OKHTTP("OkHttp"),
    RETROFIT("Retrofit");

    private final String nombre;

    Libreria(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() { return nombre; }
}
