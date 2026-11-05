package com.uth.exa1pa1199.data.repository;

/** Permite cancelar una petición en curso sin exponer tipos de Retrofit al ViewModel. */
public interface Cancelable {
    void cancelar();
}
