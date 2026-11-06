package com.uth.exa1pa1199.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.viewmodel.ListaRegistrosViewModel;
import com.uth.exa1pa1199.viewmodel.UiState;

import java.util.Collections;
import java.util.List;

/** Pantalla de listado: RecyclerView + deslizar para actualizar + botón nuevo. */
public class ListaRegistrosActivity extends AppCompatActivity {

    /**
     * Android 17 (API 37) introduce Local Network Protection: sin este permiso de tiempo de ejecución las
     * conexiones a la red local (p. ej. http://10.0.2.2:5080) se bloquean y terminan en timeout.
     * Se usa el literal para no depender de la constante de Manifest.permission.
     */
    static final String PERMISO_RED_LOCAL = "android.permission.ACCESS_LOCAL_NETWORK";
    private static final int API_RED_LOCAL = 37;
    private static final String KEY_SOLICITUD_EN_CURSO = "solicitudPermisoEnCurso";

    private ListaRegistrosViewModel viewModel;
    private RegistroAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;
    private RecyclerView recycler;
    private TextView textVacio;
    private View layoutError;
    private ProgressBar progressBar;
    private FloatingActionButton fabNuevo;
    private View layoutPermiso;

    /** Evita lanzar el diálogo dos veces (p. ej. onStart tras rotar mientras el diálogo está abierto). */
    private boolean solicitudEnCurso;
    /** El usuario ya respondió "No permitir": no se vuelve a pedir solo, se espera a que pulse el botón. */
    private boolean permisoDenegado;

    private final ActivityResultLauncher<String> solicitarPermisoRedLocal =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                solicitudEnCurso = false;
                if (concedido) {
                    permisoDenegado = false;
                    layoutPermiso.setVisibility(View.GONE);
                    viewModel.cargar();
                } else {
                    permisoDenegado = true;
                    mostrarPermisoDenegado();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiHelper.activarEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_registros);
        UiHelper.configurarPantalla(this, false);
        setTitle(R.string.titulo_lista);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        recycler = findViewById(R.id.recyclerRegistros);
        textVacio = findViewById(R.id.textVacio);
        layoutError = findViewById(R.id.layoutError);
        progressBar = findViewById(R.id.progressBar);
        fabNuevo = findViewById(R.id.fabNuevo);
        layoutPermiso = findViewById(R.id.layoutPermiso);

        if (savedInstanceState != null) {
            solicitudEnCurso = savedInstanceState.getBoolean(KEY_SOLICITUD_EN_CURSO, false);
        }

        adapter = new RegistroAdapter(this::abrirDetalle);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(ListaRegistrosViewModel.class);
        viewModel.getEstado().observe(this, this::render);

        swipeRefresh.setOnRefreshListener(this::cargarSiHayPermiso);
        fabNuevo.setOnClickListener(v ->
                startActivity(new Intent(this, FormularioRegistroActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Recarga al abrir y al volver de crear / editar / eliminar (o de Ajustes tras conceder el permiso).
        cargarSiHayPermiso();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SOLICITUD_EN_CURSO, solicitudEnCurso);
    }

    // ---------------------------------------------------------------------------------------------
    // Permiso de red local (Android 17+)
    // ---------------------------------------------------------------------------------------------

    /** Antes de Android 17 no existe la restricción; desde API 37 hay que tener el permiso concedido. */
    private boolean tienePermisoRedLocal() {
        return Build.VERSION.SDK_INT < API_RED_LOCAL
                || ContextCompat.checkSelfPermission(this, PERMISO_RED_LOCAL)
                == PackageManager.PERMISSION_GRANTED;
    }

    /** Carga la lista si hay permiso; si no, lo solicita (solo la primera vez) o muestra el aviso. */
    private void cargarSiHayPermiso() {
        if (tienePermisoRedLocal()) {
            permisoDenegado = false;
            layoutPermiso.setVisibility(View.GONE);
            viewModel.cargar();
            return;
        }
        swipeRefresh.setRefreshing(false);
        if (permisoDenegado) {
            mostrarPermisoDenegado();
        } else {
            pedirPermisoRedLocal();
        }
    }

    private void pedirPermisoRedLocal() {
        if (solicitudEnCurso) return;
        solicitudEnCurso = true;
        solicitarPermisoRedLocal.launch(PERMISO_RED_LOCAL);
    }

    /**
     * Explica por qué se necesita el permiso, con botón para volver a pedirlo. Si el sistema ya no muestra
     * el diálogo (denegado dos veces / "no volver a preguntar"), el botón abre los ajustes de la app.
     */
    private void mostrarPermisoDenegado() {
        adapter.submitList(Collections.emptyList());
        textVacio.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        fabNuevo.setEnabled(false);

        boolean puedePedirDeNuevo = shouldShowRequestPermissionRationale(PERMISO_RED_LOCAL);

        TextView titulo = layoutPermiso.findViewById(R.id.textErrorTitulo);
        TextView mensaje = layoutPermiso.findViewById(R.id.textErrorMensaje);
        Button boton = layoutPermiso.findViewById(R.id.buttonReintentar);

        titulo.setText(R.string.permiso_red_local_titulo);
        mensaje.setText(puedePedirDeNuevo
                ? R.string.permiso_red_local_mensaje
                : R.string.permiso_red_local_mensaje_ajustes);
        boton.setText(puedePedirDeNuevo ? R.string.conceder_permiso : R.string.abrir_ajustes);
        boton.setEnabled(true);
        boton.setVisibility(View.VISIBLE);
        boton.setOnClickListener(v -> {
            if (puedePedirDeNuevo) {
                pedirPermisoRedLocal();
            } else {
                // Al volver de Ajustes, onStart() comprueba de nuevo el permiso y carga la lista.
                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null)));
            }
        });
        layoutPermiso.setVisibility(View.VISIBLE);
    }

    private void abrirDetalle(RegistroResumenDto registro) {
        Intent i = new Intent(this, DetalleRegistroActivity.class);
        i.putExtra(DetalleRegistroActivity.EXTRA_ID, registro.getId());
        startActivity(i);
    }

    private void render(UiState<List<RegistroResumenDto>> estado) {
        boolean cargando = estado.esCargando();

        // Cargando: ProgressBar visible y botones deshabilitados.
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        swipeRefresh.setRefreshing(false);
        swipeRefresh.setEnabled(!cargando);
        fabNuevo.setEnabled(!cargando);
        layoutError.findViewById(R.id.buttonReintentar).setEnabled(!cargando);

        textVacio.setVisibility(estado.getEstado() == UiState.Estado.VACIO ? View.VISIBLE : View.GONE);
        layoutError.setVisibility(View.GONE);

        switch (estado.getEstado()) {
            case CARGANDO:
                List<RegistroResumenDto> previos = estado.getDatos();
                adapter.submitList(previos != null ? previos : Collections.emptyList());
                break;
            case EXITO:
                adapter.submitList(estado.getDatos());
                break;
            case VACIO:
                adapter.submitList(Collections.emptyList());
                break;
            case ERROR_SERVIDOR:
            case SIN_CONEXION:
            case TIEMPO_AGOTADO:
                adapter.submitList(Collections.emptyList());
                UiHelper.mostrarError(layoutError, estado, () -> viewModel.cargar());
                break;
        }
    }
}
