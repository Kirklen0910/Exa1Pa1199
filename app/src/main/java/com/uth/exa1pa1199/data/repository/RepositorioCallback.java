package com.uth.exa1pa1199.data.repository;

public interface RepositorioCallback<T> {
    void onExito(T datos);

    void onError(ApiError error);
}
