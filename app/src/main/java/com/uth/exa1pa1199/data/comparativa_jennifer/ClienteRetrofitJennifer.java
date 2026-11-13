
package com.uth.exa1pa1199.data.comparativa_jennifer;

import android.os.SystemClock;

import com.uth.exa1pa1199.data.remote.ApiClient;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClienteRetrofitJennifer {

    public interface ResultadoCallback {
        void onExito(int cantidad, long tiempoMs);
        void onError(String mensaje, long tiempoMs);
    }

    public void consultarRegistros(ResultadoCallback callback) {

        String token = SessionManager.get().getToken();

        if (token == null || token.trim().isEmpty()) {
            callback.onError("No hay token JWT. Inicia sesión.", 0);
            return;
        }

        long inicio = SystemClock.elapsedRealtime();

        ApiClient.getApi().listar().enqueue(
                new Callback<List<RegistroResumenDto>>() {

                    @Override
                    public void onResponse(
                            Call<List<RegistroResumenDto>> call,
                            Response<List<RegistroResumenDto>> response) {

                        long tiempo = SystemClock.elapsedRealtime() - inicio;

                        if (response.isSuccessful()) {
                            List<RegistroResumenDto> registros = response.body();

                            if (registros == null) {
                                callback.onError(
                                        "La API devolvió una respuesta vacía.",
                                        tiempo
                                );
                                return;
                            }

                            callback.onExito(registros.size(), tiempo);
                            return;
                        }

                        int codigo = response.code();
                        String mensaje;

                        if (codigo == 401) {
                            mensaje = "HTTP 401: Token inválido o vencido.";
                        } else if (codigo == 500) {
                            mensaje = "HTTP 500: Error interno del servidor.";
                        } else {
                            mensaje = "Error HTTP " + codigo;
                        }

                        callback.onError(mensaje, tiempo);
                    }

                    @Override
                    public void onFailure(
                            Call<List<RegistroResumenDto>> call,
                            Throwable error) {

                        long tiempo = SystemClock.elapsedRealtime() - inicio;

                        String mensaje;

                        if (error instanceof SocketTimeoutException) {
                            mensaje = "Timeout: el servidor tardó demasiado.";
                        } else {
                            mensaje = "Error de conexión: "
                                    + error.getClass().getSimpleName();
                        }

                        callback.onError(mensaje, tiempo);
                    }
                }
        );
    }
}

