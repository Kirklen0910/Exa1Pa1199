package com.uth.exa1pa1199.data.comparativa_fernando;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ClienteOkHttpFernando {
    private static final String ETIQUETA = "ComparativaOkHttp";
    private static final Type TIPO_LISTA = new TypeToken<List<RegistroResumenDto>>() {
    }.getType();
    private static final long TIEMPO_ESPERA_SEGUNDOS = 10;

    private final OkHttpClient cliente = new OkHttpClient.Builder()
            .connectTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .readTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .writeTimeout(TIEMPO_ESPERA_SEGUNDOS, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build();

    private final Handler principal = new Handler(Looper.getMainLooper());
    private final SessionManager sesion;
    private final Gson gson = new Gson();

    public ClienteOkHttpFernando(SessionManager sesion) {
        this.sesion = sesion;
    }

    public Call consultar(RutaComparativa ruta, CallbackComparativa callback) {
        long inicio = SystemClock.elapsedRealtime();

        Request.Builder constructor = new Request.Builder()
                .url(BuildConfig.API_BASE_URL + ruta.getRuta())
                .header("Accept", "application/json")
                .get();

        String token = sesion.getToken();
        if (token != null && !token.isEmpty()) {
            constructor.header("Authorization", "Bearer " + token);
        }

        Call llamada = cliente.newCall(constructor.build());
        llamada.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException error) {
                if (call.isCanceled()) {
                    return;
                }
                long ms = SystemClock.elapsedRealtime() - inicio;
                String detalle = error.getClass().getSimpleName()
                        + (error.getMessage() == null ? "" : ": " + error.getMessage());
                Log.w(ETIQUETA, detalle, error);
                publicar(callback, ResultadoComparativa.error(
                        LibreriaComparativa.OKHTTP, ms, 0, detalle, mensajeDeRed(error)));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                try (Response respuesta = response) {
                    int codigo = respuesta.code();
                    if (!respuesta.isSuccessful()) {
                        String detalle = "HTTP " + codigo + " " + respuesta.message();
                        Log.w(ETIQUETA, detalle);
                        publicar(callback, ResultadoComparativa.error(
                                LibreriaComparativa.OKHTTP, ms, codigo, detalle,
                                MensajesComparativa.porCodigo(codigo)));
                        return;
                    }

                    ResponseBody cuerpo = respuesta.body();
                    String json = cuerpo == null ? "[]" : cuerpo.string();
                    List<RegistroResumenDto> registros = gson.fromJson(json, TIPO_LISTA);
                    int cantidad = registros == null ? 0 : registros.size();
                    publicar(callback, ResultadoComparativa.exito(
                            LibreriaComparativa.OKHTTP, cantidad, ms));
                } catch (JsonParseException e) {
                    Log.w(ETIQUETA, "La respuesta no se pudo convertir a RegistroResumenDto", e);
                    publicar(callback, ResultadoComparativa.error(
                            LibreriaComparativa.OKHTTP, ms, 200,
                            "JsonParseException", MensajesComparativa.FORMATO));
                } catch (IOException e) {
                    Log.w(ETIQUETA, "No se pudo leer el cuerpo de la respuesta", e);
                    publicar(callback, ResultadoComparativa.error(
                            LibreriaComparativa.OKHTTP, ms, 0,
                            e.getClass().getSimpleName(), MensajesComparativa.SIN_CONEXION));
                }
            }
        });
        return llamada;
    }

    private void publicar(CallbackComparativa callback, ResultadoComparativa resultado) {
        principal.post(() -> callback.onResultado(resultado));
    }

    private String mensajeDeRed(IOException error) {
        if (error instanceof SocketTimeoutException) {
            return MensajesComparativa.TIEMPO_AGOTADO;
        }
        if (error instanceof UnknownHostException || error instanceof ConnectException) {
            return MensajesComparativa.SIN_CONEXION;
        }
        return MensajesComparativa.SIN_CONEXION;
    }
}
