package com.uth.exa1pa1199.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.comparativa.ClienteOkHttp;
import com.uth.exa1pa1199.data.comparativa.ClienteRetrofit;
import com.uth.exa1pa1199.data.comparativa.ClienteVolley;
import com.uth.exa1pa1199.data.comparativa.ConsultaCallback;
import com.uth.exa1pa1199.data.comparativa.Libreria;
import com.uth.exa1pa1199.data.comparativa.ResultadoConsulta;
import com.uth.exa1pa1199.data.comparativa.RutaPrueba;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;

public class ComparativaActivity extends AppCompatActivity {
    private static final String ETIQUETA = "Comparativa";
    private static final int REPETICIONES = 5;

    private SessionManager sesion;
    private ClienteVolley clienteVolley;
    private ClienteOkHttp clienteOkHttp;
    private ClienteRetrofit clienteRetrofit;

    private Call llamadaOkHttp;
    private Cancelable llamadaRetrofit;

    private View root;
    private RadioGroup grupoRutas;
    private CheckBox checkRepetir;
    private Button botonVolley;
    private Button botonOkHttp;
    private Button botonRetrofit;
    private Button botonAlterarToken;
    private LinearProgressIndicator progreso;
    private TextView textVolley;
    private TextView textOkHttp;
    private TextView textRetrofit;
    private TextView textBitacora;

