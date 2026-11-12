package com.uth.exa1pa1199.data.comparativa;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
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

public class ClienteVolley {
    private static final String ETIQUETA = "ClienteVolley";
    private static final int TIEMPO_ESPERA_MS = 10_000;
    private static final int REINTENTOS = 1;
    private static final Type TIPO_LISTA = new TypeToken<List<RegistroResumenDto>>() { }.getType();

    private static volatile RequestQueue cola;

    private final SessionManager sesion;
    private final Gson gson = new Gson();

    public ClienteVolley(Context context, SessionManager sesion) {
        this.sesion = sesion;
        if (cola == null) {
            synchronized (ClienteVolley.class) {
                if (cola == null) cola = Volley.newRequestQueue(context.getApplicationContext());
            }
        }
    }

    public void consultar(RutaPrueba ruta, ConsultaCallback callback) {
        long inicio = SystemClock.elapsedRealtime();
        String url = BuildConfig.API_BASE_URL + ruta.getRuta();

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                respuesta -> {
                    long ms = SystemClock.elapsedRealtime() - inicio;
                    try {
                        List<RegistroResumenDto> registros = gson.fromJson(respuesta.toString(), TIPO_LISTA);
                        int cantidad = registros == null ? 0 : registros.size();
                        callback.onResultado(ResultadoConsulta.exito(Libreria.VOLLEY, cantidad, ms));
                    } catch (JsonParseException e) {
                        Log.w(ETIQUETA, "Respuesta con formato inesperado", e);
                        callback.onResultado(ResultadoConsulta.error(Libreria.VOLLEY, ms, 200,
                                e.getClass().getSimpleName(), MensajesError.FORMATO));
                    }
                },
                error -> callback.onResultado(convertirError(error, SystemClock.elapsedRealtime() - inicio))) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> encabezados = new HashMap<>();
                encabezados.put("Accept", "application/json");
                String token = sesion.getToken();
                if (token != null) encabezados.put("Authorization", "Bearer " + token);
                return encabezados;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIEMPO_ESPERA_MS, REINTENTOS, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        request.setShouldCache(false);
        request.setTag(ETIQUETA);
        cola.add(request);
    }

    public void cancelar() {
        if (cola != null) cola.cancelAll(ETIQUETA);
    }

    private ResultadoConsulta convertirError(VolleyError error, long ms) {
        NetworkResponse respuesta = error.networkResponse;
        int codigo = respuesta != null ? respuesta.statusCode : 0;
        String tecnico = error.getClass().getSimpleName() + (codigo > 0 ? " (HTTP " + codigo + ")" : "");
        Log.w(ETIQUETA, tecnico, error);

        String mensaje;
        if (error instanceof TimeoutError) {
            mensaje = MensajesError.TIEMPO_AGOTADO;
        } else if (error instanceof NoConnectionError || codigo == 0 && !(error instanceof ParseError)) {
            mensaje = MensajesError.SIN_CONEXION;
        } else if (error instanceof ParseError) {
            mensaje = MensajesError.FORMATO;
        } else {
            mensaje = MensajesError.porCodigo(codigo);
        }
        return ResultadoConsulta.error(Libreria.VOLLEY, ms, codigo, tecnico, mensaje);
    }
}
