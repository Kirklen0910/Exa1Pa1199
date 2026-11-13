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
import com.uth.exa1pa1199.data.comparativa_fernando.CallbackComparativa;
import com.uth.exa1pa1199.data.comparativa_fernando.ClienteOkHttpFernando;
import com.uth.exa1pa1199.data.comparativa_fernando.ClienteRetrofitFernando;
import com.uth.exa1pa1199.data.comparativa_fernando.ClienteVolleyFernando;
import com.uth.exa1pa1199.data.comparativa_fernando.LibreriaComparativa;
import com.uth.exa1pa1199.data.comparativa_fernando.ResultadoComparativa;
import com.uth.exa1pa1199.data.comparativa_fernando.RutaComparativa;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;

/**
 * Laboratorio individual: ejecuta la misma consulta con Volley, OkHttp y Retrofit,
 * mide el tiempo, registra el error técnico y muestra un mensaje al usuario.
 */
public class ComparativaFernandoActivity extends AppCompatActivity {
    private static final String ETIQUETA = "ComparativaFernando";
    private static final int REPETICIONES = 5;

    private SessionManager sesion;
    private ClienteVolleyFernando clienteVolley;
    private ClienteOkHttpFernando clienteOkHttp;
    private ClienteRetrofitFernando clienteRetrofit;

    private Call llamadaOkHttp;
    private Cancelable llamadaRetrofit;

    private View root;
    private RadioGroup grupoRutas;
    private CheckBox checkRepetir;
    private Button botonVolley;
    private Button botonOkHttp;
    private Button botonRetrofit;
    private Button botonTokenInvalido;
    private Button botonSinToken;
    private Button botonRestaurarToken;
    private LinearProgressIndicator progreso;
    private TextView textVolley;
    private TextView textOkHttp;
    private TextView textRetrofit;
    private TextView textBitacora;