    private boolean enCurso;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comparativa);
        configurarPantalla();

        sesion = SessionManager.get();
        clienteVolley = new ClienteVolley(this, sesion);
        clienteOkHttp = new ClienteOkHttp(sesion);
        clienteRetrofit = new ClienteRetrofit();

        grupoRutas = findViewById(R.id.grupoRutas);
        checkRepetir = findViewById(R.id.checkRepetir);
        botonVolley = findViewById(R.id.botonVolley);
        botonOkHttp = findViewById(R.id.botonOkHttp);
        botonRetrofit = findViewById(R.id.botonRetrofit);
        botonAlterarToken = findViewById(R.id.botonAlterarToken);
        progreso = findViewById(R.id.progreso);
        textVolley = findViewById(R.id.textResultadoVolley);
        textOkHttp = findViewById(R.id.textResultadoOkHttp);
        textRetrofit = findViewById(R.id.textResultadoRetrofit);
        textBitacora = findViewById(R.id.textBitacora);

        botonVolley.setOnClickListener(v -> iniciar(Libreria.VOLLEY));
        botonOkHttp.setOnClickListener(v -> iniciar(Libreria.OKHTTP));
        botonRetrofit.setOnClickListener(v -> iniciar(Libreria.RETROFIT));
        botonAlterarToken.setOnClickListener(v -> alterarToken());
        findViewById(R.id.botonLimpiar).setOnClickListener(v -> textBitacora.setText(""));
    }

    private void configurarPantalla() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.titulo_comparativa);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        root = findViewById(R.id.root);
        View appBar = findViewById(R.id.appBar);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            appBar.setPadding(0, barras.top, 0, 0);
            v.setPadding(barras.left, 0, barras.right, barras.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private RutaPrueba rutaSeleccionada() {
        int id = grupoRutas.getCheckedRadioButtonId();
        if (id == R.id.radioDemora) return RutaPrueba.DEMORA;
        if (id == R.id.radioError) return RutaPrueba.ERROR_SERVIDOR;
        return RutaPrueba.REGISTROS;
    }

    private void iniciar(Libreria libreria) {
        if (enCurso) return;
        int total = checkRepetir.isChecked() ? REPETICIONES : 1;
        RutaPrueba ruta = rutaSeleccionada();
        mostrarEnCurso(true);
        textoDe(libreria).setText(getString(R.string.cargando));
        ejecutar(libreria, ruta, 1, total, new ArrayList<>());
    }

    private void ejecutar(Libreria libreria, RutaPrueba ruta, int numero, int total, List<Long> tiempos) {
        ConsultaCallback alTerminar = resultado -> procesar(resultado, ruta, numero, total, tiempos);

        switch (libreria) {
            case VOLLEY:
                clienteVolley.consultar(ruta, alTerminar);
                break;
            case OKHTTP:
                llamadaOkHttp = clienteOkHttp.consultar(ruta,
                        resultado -> runOnUiThread(() -> alTerminar.onResultado(resultado)));
                break;
            case RETROFIT:
                llamadaRetrofit = clienteRetrofit.consultar(ruta, alTerminar);
                break;
        }
    }

    private void procesar(ResultadoConsulta resultado, RutaPrueba ruta, int numero, int total, List<Long> tiempos) {
        if (isFinishing() || isDestroyed()) return;

        Libreria libreria = resultado.getLibreria();
        registrar(resultado, ruta, numero, total);

        if (resultado.isExito()) {
            tiempos.add(resultado.getMilisegundos());
            if (numero < total) {
                ejecutar(libreria, ruta, numero + 1, total, tiempos);
                return;
            }
        }

        mostrarEnCurso(false);
        textoDe(libreria).setText(resultado.isExito()
                ? textoExito(resultado, tiempos)
                : textoError(resultado));

        if (!resultado.isExito()) {
            Snackbar.make(root, resultado.getMensajeUsuario(), Snackbar.LENGTH_LONG).show();
        }
    }

    private String textoExito(ResultadoConsulta resultado, List<Long> tiempos) {
        StringBuilder texto = new StringBuilder()
                .append(resultado.getCantidad()).append(" registros · ")
                .append(resultado.getMilisegundos()).append(" ms");
        if (tiempos.size() > 1) {
            long suma = 0;
            for (long t : tiempos) suma += t;
            texto.append("\nPromedio de ").append(tiempos.size()).append(": ")
                    .append(suma / tiempos.size()).append(" ms (")
                    .append(TextUtils.join(", ", tiempos)).append(")");
        }
        return texto.toString();
    }

    private String textoError(ResultadoConsulta resultado) {
        return "Error: " + resultado.getMensajeUsuario()
                + "\nCódigo: " + resultado.getErrorTecnico()
                + " · " + resultado.getMilisegundos() + " ms";
    }

    private void registrar(ResultadoConsulta resultado, RutaPrueba ruta, int numero, int total) {
        String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String linea = hora + " " + resultado.getLibreria().getNombre()
                + " " + numero + "/" + total + " " + ruta.getRuta() + " → "
                + (resultado.isExito()
                    ? resultado.getCantidad() + " registros, " + resultado.getMilisegundos() + " ms"
                    : resultado.getErrorTecnico() + ", " + resultado.getMilisegundos() + " ms");

        if (resultado.isExito()) Log.i(ETIQUETA, linea);
        else Log.w(ETIQUETA, linea + " | " + resultado.getMensajeUsuario());

        CharSequence anterior = textBitacora.getText();
        textBitacora.setText(anterior.length() == 0 ? linea : linea + "\n" + anterior);
    }

    private void alterarToken() {
        String token = sesion.getToken();
        int firma = token == null ? -1 : token.lastIndexOf('.');
        if (firma < 0 || firma + 6 >= token.length()) {
            Snackbar.make(root, R.string.sin_sesion, Snackbar.LENGTH_LONG).show();
            return;
        }
        int posicion = firma + 5;
        char nuevo = token.charAt(posicion) == 'a' ? 'b' : 'a';
        sesion.reemplazarToken(token.substring(0, posicion) + nuevo + token.substring(posicion + 1));
        Log.w(ETIQUETA, "Token alterado para la prueba 401");
        Snackbar.make(root, R.string.token_alterado, Snackbar.LENGTH_LONG).show();
    }

    private TextView textoDe(Libreria libreria) {
        switch (libreria) {
            case VOLLEY: return textVolley;
            case OKHTTP: return textOkHttp;
            default: return textRetrofit;
        }
    }

    private void mostrarEnCurso(boolean valor) {
        enCurso = valor;
        progreso.setVisibility(valor ? View.VISIBLE : View.GONE);
        botonVolley.setEnabled(!valor);
        botonOkHttp.setEnabled(!valor);
        botonRetrofit.setEnabled(!valor);
        botonAlterarToken.setEnabled(!valor);
    }

    @Override
    protected void onDestroy() {
        clienteVolley.cancelar();
        if (llamadaOkHttp != null) llamadaOkHttp.cancel();
        if (llamadaRetrofit != null) llamadaRetrofit.cancelar();
        super.onDestroy();
    }
}
