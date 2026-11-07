package com.uth.exa1pa1199.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.util.FechaUtils;
import com.uth.exa1pa1199.viewmodel.DetalleRegistroViewModel;
import com.uth.exa1pa1199.viewmodel.UiState;

public class DetalleRegistroActivity extends AppCompatActivity {
    public static final String EXTRA_ID = "registro_id";

    private DetalleRegistroViewModel viewModel;

    private View root, scrollContenido, layoutError;
    private ProgressBar progressBar;
    private ImageView imageFoto;
    private TextView textSinFoto, textTitulo, textFecha, textDescripcion;
    private Button buttonEditar, buttonEliminar;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiHelper.activarEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_registro);
        UiHelper.configurarPantalla(this, true);
        setTitle(R.string.titulo_detalle);

        id = getIntent().getStringExtra(EXTRA_ID);
        if (id == null) {
            finish();
            return;
        }

        root = findViewById(R.id.root);
        scrollContenido = findViewById(R.id.scrollContenido);
        layoutError = findViewById(R.id.layoutError);
        progressBar = findViewById(R.id.progressBar);
        imageFoto = findViewById(R.id.imageFoto);
        textSinFoto = findViewById(R.id.textSinFoto);
        textTitulo = findViewById(R.id.textTitulo);
        textFecha = findViewById(R.id.textFecha);
        textDescripcion = findViewById(R.id.textDescripcion);
        buttonEditar = findViewById(R.id.buttonEditar);
        buttonEliminar = findViewById(R.id.buttonEliminar);

        viewModel = new ViewModelProvider(this).get(DetalleRegistroViewModel.class);
        viewModel.setId(id);
        viewModel.getEstado().observe(this, this::renderDetalle);
        viewModel.getEstadoEliminar().observe(this, this::renderEliminar);
        viewModel.getFoto().observe(this, bitmap -> {
            imageFoto.setImageBitmap(bitmap);
            imageFoto.setVisibility(bitmap != null ? View.VISIBLE : View.GONE);
            textSinFoto.setVisibility(bitmap != null ? View.GONE : View.VISIBLE);
        });

        buttonEditar.setOnClickListener(v -> {
            Intent i = new Intent(this, FormularioRegistroActivity.class);
            i.putExtra(FormularioRegistroActivity.EXTRA_ID, id);
            startActivity(i);
        });
        buttonEliminar.setOnClickListener(v -> confirmarEliminar());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (viewModel != null) viewModel.cargar();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void confirmarEliminar() {
        String titulo = textTitulo.getText().toString();
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.confirmar_eliminar_titulo)
                .setMessage(getString(R.string.confirmar_eliminar_mensaje, titulo))
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.eliminar, (d, w) -> viewModel.eliminar())
                .show();
    }

    private void renderDetalle(UiState<RegistroDetalleDto> estado) {
        boolean cargando = estado.esCargando();
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        actualizarBotones();
        layoutError.setVisibility(View.GONE);

        RegistroDetalleDto r = estado.getDatos();
        if (r != null) {
            scrollContenido.setVisibility(View.VISIBLE);
            textTitulo.setText(r.getTitulo());
            textDescripcion.setText(r.getDescripcion());
            textFecha.setText(getString(R.string.fecha_creacion, FechaUtils.formatear(r.getFechaCreacion())));
        } else if (!cargando) {
            scrollContenido.setVisibility(View.GONE);
        }

        if (estado.esError()) {
            scrollContenido.setVisibility(View.GONE);
            UiHelper.mostrarError(layoutError, estado, () -> viewModel.cargar());
        }
    }

    private void renderEliminar(UiState<Void> estado) {
        progressBar.setVisibility(estado.esCargando() ? View.VISIBLE : View.GONE);
        actualizarBotones();

        switch (estado.getEstado()) {
            case EXITO:
                Toast.makeText(this, R.string.registro_eliminado, Toast.LENGTH_SHORT).show();
                finish();
                break;
            case ERROR_SERVIDOR:
            case SIN_CONEXION:
            case TIEMPO_AGOTADO:
                Snackbar s = Snackbar.make(root,
                        UiHelper.tituloError(estado) + ": " + estado.getMensaje(), Snackbar.LENGTH_LONG);
                if (estado.permiteReintentar()) {
                    s.setAction(R.string.reintentar, v -> viewModel.eliminar());
                }
                s.show();
                break;
            default:
                break;
        }
    }

    private void actualizarBotones() {
        UiState<?> d = viewModel.getEstado().getValue();
        UiState<?> e = viewModel.getEstadoEliminar().getValue();
        boolean ocupado = (d != null && d.esCargando()) || (e != null && e.esCargando());
        boolean hayDatos = d != null && d.getDatos() != null;
        buttonEditar.setEnabled(!ocupado && hayDatos);
        buttonEliminar.setEnabled(!ocupado && hayDatos);
        layoutError.findViewById(R.id.buttonReintentar).setEnabled(!ocupado);
        progressBar.setVisibility(ocupado ? View.VISIBLE : View.GONE);
    }
}
