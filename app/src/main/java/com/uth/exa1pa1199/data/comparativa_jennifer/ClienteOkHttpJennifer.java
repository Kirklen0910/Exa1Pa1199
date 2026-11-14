
package com.uth.exa1pa1199.data.comparativa_jennifer;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import com.google.gson.JsonParser;
import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClienteOkHttpJennifer {

    public interface ResultadoCallback {
        void onExito(int cantidad, long tiempoMs);
        void onError(String mensaje, long tiempoMs);
    }

    private final OkHttpClient cliente = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    private final Handler principal = new Handler(Looper.getMainLooper());

    public void consultarRegistros(ResultadoCallback callback) {

        String token = SessionManager.get().getToken();

        if (token == null || token.trim().isEmpty()) {
            principal.post(() ->
                    callback.onError("No hay token JWT. Inicia sesión.", 0));
            return;
        }

        Request solicitud = new Request.Builder()
                .url(BuildConfig.API_BASE_URL + "api/registros")
                .header("Authorization", "Bearer " + token)
                .header("Accept", "application/json")
                .get()
                .build();

        long inicio = SystemClock.elapsedRealtime();

        cliente.newCall(solicitud).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException error) {
                long tiempo = SystemClock.elapsedRealtime() - inicio;

                String mensaje = error instanceof java.net.SocketTimeoutException
                        ? "Timeout: el servidor tardó demasiado."
                        : "Error de conexión: " + error.getClass().getSimpleName();

                principal.post(() -> callback.onError(mensaje, tiempo));
            }

            @Override
            public void onResponse(Call call, Response response) {
                long tiempo = SystemClock.elapsedRealtime() - inicio;

                try (Response respuesta = response) {

                    if (!respuesta.isSuccessful()) {
                        int codigo = respuesta.code();

                        String mensaje;
                        if (codigo == 401) {
                            mensaje = "HTTP 401: Token inválido o vencido.";
                        } else if (codigo == 500) {
                            mensaje = "HTTP 500: Error interno del servidor.";
                        } else {
                            mensaje = "Error HTTP " + codigo;
                        }

                        principal.post(() -> callback.onError(mensaje, tiempo));
                        return;
                    }

                    String json = respuesta.body() != null
                            ? respuesta.body().string()
                            : "[]";

                    int cantidad = JsonParser.parseString(json)
                            .getAsJsonArray()
                            .size();

                    principal.post(() -> callback.onExito(cantidad, tiempo));

                } catch (Exception error) {
                    long tiempoError = SystemClock.elapsedRealtime() - inicio;
                    principal.post(() -> callback.onError(
                            "Error al procesar JSON: " + error.getMessage(),
                            tiempoError
                    ));
                }
            }
        });
    }
}
