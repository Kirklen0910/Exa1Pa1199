package com.uth.exa1pa1199.data.comparativa;

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

public class ClienteOkHttp {
    private static final String ETIQUETA = "ClienteOkHttp";
    private static final Type TIPO_LISTA = new TypeToken<List<RegistroResumenDto>>() { }.getType();

    private static final OkHttpClient CLIENTE = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(10, TimeUnit.SECONDS)
            .build();

    private final SessionManager sesion;
    private final Gson gson = new Gson();

    public ClienteOkHttp(SessionManager sesion) {
        this.sesion = sesion;
    }

    public Call consultar(RutaPrueba ruta, ConsultaCallback callback) {
        long inicio = SystemClock.elapsedRealtime();

        Request.Builder builder = new Request.Builder()
                .url(BuildConfig.API_BASE_URL + ruta.getRuta())
                .header("Accept", "application/json")
                .get();
        String token = sesion.getToken();
        if (token != null) builder.header("Authorization", "Bearer " + token);

        Call call = CLIENTE.newCall(builder.build());
        call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call c, @NonNull IOException e) {
                if (c.isCanceled()) return;
                long ms = SystemClock.elapsedRealtime() - inicio;
                String tecnico = e.getClass().getSimpleName();
                Log.w(ETIQUETA, tecnico, e);
                String mensaje = e instanceof SocketTimeoutException
                        ? MensajesError.TIEMPO_AGOTADO
                        : MensajesError.SIN_CONEXION;
                callback.onResultado(ResultadoConsulta.error(Libreria.OKHTTP, ms, 0, tecnico, mensaje));
            }

            @Override
            public void onResponse(@NonNull Call c, @NonNull Response response) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                try (Response r = response) {
                    int codigo = r.code();
                    if (!r.isSuccessful()) {
                        String tecnico = "HTTP " + codigo + " " + r.message();
                        Log.w(ETIQUETA, tecnico);
                        callback.onResultado(ResultadoConsulta.error(Libreria.OKHTTP, ms, codigo,
                                tecnico.trim(), MensajesError.porCodigo(codigo)));
                        return;
                    }
                    ResponseBody cuerpo = r.body();
                    String json = cuerpo != null ? cuerpo.string() : "[]";
                    List<RegistroResumenDto> registros = gson.fromJson(json, TIPO_LISTA);
                    int cantidad = registros == null ? 0 : registros.size();
                    callback.onResultado(ResultadoConsulta.exito(Libreria.OKHTTP, cantidad, ms));
                } catch (IOException e) {
                    Log.w(ETIQUETA, "Error al leer la respuesta", e);
                    callback.onResultado(ResultadoConsulta.error(Libreria.OKHTTP, ms, 0,
                            e.getClass().getSimpleName(), MensajesError.SIN_CONEXION));
                } catch (JsonParseException e) {
                    Log.w(ETIQUETA, "Respuesta con formato inesperado", e);
                    callback.onResultado(ResultadoConsulta.error(Libreria.OKHTTP, ms, 200,
                            e.getClass().getSimpleName(), MensajesError.FORMATO));
                }
            }
        });
        return call;
    }
}
