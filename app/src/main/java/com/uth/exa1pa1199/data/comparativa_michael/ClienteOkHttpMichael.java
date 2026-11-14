package com.uth.exa1pa1199.data.comparativa_michael;

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
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class ClienteOkHttpMichael {
    private static final String TAG = "ComparativaMichael_OkHttp";
    private static final Type LISTA_REGISTROS_TYPE = new TypeToken<List<RegistroResumenDto>>() {}.getType();

    private final OkHttpClient client;
    private final Gson gson = new Gson();

    public ClienteOkHttpMichael() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .build();
    }

    public void consultar(String endpointRelativo, boolean forzarTokenInvalido,
                          ClienteVolleyMichael.CallbackComparativa callback) {
        long inicio = SystemClock.elapsedRealtime();
        String url = BuildConfig.API_BASE_URL + endpointRelativo;

        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .header("Accept", "application/json")
                .get();

        String token = forzarTokenInvalido
                ? "token.jwt.invalido"
                : SessionManager.get().getToken();
        if (token != null && !token.isEmpty()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        Request request = requestBuilder.build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                long tiempoMs = SystemClock.elapsedRealtime() - inicio;
                String errorTecnico = e.getClass().getSimpleName() + ": " + e.getMessage();
                Log.e(TAG, "Fallo de red en OkHttp: " + errorTecnico, e);

                String mensajeUsuario;
                if (e instanceof SocketTimeoutException) {
                    mensajeUsuario = "El servidor tardó demasiado en responder (Tiempo de espera agotado).";
                } else {
                    mensajeUsuario = "No fue posible conectar con el servidor. Verifique su conexión o el estado de la API.";
                }

                callback.onError(e.getClass().getSimpleName(), mensajeUsuario, tiempoMs, 0);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                // También se ejecuta en hilo secundario (background thread)
                long tiempoMs = SystemClock.elapsedRealtime() - inicio;
                try (Response res = response) {
                    int codigoHttp = res.code();
                    if (!res.isSuccessful()) {
                        String errorTecnico = "HTTP " + codigoHttp + " " + res.message();
                        Log.e(TAG, "Respuesta de error HTTP en OkHttp: " + errorTecnico);
                        String mensajeUsuario = traducirCodigoHttp(codigoHttp);
                        callback.onError(errorTecnico.trim(), mensajeUsuario, tiempoMs, codigoHttp);
                        return;
                    }

                    ResponseBody body = res.body();
                    String json = (body != null) ? body.string() : "[]";
                    List<RegistroResumenDto> lista = gson.fromJson(json, LISTA_REGISTROS_TYPE);
                    int cantidad = (lista != null) ? lista.size() : 0;
                    Log.i(TAG, "Éxito OkHttp: " + cantidad + " registros en " + tiempoMs + " ms");
                    callback.onExito(cantidad, tiempoMs, codigoHttp);
                } catch (IOException e) {
                    Log.e(TAG, "Error leyendo el cuerpo de la respuesta en OkHttp", e);
                    callback.onError(
                            e.getClass().getSimpleName(),
                            "Error de lectura al recibir los datos del servidor.",
                            tiempoMs,
                            0
                    );
                } catch (JsonParseException e) {
                    Log.e(TAG, "Error convirtiendo JSON con Gson en OkHttp", e);
                    callback.onError(
                            e.getClass().getSimpleName(),
                            "La respuesta del servidor no tiene un formato JSON válido.",
                            tiempoMs,
                            200
                    );
                }
            }
        });
    }

    private String traducirCodigoHttp(int codigo) {
        if (codigo == 401) {
            return "Sesión expirada o no autorizada (HTTP 401). Inicie sesión nuevamente.";
        } else if (codigo == 403) {
            return "No tiene permisos para consultar estos registros (HTTP 403).";
        } else if (codigo >= 500) {
            return "El servidor presentó un fallo interno (HTTP " + codigo + "). Intente más tarde.";
        }
        return "Error del servidor al procesar la solicitud (HTTP " + codigo + ").";
    }
}
