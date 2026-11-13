
package com.uth.exa1pa1199.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.comparativa_jennifer.ClienteVolleyJennifer;
import com.uth.exa1pa1199.data.comparativa_jennifer.ClienteOkHttpJennifer;
import com.uth.exa1pa1199.data.comparativa_jennifer.ClienteRetrofitJennifer;

public class ComparativaJenniferActivity extends AppCompatActivity {

    private Button btnVolley;
    private Button btnOkHttp;
    private Button btnRetrofit;
    private TextView txtResultado;
    private ProgressBar progressBar;

    private boolean consultando = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comparativa_jennifer);

        btnVolley = findViewById(R.id.btnVolleyJennifer);
        btnOkHttp = findViewById(R.id.btnOkHttpJennifer);
        btnRetrofit = findViewById(R.id.btnRetrofitJennifer);
        txtResultado = findViewById(R.id.txtResultadoJennifer);
        progressBar = findViewById(R.id.progressJennifer);

        progressBar.setVisibility(View.GONE);

        btnVolley.setOnClickListener(v -> consultarVolley());
        btnOkHttp.setOnClickListener(v -> consultarOkHttp());
        btnRetrofit.setOnClickListener(v -> consultarRetrofit());
    }

    private void iniciarConsulta(String biblioteca) {
        consultando = true;

        btnVolley.setEnabled(false);
        btnOkHttp.setEnabled(false);
        btnRetrofit.setEnabled(false);

        progressBar.setVisibility(View.VISIBLE);

        txtResultado.setText(
                "Consultando registros con " + biblioteca + "..."
        );
    }

    private void finalizarConsulta(String mensaje) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        consultando = false;

        btnVolley.setEnabled(true);
        btnOkHttp.setEnabled(true);
        btnRetrofit.setEnabled(true);

        progressBar.setVisibility(View.GONE);
        txtResultado.setText(mensaje);
    }

    private void mostrarExito(
            String biblioteca,
            int cantidad,
            long tiempoMs) {

        finalizarConsulta(
                "Biblioteca: " + biblioteca
                        + "\nEstado: ÉXITO"
                        + "\nRegistros encontrados: " + cantidad
                        + "\nTiempo de respuesta: " + tiempoMs + " ms"
        );
    }

    private void mostrarError(
            String biblioteca,
            String error,
            long tiempoMs) {

        finalizarConsulta(
                "Biblioteca: " + biblioteca
                        + "\nEstado: ERROR"
                        + "\nDetalle: " + error
                        + "\nTiempo transcurrido: " + tiempoMs + " ms"
        );
    }

    private void consultarVolley() {
        if (consultando) return;

        iniciarConsulta("Volley");

        ClienteVolleyJennifer cliente =
                new ClienteVolleyJennifer(this);

        cliente.consultarRegistros(
                new ClienteVolleyJennifer.ResultadoCallback() {

                    @Override
                    public void onExito(int cantidad, long tiempoMs) {
                        runOnUiThread(() ->
                                mostrarExito("Volley", cantidad, tiempoMs));
                    }

                    @Override
                    public void onError(String mensaje, long tiempoMs) {
                        runOnUiThread(() ->
                                mostrarError("Volley", mensaje, tiempoMs));
                    }
                }
        );
    }

    private void consultarOkHttp() {
        if (consultando) return;

        iniciarConsulta("OkHttp");

        ClienteOkHttpJennifer cliente =
                new ClienteOkHttpJennifer();

        cliente.consultarRegistros(
                new ClienteOkHttpJennifer.ResultadoCallback() {

                    @Override
                    public void onExito(int cantidad, long tiempoMs) {
                        runOnUiThread(() ->
                                mostrarExito("OkHttp", cantidad, tiempoMs));
                    }

                    @Override
                    public void onError(String mensaje, long tiempoMs) {
                        runOnUiThread(() ->
                                mostrarError("OkHttp", mensaje, tiempoMs));
                    }
                }
        );
    }

    private void consultarRetrofit() {
        if (consultando) return;

        iniciarConsulta("Retrofit");

        ClienteRetrofitJennifer cliente =
                new ClienteRetrofitJennifer();

        cliente.consultarRegistros(
                new ClienteRetrofitJennifer.ResultadoCallback() {

                    @Override
                    public void onExito(int cantidad, long tiempoMs) {
                        runOnUiThread(() ->
                                mostrarExito("Retrofit", cantidad, tiempoMs));
                    }

                    @Override
                    public void onError(String mensaje, long tiempoMs) {
                        runOnUiThread(() ->
                                mostrarError("Retrofit", mensaje, tiempoMs));
                    }
                }
        );
    }
}
