package com.uth.exa1pa1199.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.viewmodel.ListaRegistrosViewModel;
import com.uth.exa1pa1199.viewmodel.UiState;

import java.util.Collections;
import java.util.List;

public class ListaRegistrosActivity extends SesionActivity {
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

    private boolean solicitudEnCurso;

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
        mostrarUsuario();

        swipeRefresh.setOnRefreshListener(this::cargarSiHayPermiso);
        fabNuevo.setOnClickListener(v ->
                startActivity(new Intent(this, FormularioRegistroActivity.class)));
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!sesionActiva()) return;
        cargarSiHayPermiso();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lista, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.accionComparativa) {
            startActivity(new Intent(this, ComparativaActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.accionComparativaJennifer) {
            startActivity(new Intent(this, ComparativaJenniferActivity.class));
            return true;
        }

        if (item.getItemId() == R.id.accionCerrarSesion) {
            confirmarCerrarSesion();
            return true;
        }

        if (item.getItemId() == R.id.accionComparativaMichael) {
            startActivity(new Intent(this, ComparativaMichaelActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void mostrarUsuario() {
        if (getSupportActionBar() == null || viewModel.getCorreo() == null) return;
        getSupportActionBar().setSubtitle(
                getString(R.string.subtitulo_sesion, viewModel.getCorreo(), viewModel.getRol()));
    }

    private void confirmarCerrarSesion() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.cerrar_sesion)
                .setMessage(R.string.confirmar_cerrar_sesion)
                .setNegativeButton(R.string.cancelar, null)
                .setPositiveButton(R.string.cerrar_sesion, (d, w) -> {
                    viewModel.cerrarSesion();
                    Toast.makeText(this, R.string.sesion_cerrada, Toast.LENGTH_SHORT).show();
                    irALogin(null);
                })
                .show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SOLICITUD_EN_CURSO, solicitudEnCurso);
    }

    private boolean tienePermisoRedLocal() {
        return PermisoRedLocal.concedido(this);
    }

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
        solicitarPermisoRedLocal.launch(PermisoRedLocal.PERMISO);
    }

    private void mostrarPermisoDenegado() {
        adapter.submitList(Collections.emptyList());
        textVacio.setVisibility(View.GONE);
        layoutError.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        fabNuevo.setEnabled(false);

        boolean puedePedirDeNuevo = shouldShowRequestPermissionRationale(PermisoRedLocal.PERMISO);

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
        if (manejarSesionExpirada(estado)) return;
        boolean cargando = estado.esCargando();

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
            case SIN_PERMISO:
                adapter.submitList(Collections.emptyList());
                UiHelper.mostrarError(layoutError, estado, () -> viewModel.cargar());
                break;
        }
    }
}
