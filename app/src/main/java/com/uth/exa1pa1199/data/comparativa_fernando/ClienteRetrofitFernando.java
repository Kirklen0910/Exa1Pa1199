package com.uth.exa1pa1199.data.comparativa_fernando;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonParseException;
import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.data.remote.PruebasApi;
import com.uth.exa1pa1199.data.remote.RegistroApi;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ClienteRetrofitFernando {
    private static final String ETIQUETA = "ComparativaRetrofit";
    private static final long TIEMPO_ESPERA_SEGUNDOS = 10;

    private final RegistroRepository repositorio;
    private final PruebasApi pruebasApi;

    public ClienteRetrofitFernando(SessionManager sesion) {
        OkHttpClient clienteHttp = new OkHttpClient.Builder()
                .connectTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
                .readTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
                .writeTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
                .retryOnConnectionFailure(false)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    String token = sesion.getToken();
                    Request peticion = token == null || token.isEmpty()
                            ? original
                            : original.newBuilder()
                                    .header("Authorization", "Bearer " + token)
                                    .build();
                    return chain.proceed(peticion);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(clienteHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegistroApi api = retrofit.create(RegistroApi.class);
        this.pruebasApi = retrofit.create(PruebasApi.class);
        this.repositorio = new RegistroRepository(api);
    }

    public Cancelable consultar(RutaComparativa ruta, CallbackComparativa callback) {
        long inicio = SystemClock.elapsedRealtime();

        if (ruta == RutaComparativa.REGISTROS) {
            return repositorio.obtenerRegistros(new RepositorioCallback<List<RegistroResumenDto>>() {
                @Override
                public void onExito(List<RegistroResumenDto> datos) {
                    long ms = SystemClock.elapsedRealtime() - inicio;
                    callback.onResultado(ResultadoComparativa.exito(
                            LibreriaComparativa.RETROFIT, datos == null ? 0 : datos.size(), ms));
                }

                @Override
                public void onError(ApiError error) {
                    long ms = SystemClock.elapsedRealtime() - inicio;
                    String detalle = "ApiError." + error.getTipo().name()
                            + (error.getCodigoHttp() > 0 ? " · HTTP " + error.getCodigoHttp() : "");
                    Log.w(ETIQUETA, detalle + " | " + error.getMensaje());
                    callback.onResultado(ResultadoComparativa.error(
                            LibreriaComparativa.RETROFIT, ms, error.getCodigoHttp(),
                            detalle, error.getMensaje()));
                }
            });
        }

        Call<List<RegistroResumenDto>> llamada = pruebasApi.consultar(ruta.getRuta());
        llamada.enqueue(new Callback<List<RegistroResumenDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<RegistroResumenDto>> call,
                                   @NonNull Response<List<RegistroResumenDto>> respuesta) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                if (respuesta.isSuccessful()) {
                    List<RegistroResumenDto> datos = respuesta.body();
                    callback.onResultado(ResultadoComparativa.exito(
                            LibreriaComparativa.RETROFIT, datos == null ? 0 : datos.size(), ms));
                    return;
                }

                int codigo = respuesta.code();
                String detalle = "HTTP " + codigo + " " + respuesta.message();
                Log.w(ETIQUETA, detalle);
                callback.onResultado(ResultadoComparativa.error(
                        LibreriaComparativa.RETROFIT, ms, codigo, detalle,
                        MensajesComparativa.porCodigo(codigo)));
            }

            @Override
            public void onFailure(@NonNull Call<List<RegistroResumenDto>> call,
                                  @NonNull Throwable error) {
                if (call.isCanceled()) {
                    return;
                }
                long ms = SystemClock.elapsedRealtime() - inicio;
                String detalle = error.getClass().getSimpleName()
                        + (error.getMessage() == null ? "" : ": " + error.getMessage());
                Log.w(ETIQUETA, detalle, error);
                callback.onResultado(ResultadoComparativa.error(
                        LibreriaComparativa.RETROFIT, ms, 0, detalle, mensajeDeError(error)));
            }
        });
        return llamada::cancel;
    }

    private String mensajeDeError(Throwable error) {
        if (error instanceof SocketTimeoutException) {
            return MensajesComparativa.TIEMPO_AGOTADO;
        }
        if (error instanceof JsonParseException) {
            return MensajesComparativa.FORMATO;
        }
        if (error instanceof IOException) {
            return MensajesComparativa.SIN_CONEXION;
        }
        return "Ocurrió un error inesperado: " + error.getClass().getSimpleName();
    }
}
