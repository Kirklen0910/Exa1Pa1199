package com.uth.exa1pa1199.ui;

import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.uth.exa1pa1199.BuildConfig;

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

import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;

public class ComparativaFernandoAbadActivity extends AppCompatActivity {

    private static final String PATH = "api/registros";

    private TextView tv;
    private RequestQueue queue;
    private final Gson gson = new Gson();
    private final Map<String, List<Long>> tiempos = new HashMap<>();
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build();

    private String url() { return BuildConfig.API_BASE_URL + PATH; }

    private String token() {
        String t = com.uth.exa1pa1199.data.session.SessionManager.get().getToken();
        return t != null ? t : "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(this);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(32, 96, 32, 32);

        Button bVolley = new Button(this);
        bVolley.setText("Volley");
        bVolley.setOnClickListener(v -> volley());

        Button bOk = new Button(this);
        bOk.setText("OkHttp");
        bOk.setOnClickListener(v -> okhttp());

        Button bRetro = new Button(this);
        bRetro.setText("Retrofit");
        bRetro.setOnClickListener(v -> retrofit());

        tv = new TextView(this);
        tv.setTextSize(16f);

        root.addView(bVolley);
        root.addView(bOk);
        root.addView(bRetro);
        root.addView(tv);
        setContentView(root);
    }

    private void mostrar(String lib, String msg, long t0) {
        long ms = SystemClock.elapsedRealtime() - t0;
        List<Long> l = tiempos.get(lib);
        if (l == null) { l = new ArrayList<>(); tiempos.put(lib, l); }
        l.add(ms);
        long suma = 0;
        for (long x : l) suma += x;
        tv.setText(lib + ": " + msg + "\n" + ms + " ms\nPromedio: "
                + (suma / l.size()) + " ms (" + l.size() + " ejec.)");
        Log.d("COMPARATIVA", lib + " | " + msg + " | " + ms + " ms");
    }

    private void volley() {
        final long t0 = SystemClock.elapsedRealtime();
        JsonArrayRequest req = new JsonArrayRequest(Request.Method.GET, url(), null,
                r -> mostrar("Volley", r.length() + " registros", t0),
                e -> {
                    Log.e("COMPARATIVA", "Volley error", e);
                    String msg;
                    if (e instanceof TimeoutError) msg = "Tiempo de espera agotado";
                    else if (e instanceof NoConnectionError) msg = "Sin conexión con el servidor";
                    else if (e instanceof AuthFailureError) msg = "Sesión inválida o vencida (401)";
                    else if (e instanceof ServerError && e.networkResponse != null)
                        msg = "Error del servidor (" + e.networkResponse.statusCode + ")";
                    else msg = "Error: " + e.getMessage();
                    mostrar("Volley", msg, t0);
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> h = new HashMap<>();
                h.put("Authorization", "Bearer " + token());
                return h;
            }
        };
        req.setRetryPolicy(new DefaultRetryPolicy(5000, 1, 1f));
        queue.add(req);
    }

    private void okhttp() {
        final long t0 = SystemClock.elapsedRealtime();
        okhttp3.Request req = new okhttp3.Request.Builder()
                .url(url())
                .header("Authorization", "Bearer " + token())
                .build();

        client.newCall(req).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("COMPARATIVA", "OkHttp error", e);
                final String msg = (e instanceof SocketTimeoutException)
                        ? "Tiempo de espera agotado" : "Sin conexión con el servidor";
                runOnUiThread(() -> mostrar("OkHttp", msg, t0));
            }

            @Override
            public void onResponse(Call call, Response response) {
                String m;
                try (Response r = response) {
                    int code = r.code();
                    String body = r.body() != null ? r.body().string() : "";
                    if (code == 401) m = "Sesión inválida o vencida (401)";
                    else if (code >= 500) m = "Error del servidor (" + code + ")";
                    else if (!r.isSuccessful()) m = "Error HTTP " + code;
                    else {
                        Type tipo = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        List<Map<String, Object>> lista = gson.fromJson(body, tipo);
                        m = lista.size() + " registros";
                    }
                } catch (Exception ex) {
                    m = "Respuesta inválida: " + ex.getMessage();
                }
                final String msg = m;
                runOnUiThread(() -> mostrar("OkHttp", msg, t0));
            }
        });
    }

    private void retrofit() {
        final long t0 = SystemClock.elapsedRealtime();
        new RegistroRepository().obtenerRegistros(new RepositorioCallback<List<RegistroResumenDto>>() {
            @Override
            public void onExito(List<RegistroResumenDto> datos) {
                mostrar("Retrofit", datos.size() + " registros", t0);
            }

            @Override
            public void onError(ApiError error) {
                Log.e("COMPARATIVA", "Retrofit error: " + error.getTipo() + " " + error.getCodigoHttp());
                mostrar("Retrofit", error.getTipo() + " (" + error.getCodigoHttp() + "): " + error.getMensaje(), t0);
            }
        });
    }
}