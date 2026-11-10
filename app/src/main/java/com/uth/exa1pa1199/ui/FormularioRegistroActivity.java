package com.uth.exa1pa1199.ui;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.viewmodel.FormularioRegistroViewModel;
import com.uth.exa1pa1199.viewmodel.UiState;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class FormularioRegistroActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "registro_id";
    private static final String IDIOMA_VOZ = "es-ES";

    private FormularioRegistroViewModel viewModel;

    private ImageView imageFoto;
    private Button buttonTomarFoto, buttonVoz, buttonGuardar, buttonReintentar;
    private TextInputLayout layoutTitulo, layoutDescripcion;
    private TextInputEditText editTitulo, editDescripcion;
    private TextView textErrorGeneral;
    private ProgressBar progressBar;

    private Uri uriFotoTemp;
    private Runnable accionReintentar;

    private ActivityResultLauncher<Uri> camaraLauncher;
    private ActivityResultLauncher<String> permisoCamaraLauncher;
    private ActivityResultLauncher<String> permisoAudioLauncher;
    private ActivityResultLauncher<Intent> vozLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiHelper.activarEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_formulario_registro);
        UiHelper.configurarPantalla(this, true);

        imageFoto = findViewById(R.id.imageFoto);
        buttonTomarFoto = findViewById(R.id.buttonTomarFoto);
        buttonVoz = findViewById(R.id.buttonVoz);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        buttonReintentar = findViewById(R.id.buttonReintentar);
        layoutTitulo = findViewById(R.id.layoutTitulo);
        layoutDescripcion = findViewById(R.id.layoutDescripcion);
        editTitulo = findViewById(R.id.editTitulo);
        editDescripcion = findViewById(R.id.editDescripcion);
        textErrorGeneral = findViewById(R.id.textErrorGeneral);
        progressBar = findViewById(R.id.progressBar);

        if (savedInstanceState != null) {
            String u = savedInstanceState.getString("uriFotoTemp");
            if (u != null) uriFotoTemp = Uri.parse(u);
        }

        registrarLaunchers();

        viewModel = new ViewModelProvider(this).get(FormularioRegistroViewModel.class);
        viewModel.iniciar(getIntent().getStringExtra(EXTRA_ID));
        setTitle(viewModel.esEdicion() ? R.string.titulo_editar : R.string.titulo_crear);

        viewModel.getEstadoCarga().observe(this, this::renderCarga);
        viewModel.getEstadoGuardar().observe(this, this::renderGuardar);
        viewModel.getErroresValidacion().observe(this, this::mostrarErroresCampo);
        viewModel.getFotoPreview().observe(this, bmp -> {
            if (bmp != null) imageFoto.setImageBitmap(bmp);
        });
        viewModel.getProcesandoFoto().observe(this, p -> actualizarHabilitados());
        viewModel.getErrorFoto().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                viewModel.limpiarErrorFoto();
            }
        });

        buttonTomarFoto.setOnClickListener(v -> pedirCamara());
        buttonVoz.setOnClickListener(v -> pedirMicrofono());
        buttonGuardar.setOnClickListener(v -> guardar());
        buttonReintentar.setOnClickListener(v -> {
            if (accionReintentar != null) accionReintentar.run();
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (uriFotoTemp != null) outState.putString("uriFotoTemp", uriFotoTemp.toString());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void registrarLaunchers() {
        camaraLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(), ok -> {
            if (Boolean.TRUE.equals(ok) && uriFotoTemp != null) {
                viewModel.procesarFoto(uriFotoTemp);
            }
        });

        permisoCamaraLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
            if (concedido) abrirCamara();
            else Toast.makeText(this, R.string.permiso_camara_denegado, Toast.LENGTH_SHORT).show();
        });

        permisoAudioLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
            if (concedido) abrirReconocimientoVoz();
            else Toast.makeText(this, R.string.permiso_audio_denegado, Toast.LENGTH_SHORT).show();
        });

        vozLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() != RESULT_OK || result.getData() == null) return;
            ArrayList<String> textos = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (textos == null || textos.isEmpty()) return;
            agregarTextoDictado(textos.get(0));
        });
    }

    private void pedirCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            abrirCamara();
        } else {
            permisoCamaraLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void abrirCamara() {
        try {
            File archivo = new File(getCacheDir(), "foto_temp.jpg");
            uriFotoTemp = FileProvider.getUriForFile(this, getPackageName() + ".provider", archivo);
            camaraLauncher.launch(uriFotoTemp);
        } catch (IllegalArgumentException | ActivityNotFoundException e) {
            Toast.makeText(this, R.string.camara_no_disponible, Toast.LENGTH_SHORT).show();
        }
    }

    private void pedirMicrofono() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            abrirReconocimientoVoz();
        } else {
            permisoAudioLauncher.launch(Manifest.permission.RECORD_AUDIO);
        }
    }

    private void abrirReconocimientoVoz() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, IDIOMA_VOZ);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, IDIOMA_VOZ);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.prompt_voz));
        try {
            vozLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.voz_no_disponible, Toast.LENGTH_LONG).show();
        }
    }

    private void agregarTextoDictado(String dictado) {
        Editable actual = editDescripcion.getText();
        String previo = actual == null ? "" : actual.toString().trim();
        String nuevo = previo.isEmpty() ? dictado : previo + " " + dictado;
        editDescripcion.setText(nuevo);
        editDescripcion.setSelection(nuevo.length());
        layoutDescripcion.setError(null);
    }

    private void guardar() {
        limpiarErrores();
        viewModel.guardar(texto(editTitulo), texto(editDescripcion));
    }

    private void renderCarga(UiState<RegistroDetalleDto> estado) {
        actualizarHabilitados();
        switch (estado.getEstado()) {
            case EXITO:
                ocultarErrorGeneral();
                RegistroDetalleDto r = estado.getDatos();
                if (viewModel.consumirDatosParaCampos()) {
                    editTitulo.setText(r.getTitulo());
                    editDescripcion.setText(r.getDescripcion());
                }
                break;
            case ERROR_SERVIDOR:
            case SIN_CONEXION:
            case TIEMPO_AGOTADO:
                mostrarErrorGeneral(estado, () -> viewModel.cargarRegistro());
                break;
            default:
                break;
        }
    }

    private void renderGuardar(UiState<Void> estado) {
        actualizarHabilitados();
        switch (estado.getEstado()) {
            case EXITO:
                Toast.makeText(this, viewModel.esEdicion() ? R.string.registro_actualizado : R.string.registro_creado,
                        Toast.LENGTH_SHORT).show();
                finish();
                break;
            case ERROR_SERVIDOR:
            case SIN_CONEXION:
            case TIEMPO_AGOTADO:

                Map<String, String> noAsignados = mostrarErroresCampo(estado.getErroresCampo());
                mostrarErrorGeneral(estado, () -> viewModel.reintentarGuardar());
                if (!noAsignados.isEmpty()) {
                    textErrorGeneral.append("\n" + String.join("\n", noAsignados.values()));
                }
                break;
            default:
                break;
        }
    }

    private Map<String, String> mostrarErroresCampo(Map<String, String> errores) {
        Map<String, String> resto = new java.util.HashMap<>();
        if (errores == null) return resto;
        layoutTitulo.setError(errores.get(FormularioRegistroViewModel.CAMPO_TITULO));
        layoutDescripcion.setError(errores.get(FormularioRegistroViewModel.CAMPO_DESCRIPCION));
        for (Map.Entry<String, String> e : errores.entrySet()) {
            if (!FormularioRegistroViewModel.CAMPO_TITULO.equals(e.getKey())
                    && !FormularioRegistroViewModel.CAMPO_DESCRIPCION.equals(e.getKey())) {
                resto.put(e.getKey(), e.getValue());
            }
        }
        return resto;
    }

    private void mostrarErrorGeneral(UiState<?> estado, Runnable reintentar) {
        textErrorGeneral.setVisibility(View.VISIBLE);
        textErrorGeneral.setText(UiHelper.tituloError(estado) + ": " + estado.getMensaje());
        accionReintentar = reintentar;
        buttonReintentar.setVisibility(estado.permiteReintentar() ? View.VISIBLE : View.GONE);
    }

    private void ocultarErrorGeneral() {
        textErrorGeneral.setVisibility(View.GONE);
        buttonReintentar.setVisibility(View.GONE);
        accionReintentar = null;
    }

    private void limpiarErrores() {
        layoutTitulo.setError(null);
        layoutDescripcion.setError(null);
        ocultarErrorGeneral();
    }

    private void actualizarHabilitados() {
        UiState<?> c = viewModel.getEstadoCarga().getValue();
        UiState<?> g = viewModel.getEstadoGuardar().getValue();
        boolean procesando = Boolean.TRUE.equals(viewModel.getProcesandoFoto().getValue());
        boolean cargando = (c != null && c.esCargando()) || (g != null && g.esCargando()) || procesando;

        boolean edicionSinDatos = viewModel.esEdicion() && (c == null || c.getEstado() != UiState.Estado.EXITO);

        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        buttonTomarFoto.setEnabled(!cargando);
        buttonVoz.setEnabled(!cargando);
        buttonGuardar.setEnabled(!cargando && !edicionSinDatos);
        buttonReintentar.setEnabled(!cargando);
        editTitulo.setEnabled(!cargando);
        editDescripcion.setEnabled(!cargando);
    }

    private static String texto(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }
}
