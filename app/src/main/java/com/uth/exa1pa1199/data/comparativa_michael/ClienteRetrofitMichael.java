package com.uth.exa1pa1199.data.comparativa_michael;

import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;

import com.uth.exa1pa1199.data.remote.ApiClient;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.net.SocketTimeoutException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClienteRetrofitMichael {
    private static final String TAG = "ComparativaMichael_Retrofit";

    public void consultar(String endpointRelativo, boolean forzarTokenInvalido,
                          ClienteVolleyMichael.CallbackComparativa callback) {
        String tokenOriginal = SessionManager.get().getToken();

        if (!forzarTokenInvalido && (tokenOriginal == null || tokenOriginal.trim().isEmpty())) {
            callback.onError(
                    "TokenAusente",
                    "No hay una sesión JWT activa. Inicie sesión primero.",
                    0,
                    401
            );
            return;
        }

        if (forzarTokenInvalido) {
            SessionManager.get().reemplazarToken("token.jwt.invalido");
        }

        long inicio = SystemClock.elapsedRealtime();

        Call<List<RegistroResumenDto>> llamada = "api/registros".equals(endpointRelativo)
                ? ApiClient.getApi().listar()
                : ApiClient.getPruebasApi().consultar(endpointRelativo);

        llamada.enqueue(new Callback<List<RegistroResumenDto>>() {
            @Override
            public void onResponse(@NonNull Call<List<RegistroResumenDto>> call,
                                   @NonNull Response<List<RegistroResumenDto>> response) {
                restaurarTokenSiNecesario(forzarTokenInvalido, tokenOriginal);
                long tiempoMs = SystemClock.elapsedRealtime() - inicio;

                if (response.isSuccessful()) {
                    List<RegistroResumenDto> registros = response.body();
                    if (registros == null) {
                        callback.onError(
                                "RespuestaVacia (HTTP " + response.code() + ")",
                                "El servidor respondió sin datos en el cuerpo JSON.",
                                tiempoMs,
                                response.code()
                        );
                        return;
                    }
                    Log.i(TAG, "Éxito Retrofit: " + registros.size() + " registros en " + tiempoMs + " ms");
                    callback.onExito(registros.size(), tiempoMs, response.code());
                    return;
                }

                int codigo = response.code();
                String errorTecnico = "HTTP " + codigo + " " + response.message();
                Log.e(TAG, "Error HTTP en Retrofit: " + errorTecnico);
                callback.onError(errorTecnico.trim(), traducirCodigoHttp(codigo), tiempoMs, codigo);
            }

            @Override
            public void onFailure(@NonNull Call<List<RegistroResumenDto>> call, @NonNull Throwable t) {
                restaurarTokenSiNecesario(forzarTokenInvalido, tokenOriginal);
                long tiempoMs = SystemClock.elapsedRealtime() - inicio;
                String errorTecnico = t.getClass().getSimpleName();
                Log.e(TAG, "Fallo de red en Retrofit: " + errorTecnico, t);

                String mensajeUsuario = (t instanceof SocketTimeoutException)
                        ? "El servidor tardó demasiado en responder (Tiempo de espera agotado)."
                        : "No fue posible conectar con el servidor. Verifique su red o el estado de la API.";

                callback.onError(errorTecnico, mensajeUsuario, tiempoMs, 0);
            }
        });
    }

    private void restaurarTokenSiNecesario(boolean forzarTokenInvalido, String tokenOriginal) {
        if (forzarTokenInvalido && tokenOriginal != null) {
            SessionManager.get().reemplazarToken(tokenOriginal);
        }
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