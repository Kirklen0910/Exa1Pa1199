package com.uth.exa1pa1199.ui;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.comparativa_michael.ClienteOkHttpMichael;
import com.uth.exa1pa1199.data.comparativa_michael.ClienteRetrofitMichael;
import com.uth.exa1pa1199.data.comparativa_michael.ClienteVolleyMichael;

public class ComparativaMichaelActivity extends AppCompatActivity {

    private RadioGroup rgEscenario;
    private Button btnVolley;
    private Button btnOkHttp;
    private Button btnRetrofit;
    private ProgressBar progressBar;
    private TextView txtResultado;

    private ClienteVolleyMichael clienteVolley;
    private ClienteOkHttpMichael clienteOkHttp;
    private ClienteRetrofitMichael clienteRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa_michael);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Comparativa (Michael Abad)");
        }

        rgEscenario = findViewById(R.id.rgEscenarioMichael);
        btnVolley = findViewById(R.id.btnVolleyMichael);
        btnOkHttp = findViewById(R.id.btnOkHttpMichael);
        btnRetrofit = findViewById(R.id.btnRetrofitMichael);
        progressBar = findViewById(R.id.progressMichael);
        txtResultado = findViewById(R.id.txtResultadoMichael);

        clienteVolley = new ClienteVolleyMichael(this);
        clienteOkHttp = new ClienteOkHttpMichael();
        clienteRetrofit = new ClienteRetrofitMichael();

        btnVolley.setOnClickListener(v -> ejecutarConVolley());
        btnOkHttp.setOnClickListener(v -> ejecutarConOkHttp());
        btnRetrofit.setOnClickListener(v -> ejecutarConRetrofit());
    }

    private String obtenerEndpointSeleccionado() {
        int id = rgEscenario.getCheckedRadioButtonId();
        if (id == R.id.rbDemoraMichael) {
            return "api/pruebas/demora";
        } else if (id == R.id.rbError500Michael) {
            return "api/pruebas/error";
        }
        return "api/registros";
    }

    private boolean esEscenarioTokenInvalido() {
        return rgEscenario.getCheckedRadioButtonId() == R.id.rbTokenInvalidoMichael;
    }

    private void ejecutarConVolley() {
        mostrarCarga("Volley");
        String endpoint = obtenerEndpointSeleccionado();
        boolean tokenInvalido = esEscenarioTokenInvalido();

        clienteVolley.consultar(endpoint, tokenInvalido, new ClienteVolleyMichael.CallbackComparativa() {
            @Override
            public void onExito(int cantidadRegistros, long tiempoMs, int codigoHttp) {
                boolean esHiloPrincipal = Looper.myLooper() == Looper.getMainLooper();
                ocultarCarga();
                mostrarExito("Volley", cantidadRegistros, tiempoMs, codigoHttp,
                        "Main Thread (" + esHiloPrincipal + ")");
            }

            @Override
            public void onError(String errorTecnico, String mensajeUsuario, long tiempoMs, int codigoHttp) {
                ocultarCarga();
                mostrarError("Volley", errorTecnico, mensajeUsuario, tiempoMs, codigoHttp);
            }
        });
    }

    private void ejecutarConOkHttp() {
        mostrarCarga("OkHttp");
        String endpoint = obtenerEndpointSeleccionado();
        boolean tokenInvalido = esEscenarioTokenInvalido();

        clienteOkHttp.consultar(endpoint, tokenInvalido, new ClienteVolleyMichael.CallbackComparativa() {
            @Override
            public void onExito(int cantidadRegistros, long tiempoMs, int codigoHttp) {
                String hiloRespuesta = Thread.currentThread().getName();
                runOnUiThread(() -> {
                    ocultarCarga();
                    mostrarExito("OkHttp", cantidadRegistros, tiempoMs, codigoHttp,
                            "Background (" + hiloRespuesta + ") -> runOnUiThread");
                });
            }

            @Override
            public void onError(String errorTecnico, String mensajeUsuario, long tiempoMs, int codigoHttp) {
                runOnUiThread(() -> {
                    ocultarCarga();
                    mostrarError("OkHttp", errorTecnico, mensajeUsuario, tiempoMs, codigoHttp);
                });
            }
        });
    }

    private void ejecutarConRetrofit() {
        mostrarCarga("Retrofit");
        String endpoint = obtenerEndpointSeleccionado();
        boolean tokenInvalido = esEscenarioTokenInvalido();

        clienteRetrofit.consultar(endpoint, tokenInvalido, new ClienteVolleyMichael.CallbackComparativa() {
            @Override
            public void onExito(int cantidadRegistros, long tiempoMs, int codigoHttp) {
                ocultarCarga();
                mostrarExito("Retrofit", cantidadRegistros, tiempoMs, codigoHttp,
                        "Main Thread (vía MainThreadExecutor)");
            }

            @Override
            public void onError(String errorTecnico, String mensajeUsuario, long tiempoMs, int codigoHttp) {
                ocultarCarga();
                mostrarError("Retrofit", errorTecnico, mensajeUsuario, tiempoMs, codigoHttp);
            }
        });
    }

    private void mostrarCarga(String cliente) {
        progressBar.setVisibility(View.VISIBLE);
        btnVolley.setEnabled(false);
        btnOkHttp.setEnabled(false);
        btnRetrofit.setEnabled(false);
        txtResultado.setText("Consultando con " + cliente + "...");
    }

    private void ocultarCarga() {
        progressBar.setVisibility(View.GONE);
        btnVolley.setEnabled(true);
        btnOkHttp.setEnabled(true);
        btnRetrofit.setEnabled(true);
    }

    private void mostrarExito(String cliente, int cantidad, long tiempoMs, int codigoHttp, String infoHilo) {
        String detalle = "CLIENTE: " + cliente + "\n"
                + "ESTADO: ÉXITO (HTTP " + codigoHttp + ")\n"
                + "REGISTROS RECIBIDOS: " + cantidad + "\n"
                + "TIEMPO DE RESPUESTA: " + tiempoMs + " ms\n"
                + "HILO DE ENTREGA: " + infoHilo;
        txtResultado.setText(detalle);
    }

    private void mostrarError(String cliente, String errorTecnico, String mensajeUsuario, long tiempoMs, int codigoHttp) {
        String detalle = "CLIENTE: " + cliente + "\n"
                + "ESTADO: FALLO" + (codigoHttp > 0 ? " (HTTP " + codigoHttp + ")" : " (Sin respuesta HTTP)") + "\n"
                + "TIEMPO TRANSCURRIDO: " + tiempoMs + " ms\n"
                + "ERROR EN CÓDIGO: " + errorTecnico + "\n\n"
                + "MENSAJE AL USUARIO:\n" + mensajeUsuario;
        txtResultado.setText(detalle);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}