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

/** Los cinco endpoints de la API de la Bitácora de campo (tarea 2.2). */
public interface RegistroApi {

    /** 200 OK con la lista (sin foto, solo el indicador tieneFoto). */
    @GET("api/registros")
    Call<List<RegistroResumenDto>> listar();

    /** 200 OK con el registro completo, incluida fotoBase64. 404 si no existe. */
    @GET("api/registros/{id}")
    Call<RegistroDetalleDto> obtener(@Path("id") String id);

    /** 201 Created. 400 con ValidationProblemDetails si los datos no son válidos. */
    @POST("api/registros")
    Call<RegistroDetalleDto> crear(@Body RegistroRequestDto registro);

    /** 204 No Content. 400 si no es válido, 404 si no existe. */
    @PUT("api/registros/{id}")
    Call<Void> actualizar(@Path("id") String id, @Body RegistroRequestDto registro);

    /** 204 No Content. 404 si no existe. */
    @DELETE("api/registros/{id}")
    Call<Void> eliminar(@Path("id") String id);
}
