package com.uth.exa1pa1199.data.repository;

import androidx.annotation.NonNull;

import com.uth.exa1pa1199.data.remote.ApiClient;
import com.uth.exa1pa1199.data.remote.AuthApi;
import com.uth.exa1pa1199.data.remote.dto.CredencialesDto;
import com.uth.exa1pa1199.data.remote.dto.TokenDto;
import com.uth.exa1pa1199.data.remote.dto.UsuarioDto;
import com.uth.exa1pa1199.data.session.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private final AuthApi api;
    private final SessionManager sesion;
    private final ApiErrorMapper errores = new ApiErrorMapper();

    public AuthRepository() {
        this(ApiClient.getAuthApi(), SessionManager.get());
    }

    public AuthRepository(AuthApi api, SessionManager sesion) {
        this.api = api;
        this.sesion = sesion;
    }

    public Cancelable iniciarSesion(String correo, String password, RepositorioCallback<TokenDto> callback) {
        Call<TokenDto> call = api.login(new CredencialesDto(correo, password));
        call.enqueue(new Callback<TokenDto>() {
            @Override
            public void onResponse(@NonNull Call<TokenDto> c, @NonNull Response<TokenDto> response) {
                TokenDto token = response.body();
                if (response.isSuccessful() && token != null && token.getToken() != null) {
                    sesion.guardar(token.getToken(), token.getExpiracion(), token.getRol(), token.getCorreo());
                    callback.onExito(token);
                } else if (response.isSuccessful()) {
                    callback.onError(new ApiError(ApiError.Tipo.SERVIDOR, response.code(),
                            "El servidor no devolvió el token.", null));
                } else {
                    callback.onError(errores.desdeRespuesta(response, true));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TokenDto> c, @NonNull Throwable t) {
                if (c.isCanceled()) return;
                callback.onError(errores.desdeExcepcion(t));
            }
        });
        return call::cancel;
    }

    public Cancelable registrar(String correo, String password, RepositorioCallback<UsuarioDto> callback) {
        Call<UsuarioDto> call = api.registrar(new CredencialesDto(correo, password));
        call.enqueue(new Callback<UsuarioDto>() {
            @Override
            public void onResponse(@NonNull Call<UsuarioDto> c, @NonNull Response<UsuarioDto> response) {
                if (response.isSuccessful()) {
                    callback.onExito(response.body());
                } else {
                    callback.onError(errores.desdeRespuesta(response, true));
                }
            }

            @Override
            public void onFailure(@NonNull Call<UsuarioDto> c, @NonNull Throwable t) {
                if (c.isCanceled()) return;
                callback.onError(errores.desdeExcepcion(t));
            }
        });
        return call::cancel;
    }

    public void cerrarSesion() {
        sesion.cerrar();
    }
}
