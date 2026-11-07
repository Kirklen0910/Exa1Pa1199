package com.uth.exa1pa1199.data.repository;

/** Resultado asíncrono de una operación del repositorio (se invoca en el hilo principal). */
public interface RepositorioCallback<T> {
    void onExito(T datos);

    void onError(ApiError error);
}
