package com.uth.exa1pa1199.data.remote;

import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroRequestDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface RegistroApi {
    @GET("api/registros")
    Call<List<RegistroResumenDto>> listar();

    @GET("api/registros/{id}")
    Call<RegistroDetalleDto> obtener(@Path("id") String id);

    @POST("api/registros")
    Call<RegistroDetalleDto> crear(@Body RegistroRequestDto registro);

    @PUT("api/registros/{id}")
    Call<Void> actualizar(@Path("id") String id, @Body RegistroRequestDto registro);

    @DELETE("api/registros/{id}")
    Call<Void> eliminar(@Path("id") String id);
}
