package com.uth.exa1pa1199.data.comparativa_fernando;

import android.content.Context;
import android.os.SystemClock;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
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

public class ClienteVolleyFernando {
    private static final String ETIQUETA = "ComparativaVolley";
    private static final int TIEMPO_ESPERA_MS = 10_000;
    private static final int REINTENTOS = 1;
    private static final Type TIPO_LISTA = new TypeToken<List<RegistroResumenDto>>() {
    }.getType();

    private static volatile RequestQueue cola;

    private final SessionManager sesion;
    private final Gson gson = new Gson();

    public ClienteVolleyFernando(Context contexto, SessionManager sesion) {
        this.sesion = sesion;
        if (cola == null) {
            synchronized (ClienteVolleyFernando.class) {
                if (cola == null) {
                    cola = Volley.newRequestQueue(contexto.getApplicationContext());
                }
            }
        }
    }

    public void consultar(RutaComparativa ruta, CallbackComparativa callback) {
        long inicio = SystemClock.elapsedRealtime();
        String url = BuildConfig.API_BASE_URL + ruta.getRuta();

        JsonArrayRequest solicitud = new JsonArrayRequest(
                Request.Method.GET,
                url,
                null,
                respuesta -> {
                    long ms = SystemClock.elapsedRealtime() - inicio;
                    try {
                        List<RegistroResumenDto> registros =
                                gson.fromJson(respuesta.toString(), TIPO_LISTA);
                        int cantidad = registros == null ? 0 : registros.size();
                        callback.onResultado(ResultadoComparativa.exito(
                                LibreriaComparativa.VOLLEY, cantidad, ms));
                    } catch (JsonParseException e) {
                        Log.w(ETIQUETA, "La respuesta no se pudo convertir a RegistroResumenDto", e);
                        callback.onResultado(ResultadoComparativa.error(
                                LibreriaComparativa.VOLLEY, ms, 200,
                                "JsonParseException", MensajesComparativa.FORMATO));
                    }
                },
                error -> callback.onResultado(describirError(error, SystemClock.elapsedRealtime() - inicio))
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> encabezados = new HashMap<>();
                encabezados.put("Accept", "application/json");
                String token = sesion.getToken();
                if (token != null && !token.isEmpty()) {
                    encabezados.put("Authorization", "Bearer " + token);
                }
                return encabezados;
            }
        };

        solicitud.setRetryPolicy(new DefaultRetryPolicy(
                TIEMPO_ESPERA_MS,
                REINTENTOS,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        solicitud.setShouldCache(false);
        solicitud.setTag(ETIQUETA);
        cola.add(solicitud);
    }

    public void cancelar() {
        if (cola != null) {
            cola.cancelAll(ETIQUETA);
        }
    }

    private ResultadoComparativa describirError(VolleyError error, long ms) {
        NetworkResponse respuesta = error.networkResponse;
        int codigo = respuesta != null ? respuesta.statusCode : 0;
        String detalle = detalleTecnico(error, codigo);
        Log.w(ETIQUETA, detalle, error);

        String mensaje;
        if (error instanceof TimeoutError) {
            mensaje = MensajesComparativa.TIEMPO_AGOTADO;
        } else if (error instanceof NoConnectionError) {
            mensaje = MensajesComparativa.SIN_CONEXION;
        } else if (error instanceof AuthFailureError || codigo == 401) {
            mensaje = MensajesComparativa.porCodigo(401);
        } else if (error instanceof ServerError || codigo >= 500) {
            mensaje = MensajesComparativa.porCodigo(codigo >= 500 ? codigo : 500);
        } else if (error instanceof ParseError) {
            mensaje = MensajesComparativa.FORMATO;
        } else if (error instanceof NetworkError) {
            mensaje = codigo > 0 ? MensajesComparativa.porCodigo(codigo)
                    : MensajesComparativa.SIN_CONEXION;
        } else {
            mensaje = codigo > 0 ? MensajesComparativa.porCodigo(codigo)
                    : "Ocurrió un error inesperado con Volley.";
        }
        return ResultadoComparativa.error(
                LibreriaComparativa.VOLLEY, ms, codigo, detalle, mensaje);
    }

    private String detalleTecnico(VolleyError error, int codigo) {
        Throwable causa = error.getCause();
        String sufijo = causa == null ? "" : " · causa=" + causa.getClass().getSimpleName();
        return error.getClass().getSimpleName()
                + (codigo > 0 ? " · HTTP " + codigo : "")
                + sufijo;
    }
}
