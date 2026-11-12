package com.uth.exa1pa1199.data.repository;

import androidx.annotation.NonNull;

import com.uth.exa1pa1199.data.remote.ApiClient;
import com.uth.exa1pa1199.data.remote.RegistroApi;
import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroRequestDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroRepository {
    private final RegistroApi api;
    private final ApiErrorMapper errores = new ApiErrorMapper();

    public RegistroRepository() {
        this(ApiClient.getApi());
    }

    public RegistroRepository(RegistroApi api) {
        this.api = api;
    }

    public Cancelable obtenerRegistros(RepositorioCallback<List<RegistroResumenDto>> callback) {
        return ejecutar(api.listar(), callback);
    }

    public Cancelable obtenerRegistrosDesde(String ruta, RepositorioCallback<List<RegistroResumenDto>> callback) {
        return ejecutar(ApiClient.getPruebasApi().consultar(ruta), callback);
    }

    public Cancelable obtenerRegistro(String id, RepositorioCallback<RegistroDetalleDto> callback) {
        return ejecutar(api.obtener(id), callback);
    }

    public Cancelable crearRegistro(RegistroRequestDto registro, RepositorioCallback<RegistroDetalleDto> callback) {
        return ejecutar(api.crear(registro), callback);
    }

    public Cancelable actualizarRegistro(String id, RegistroRequestDto registro, RepositorioCallback<Void> callback) {
        return ejecutar(api.actualizar(id, registro), callback);
    }

    public Cancelable eliminarRegistro(String id, RepositorioCallback<Void> callback) {
        return ejecutar(api.eliminar(id), callback);
    }

    private <T> Cancelable ejecutar(Call<T> call, RepositorioCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> c, @NonNull Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onExito(response.body());
                } else {
                    callback.onError(errores.desdeRespuesta(response, false));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> c, @NonNull Throwable t) {
                if (c.isCanceled()) return;
                callback.onError(errores.desdeExcepcion(t));
            }
        });
        return call::cancel;
    }
}
