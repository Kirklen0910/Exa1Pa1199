
package com.uth.exa1pa1199.data.comparativa_jennifer;

import android.content.Context;
import android.os.SystemClock;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.data.session.SessionManager;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;

public class ClienteVolleyJennifer {

    public interface ResultadoCallback {
        void onExito(int cantidad, long tiempoMs);
        void onError(String mensaje, long tiempoMs);
    }

    private final RequestQueue cola;

    public ClienteVolleyJennifer(Context context) {
        cola = Volley.newRequestQueue(context.getApplicationContext());
    }

    public void consultarRegistros(ResultadoCallback callback) {

        String token = SessionManager.get().getToken();

        if (token == null || token.trim().isEmpty()) {
            callback.onError("No hay token JWT. Inicia sesión.", 0);
            return;
        }

        String url = BuildConfig.API_BASE_URL + "api/registros";
        long inicio = SystemClock.elapsedRealtime();

        JsonArrayRequest solicitud = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                respuesta -> {
                    long tiempo = SystemClock.elapsedRealtime() - inicio;
                    callback.onExito(respuesta.length(), tiempo);
                },
                error -> {
                    long tiempo = SystemClock.elapsedRealtime() - inicio;
                    callback.onError(describirError(error), tiempo);
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Authorization", "Bearer " + token);
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        solicitud.setRetryPolicy(
                new DefaultRetryPolicy(
                        10000,
                        0,
                        1.0f
                )
        );

        cola.add(solicitud);
    }

    private String describirError(VolleyError error) {

        if (error.networkResponse != null) {
            int codigo = error.networkResponse.statusCode;

            if (codigo == 401) {
                return "HTTP 401: Token ausente, inválido o vencido.";
            }

            if (codigo == 500) {
                return "HTTP 500: Error interno del servidor.";
            }

            return "Error HTTP " + codigo;
        }

        if (error instanceof com.android.volley.TimeoutError) {
            return "Timeout: el servidor tardó demasiado en responder.";
        }

        if (error instanceof com.android.volley.NoConnectionError) {
            return "Sin conexión con el servidor o sin Internet.";
        }

        return "Error de red: " + error.getClass().getSimpleName();
    }
}
