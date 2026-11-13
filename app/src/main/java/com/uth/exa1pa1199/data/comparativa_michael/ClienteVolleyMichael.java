package com.uth.exa1pa1199.data.comparativa_michael;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClienteVolleyMichael {
    private static final String TAG = "ComparativaMichael_Volley";
    private static final int TIMEOUT_MS = 5_000;
    private static final int MAX_RETRIES = 1;
    private static final Type LISTA_REGISTROS_TYPE = new TypeToken<List<RegistroResumenDto>>() {}.getType();

    private final RequestQueue requestQueue;
    private final Gson gson = new Gson();

    public interface CallbackComparativa {
        void onExito(int cantidadRegistros, long tiempoMs, int codigoHttp);
        void onError(String errorTecnico, String mensajeUsuario, long tiempoMs, int codigoHttp);
    }

    public ClienteVolleyMichael(Context context) {
        this.requestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void consultar(String endpointRelativo, boolean forzarTokenInvalido, CallbackComparativa callback) {
        long inicio = SystemClock.elapsedRealtime();
        String url = BuildConfig.API_BASE_URL + endpointRelativo;

        JsonArrayRequest request = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    long tiempoMs = SystemClock.elapsedRealtime() - inicio;
                    try {
                        List<RegistroResumenDto> lista = gson.fromJson(response.toString(), LISTA_REGISTROS_TYPE);
                        int cantidad = (lista != null) ? lista.size() : 0;
                        Log.i(TAG, "Éxito Volley: " + cantidad + " registros en " + tiempoMs + " ms");
                        callback.onExito(cantidad, tiempoMs, 200);
                    } catch (JsonParseException e) {
                        Log.e(TAG, "Error de parseo JSON en Volley", e);
                        callback.onError(
                                e.getClass().getSimpleName(),
                                "La respuesta del servidor no tiene un formato válido.",
                                tiempoMs,
                                200
                        );
                    }
                },
                error -> {
                    long tiempoMs = SystemClock.elapsedRealtime() - inicio;
                    procesarErrorVolley(error, tiempoMs, callback);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                String token = forzarTokenInvalido
                        ? "token.jwt.invalido"
                        : SessionManager.get().getToken();
                if (token != null && !token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        request.setShouldCache(false);
        requestQueue.add(request);
    }

    private void procesarErrorVolley(VolleyError error, long tiempoMs, CallbackComparativa callback) {
        NetworkResponse networkResponse = error.networkResponse;
        int statusCode = (networkResponse != null) ? networkResponse.statusCode : 0;
        String errorTecnico = error.getClass().getSimpleName()
                + (statusCode > 0 ? " (HTTP " + statusCode + ")" : "");
        Log.e(TAG, "Fallo en Volley: " + errorTecnico, error);

        String mensajeUsuario;
        if (error instanceof TimeoutError) {
            mensajeUsuario = "El servidor tardó demasiado en responder (Tiempo de espera agotado).";
        } else if (error instanceof NoConnectionError) {
            mensajeUsuario = "No fue posible conectar con el servidor. Verifique su red o que la API esté encendida.";
        } else if (error instanceof AuthFailureError || statusCode == 401) {
            mensajeUsuario = "Sesión expirada o no autorizada (HTTP 401). Inicie sesión nuevamente.";
        } else if (error instanceof ServerError || statusCode >= 500) {
            mensajeUsuario = "El servidor presentó un fallo interno (HTTP " + statusCode + "). Intente más tarde.";
        } else if (error instanceof ParseError) {
            mensajeUsuario = "Error al procesar los datos recibidos del servidor.";
        } else {
            mensajeUsuario = "Ocurrió un error al consultar los registros (Código: " + statusCode + ").";
        }

        callback.onError(errorTecnico, mensajeUsuario, tiempoMs, statusCode);
    }
}