    private boolean enCurso;
    private String tokenOriginal;
    private boolean tokenModificado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comparativa_fernando);
        configurarPantalla();

        sesion = SessionManager.get();
        clienteVolley = new ClienteVolleyFernando(this, sesion);
        clienteOkHttp = new ClienteOkHttpFernando(sesion);
        clienteRetrofit = new ClienteRetrofitFernando(sesion);

        root = findViewById(R.id.rootFernando);
        grupoRutas = findViewById(R.id.grupoRutasFernando);
        checkRepetir = findViewById(R.id.checkRepetirFernando);
        botonVolley = findViewById(R.id.botonVolleyFernando);
        botonOkHttp = findViewById(R.id.botonOkHttpFernando);
        botonRetrofit = findViewById(R.id.botonRetrofitFernando);
        botonTokenInvalido = findViewById(R.id.botonTokenInvalidoFernando);
        botonSinToken = findViewById(R.id.botonSinTokenFernando);
        botonRestaurarToken = findViewById(R.id.botonRestaurarTokenFernando);
        progreso = findViewById(R.id.progresoFernando);
        textVolley = findViewById(R.id.textResultadoVolleyFernando);
        textOkHttp = findViewById(R.id.textResultadoOkHttpFernando);
        textRetrofit = findViewById(R.id.textResultadoRetrofitFernando);
        textBitacora = findViewById(R.id.textBitacoraFernando);

        botonVolley.setOnClickListener(v -> iniciar(LibreriaComparativa.VOLLEY));
        botonOkHttp.setOnClickListener(v -> iniciar(LibreriaComparativa.OKHTTP));
        botonRetrofit.setOnClickListener(v -> iniciar(LibreriaComparativa.RETROFIT));
        botonTokenInvalido.setOnClickListener(v -> alterarToken());
        botonSinToken.setOnClickListener(v -> quitarToken());
        botonRestaurarToken.setOnClickListener(v -> restaurarToken());
        findViewById(R.id.botonLimpiarFernando).setOnClickListener(v -> textBitacora.setText(""));

        actualizarBotonesToken();
    }

    private void configurarPantalla() {
        MaterialToolbar toolbar = findViewById(R.id.toolbarFernando);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.titulo_comparativa_fernando);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        View contenedor = findViewById(R.id.rootFernando);
        View appBar = findViewById(R.id.appBarFernando);
        ViewCompat.setOnApplyWindowInsetsListener(contenedor, (v, insets) -> {
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

    private RutaComparativa rutaSeleccionada() {
        int id = grupoRutas.getCheckedRadioButtonId();
        if (id == R.id.radioDemoraFernando) return RutaComparativa.DEMORA;
        if (id == R.id.radioErrorFernando) return RutaComparativa.ERROR_500;
        return RutaComparativa.REGISTROS;
    }

    private void iniciar(LibreriaComparativa libreria) {
        if (enCurso) return;
        int total = checkRepetir.isChecked() ? REPETICIONES : 1;
        RutaComparativa ruta = rutaSeleccionada();
        mostrarEnCurso(true);
        textoDe(libreria).setText(R.string.cargando);
        ejecutar(libreria, ruta, 1, total, new ArrayList<>());
    }

    private void ejecutar(LibreriaComparativa libreria, RutaComparativa ruta, int numero,
                          int total, List<Long> tiempos) {
        // Todos los clientes terminan publicando en el hilo principal.
        CallbackComparativa alTerminar = resultado ->
                runOnUiThread(() -> procesar(resultado, ruta, numero, total, tiempos));

        switch (libreria) {
            case VOLLEY:
                clienteVolley.consultar(ruta, alTerminar);
                break;
            case OKHTTP:
                llamadaOkHttp = clienteOkHttp.consultar(ruta, alTerminar);
                break;
            case RETROFIT:
                llamadaRetrofit = clienteRetrofit.consultar(ruta, alTerminar);
                break;
        }
    }

    private void procesar(ResultadoComparativa resultado, RutaComparativa ruta, int numero,
                          int total, List<Long> tiempos) {
        if (isFinishing() || isDestroyed()) return;

        registrar(resultado, ruta, numero, total);

        if (resultado.isExito()) {
            tiempos.add(resultado.getMilisegundos());
            if (numero < total) {
                ejecutar(resultado.getLibreria(), ruta, numero + 1, total, tiempos);
                return;
            }
        }

        mostrarEnCurso(false);
        textoDe(resultado.getLibreria()).setText(resultado.isExito()
                ? textoExito(resultado, tiempos)
                : textoError(resultado));

        if (!resultado.isExito()) {
            Snackbar.make(root, resultado.getMensajeUsuario(), Snackbar.LENGTH_LONG).show();
        }
    }

    private String textoExito(ResultadoComparativa resultado, List<Long> tiempos) {
        StringBuilder texto = new StringBuilder()
                .append(resultado.getCantidad()).append(" registros · ")
                .append(resultado.getMilisegundos()).append(" ms");
        if (tiempos.size() > 1) {
            long suma = 0;
            for (long tiempo : tiempos) suma += tiempo;
            texto.append("\nPromedio de ").append(tiempos.size()).append(": ")
                    .append(suma / tiempos.size()).append(" ms (")
                    .append(TextUtils.join(", ", tiempos)).append(")");
        }
        return texto.toString();
    }

    private String textoError(ResultadoComparativa resultado) {
        return "Error: " + resultado.getMensajeUsuario()
                + "\nDetalle técnico: " + resultado.getDetalleTecnico()
                + "\nTiempo: " + resultado.getMilisegundos() + " ms";
    }

    private void registrar(ResultadoComparativa resultado, RutaComparativa ruta, int numero,
                           int total) {
        String hora = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
        String linea = hora + " · " + resultado.getLibreria().getNombre()
                + " · intento " + numero + "/" + total
                + " · " + ruta.getDescripcion() + " → "
                + (resultado.isExito()
                    ? resultado.getCantidad() + " registros, " + resultado.getMilisegundos() + " ms"
                    : resultado.getDetalleTecnico() + ", " + resultado.getMilisegundos() + " ms");

        if (resultado.isExito()) {
            Log.i(ETIQUETA, linea);
        } else {
            Log.w(ETIQUETA, linea + " | " + resultado.getMensajeUsuario());
        }

        CharSequence anterior = textBitacora.getText();
        textBitacora.setText(anterior.length() == 0 ? linea : linea + "\n" + anterior);
    }

    private void alterarToken() {
        if (enCurso) return;
        String token = sesion.getToken();
        if (token == null || token.isEmpty()) {
            Snackbar.make(root, R.string.sin_sesion_fernando, Snackbar.LENGTH_LONG).show();
            return;
        }

        int firma = token.lastIndexOf('.');
        if (firma < 0 || firma + 4 >= token.length()) {
            Snackbar.make(root, R.string.token_no_alterable_fernando, Snackbar.LENGTH_LONG).show();
            return;
        }

        guardarTokenOriginal();
        int posicion = firma + 4;
        char actual = token.charAt(posicion);
        char nuevo = actual == 'a' ? 'b' : 'a';
        sesion.reemplazarToken(token.substring(0, posicion) + nuevo + token.substring(posicion + 1));
        tokenModificado = true;
        actualizarBotonesToken();
        Log.w(ETIQUETA, "Token alterado para provocar un 401");
        Snackbar.make(root, R.string.token_alterado_fernando, Snackbar.LENGTH_LONG).show();
    }

    private void quitarToken() {
        if (enCurso) return;
        if (sesion.getToken() == null) {
            Snackbar.make(root, R.string.sin_sesion_fernando, Snackbar.LENGTH_LONG).show();
            return;
        }
        guardarTokenOriginal();
        sesion.reemplazarToken(null);
        tokenModificado = true;
        actualizarBotonesToken();
        Log.w(ETIQUETA, "Token retirado para provocar un 401");
        Snackbar.make(root, R.string.token_retirado_fernando, Snackbar.LENGTH_LONG).show();
    }

    private void guardarTokenOriginal() {
        if (!tokenModificado) {
            tokenOriginal = sesion.getToken();
            tokenModificado = true;
        }
    }

    private void restaurarToken() {
        if (enCurso) return;
        if (!tokenModificado) {
            Snackbar.make(root, R.string.token_sin_cambios_fernando, Snackbar.LENGTH_LONG).show();
            return;
        }
        restaurarTokenSilencioso();
        Snackbar.make(root, R.string.token_restaurado_fernando, Snackbar.LENGTH_LONG).show();
    }

    private void restaurarTokenSilencioso() {
        if (!tokenModificado || sesion == null) return;
        sesion.reemplazarToken(tokenOriginal);
        tokenOriginal = null;
        tokenModificado = false;
        actualizarBotonesToken();
    }

    private void actualizarBotonesToken() {
        if (botonRestaurarToken != null) {
            botonRestaurarToken.setEnabled(!enCurso && tokenModificado);
        }
    }

    private TextView textoDe(LibreriaComparativa libreria) {
        switch (libreria) {
            case VOLLEY:
                return textVolley;
            case OKHTTP:
                return textOkHttp;
            default:
                return textRetrofit;
        }
    }

    private void mostrarEnCurso(boolean valor) {
        enCurso = valor;
        progreso.setVisibility(valor ? View.VISIBLE : View.GONE);
        botonVolley.setEnabled(!valor);
        botonOkHttp.setEnabled(!valor);
        botonRetrofit.setEnabled(!valor);
        botonTokenInvalido.setEnabled(!valor);
        botonSinToken.setEnabled(!valor);
        botonRestaurarToken.setEnabled(!valor && tokenModificado);
        checkRepetir.setEnabled(!valor);
        grupoRutas.setEnabled(!valor);
        for (int i = 0; i < grupoRutas.getChildCount(); i++) {
            grupoRutas.getChildAt(i).setEnabled(!valor);
        }
    }

    @Override
    protected void onDestroy() {
        if (clienteVolley != null) clienteVolley.cancelar();
        if (llamadaOkHttp != null) llamadaOkHttp.cancel();
        if (llamadaRetrofit != null) llamadaRetrofit.cancelar();
        restaurarTokenSilencioso();
        super.onDestroy();
    }
}
