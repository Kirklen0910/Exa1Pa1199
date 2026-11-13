package com.uth.exa1pa1199.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uth.exa1pa1199.BuildConfig;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.remote.ApiClient;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Response;

public class ComparativaRomelActivity extends AppCompatActivity {

    private static final String TAG = "ComparativaRomel";

    private static final int TIMEOUT_MS = 20000;

    private static final String RUTA_NORMAL = "api/registros";
    private static final String RUTA_LENTA = "api/pruebas/demora";
    private static final String RUTA_ERROR = "api/pruebas/error";

    private TextView txtResultado;
    private RadioGroup grupoEscenario;
    private RequestQueue colaVolley;

    private final OkHttpClient clienteOk = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .readTimeout(TIMEOUT_MS, TimeUnit.MILLISECONDS)
            .build();

    private final Map<String, List<Long>> tiempos = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa_romel);

        txtResultado = findViewById(R.id.txtResultado);
        grupoEscenario = findViewById(R.id.grupoEscenario);
        colaVolley = Volley.newRequestQueue(this);

        Button btnVolley = findViewById(R.id.btnVolley);
        Button btnOkHttp = findViewById(R.id.btnOkHttp);
        Button btnRetrofit = findViewById(R.id.btnRetrofit);

        btnVolley.setOnClickListener(v -> consultarVolley());
        btnOkHttp.setOnClickListener(v -> consultarOkHttp());
        btnRetrofit.setOnClickListener(v -> consultarRetrofit());
    }

    private void consultarVolley() {
        mostrarCargando("Volley");
        final long inicio = SystemClock.elapsedRealtime();

        JsonArrayRequest peticion = new JsonArrayRequest(
                com.android.volley.Request.Method.GET, urlCompleta(), null,
                respuesta -> mostrarOk("Volley", respuesta.length(),
                        SystemClock.elapsedRealtime() - inicio),
                error -> mostrarError("Volley", mensajeVolley(error),
                        SystemClock.elapsedRealtime() - inicio)) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> cabeceras = new HashMap<>();
                cabeceras.put("Authorization", "Bearer " + obtenerToken());
                return cabeceras;
            }
        };

        peticion.setRetryPolicy(new DefaultRetryPolicy(TIMEOUT_MS, 1, 1f));
        colaVolley.add(peticion);
    }

    private String mensajeVolley(VolleyError error) {
        if (error instanceof TimeoutError) {
            return "Tiempo de espera agotado";
        } else if (error instanceof NoConnectionError) {
            return "Sin conexión o servidor detenido";
        } else if (error instanceof AuthFailureError) {
            return "401: token vencido o ausente";
        } else if (error instanceof ServerError) {
            int codigo = error.networkResponse != null ? error.networkResponse.statusCode : 500;
            return "Error del servidor (" + codigo + ")";
        } else if (error instanceof ParseError) {
            return "Respuesta con formato inválido";
        }
        return "Error de red: " + error.getMessage();
    }

    private void consultarOkHttp() {
        mostrarCargando("OkHttp");
        final long inicio = SystemClock.elapsedRealtime();

        okhttp3.Request peticion = new okhttp3.Request.Builder()
                .url(urlCompleta())
                .addHeader("Authorization", "Bearer " + obtenerToken())
                .build();

        clienteOk.newCall(peticion).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                final long ms = SystemClock.elapsedRealtime() - inicio;
                final String mensaje = mensajeError(e);
                runOnUiThread(() -> mostrarError("OkHttp", mensaje, ms));
            }

            @Override
            public void onResponse(Call call, Response response) {
                final long ms = SystemClock.elapsedRealtime() - inicio;
                try (Response r = response) {
                    if (!r.isSuccessful()) {
                        final String mensaje = mensajeHttp(r.code());
                        runOnUiThread(() -> mostrarError("OkHttp", mensaje, ms));
                        return;
                    }
                    String json = r.body() != null ? r.body().string() : "";
                    Type tipo = new TypeToken<List<RegistroResumenDto>>() { }.getType();
                    List<RegistroResumenDto> lista = new Gson().fromJson(json, tipo);
                    final int cantidad = lista == null ? 0 : lista.size();
                    runOnUiThread(() -> mostrarOk("OkHttp", cantidad, ms));
                } catch (IOException e) {
                    final String mensaje = mensajeError(e);
                    runOnUiThread(() -> mostrarError("OkHttp", mensaje, ms));
                } catch (Exception e) {
                    runOnUiThread(() ->
                            mostrarError("OkHttp", "Respuesta con formato inválido", ms));
                }
            }
        });
    }

    private void consultarRetrofit() {
        mostrarCargando("Retrofit");
        final long inicio = SystemClock.elapsedRealtime();

        ApiClient.getPruebasApi().consultar(rutaSeleccionada()).enqueue(
                new retrofit2.Callback<List<RegistroResumenDto>>() {
                    @Override
                    public void onResponse(
                            retrofit2.Call<List<RegistroResumenDto>> call,
                            retrofit2.Response<List<RegistroResumenDto>> response) {
                        long ms = SystemClock.elapsedRealtime() - inicio;
                        if (response.isSuccessful() && response.body() != null) {
                            mostrarOk("Retrofit", response.body().size(), ms);
                        } else {
                            mostrarError("Retrofit", mensajeHttp(response.code()), ms);
                        }
                    }

                    @Override
                    public void onFailure(
                            retrofit2.Call<List<RegistroResumenDto>> call, Throwable t) {
                        long ms = SystemClock.elapsedRealtime() - inicio;
                        mostrarError("Retrofit", mensajeError(t), ms);
                    }
                });
    }

    private String obtenerToken() {
        String token = SessionManager.get().getToken();
        return token == null ? "" : token;
    }

    private String rutaSeleccionada() {
        int id = grupoEscenario.getCheckedRadioButtonId();
        if (id == R.id.rbLento) return RUTA_LENTA;
        if (id == R.id.rbError) return RUTA_ERROR;
        return RUTA_NORMAL;
    }

    private String urlCompleta() {
        String base = BuildConfig.API_BASE_URL;
        if (!base.endsWith("/")) base = base + "/";
        return base + rutaSeleccionada();
    }

    private String mensajeHttp(int codigo) {
        if (codigo == 401) return "401: token vencido o ausente";
        if (codigo >= 500 && codigo <= 599) return "Error del servidor (" + codigo + ")";
        return "Error HTTP " + codigo;
    }

    private String mensajeError(Throwable e) {
        if (e instanceof SocketTimeoutException) return "Tiempo de espera agotado";
        if (e instanceof IOException) return "Sin conexión o servidor detenido";
        return "Respuesta inválida o error inesperado";
    }

    private void mostrarCargando(String libreria) {
        txtResultado.setText(libreria + ": consultando...");
    }

    private void mostrarOk(String libreria, int cantidad, long ms) {
        List<Long> lista = tiempos.get(libreria);
        if (lista == null) {
            lista = new ArrayList<>();
            tiempos.put(libreria, lista);
        }
        lista.add(ms);
        long suma = 0;
        for (long t : lista) suma += t;
        long promedio = suma / lista.size();

        Log.d(TAG, libreria + " OK: " + cantidad + " registros en " + ms + " ms");
        txtResultado.setText(libreria + "\nRegistros: " + cantidad
                + "\nTiempo: " + ms + " ms"
                + "\nPromedio (" + lista.size() + " ejecuciones): " + promedio + " ms");
    }

    private void mostrarError(String libreria, String mensaje, long ms) {
        Log.e(TAG, libreria + " ERROR: " + mensaje + " (" + ms + " ms)");
        txtResultado.setText(libreria + "\nError: " + mensaje + "\nTiempo: " + ms + " ms");
    }
}