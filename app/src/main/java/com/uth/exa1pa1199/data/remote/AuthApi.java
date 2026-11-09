package com.uth.exa1pa1199.data.remote;

import com.uth.exa1pa1199.data.remote.dto.CredencialesDto;
import com.uth.exa1pa1199.data.remote.dto.TokenDto;
import com.uth.exa1pa1199.data.remote.dto.UsuarioDto;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {
    @POST("api/auth/login")
    Call<TokenDto> login(@Body CredencialesDto credenciales);

    @POST("api/auth/registro")
    Call<UsuarioDto> registrar(@Body CredencialesDto credenciales);
}
