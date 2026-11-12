package com.uth.exa1pa1199.data.remote;

import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

public interface PruebasApi {
    @GET
    Call<List<RegistroResumenDto>> consultar(@Url String ruta);
}
