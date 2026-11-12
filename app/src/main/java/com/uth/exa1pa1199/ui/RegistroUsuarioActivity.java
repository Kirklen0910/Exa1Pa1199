package com.uth.exa1pa1199.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.viewmodel.RegistroUsuarioViewModel;
import com.uth.exa1pa1199.viewmodel.UiState;

import java.util.Map;

public class RegistroUsuarioActivity extends AppCompatActivity {
    private RegistroUsuarioViewModel viewModel;

    private TextInputLayout layoutCorreo, layoutPassword, layoutConfirmacion;
    private TextInputEditText editCorreo, editPassword, editConfirmacion;
    private TextView textErrorGeneral;
    private Button buttonRegistrar, buttonReintentar;
    private ProgressBar progressBar;

    private final ActivityResultLauncher<String> permisoRedLocal =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), concedido -> {
                if (concedido) {
                    enviar();
                } else {
                    mostrarErrorTexto(getString(R.string.permiso_red_local_mensaje), false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        UiHelper.activarEdgeToEdge(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);
        UiHelper.configurarPantalla(this, true);
        setTitle(R.string.titulo_registro_usuario);

        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutPassword = findViewById(R.id.layoutPassword);
        layoutConfirmacion = findViewById(R.id.layoutConfirmacion);
        editCorreo = findViewById(R.id.editCorreo);
        editPassword = findViewById(R.id.editPassword);
        editConfirmacion = findViewById(R.id.editConfirmacion);
        textErrorGeneral = findViewById(R.id.textErrorGeneral);
        buttonRegistrar = findViewById(R.id.buttonRegistrar);
        buttonReintentar = findViewById(R.id.buttonReintentar);
        progressBar = findViewById(R.id.progressBar);

        viewModel = new ViewModelProvider(this).get(RegistroUsuarioViewModel.class);
        viewModel.getEstado().observe(this, this::render);
        viewModel.getErroresValidacion().observe(this, this::mostrarErroresCampo);

        buttonRegistrar.setOnClickListener(v -> {
            if (PermisoRedLocal.concedido(this)) enviar();
            else permisoRedLocal.launch(PermisoRedLocal.PERMISO);
        });
        buttonReintentar.setOnClickListener(v -> viewModel.reintentar());
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void enviar() {
        ocultarErrorGeneral();
        viewModel.registrar(texto(editCorreo), texto(editPassword), texto(editConfirmacion));
    }

    private void render(UiState<Void> estado) {
        boolean cargando = estado.esCargando();
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        buttonRegistrar.setEnabled(!cargando);
        buttonReintentar.setEnabled(!cargando);
        editCorreo.setEnabled(!cargando);
        editPassword.setEnabled(!cargando);
        editConfirmacion.setEnabled(!cargando);

        if (estado.getEstado() == UiState.Estado.EXITO) {
            Toast.makeText(this, R.string.cuenta_creada, Toast.LENGTH_SHORT).show();
            Intent i = new Intent(this, ListaRegistrosActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        } else if (estado.esError()) {
            mostrarErroresCampo(estado.getErroresCampo());
            mostrarErrorTexto(estado.getMensaje(), estado.permiteReintentar());
        }
    }

    private void mostrarErroresCampo(Map<String, String> errores) {
        layoutCorreo.setError(errores.get(RegistroUsuarioViewModel.CAMPO_CORREO));
        layoutPassword.setError(errores.get(RegistroUsuarioViewModel.CAMPO_PASSWORD));
        layoutConfirmacion.setError(errores.get(RegistroUsuarioViewModel.CAMPO_CONFIRMACION));
    }

    private void mostrarErrorTexto(String mensaje, boolean reintentar) {
        textErrorGeneral.setText(mensaje);
        textErrorGeneral.setVisibility(View.VISIBLE);
        buttonReintentar.setVisibility(reintentar ? View.VISIBLE : View.GONE);
    }

    private void ocultarErrorGeneral() {
        textErrorGeneral.setVisibility(View.GONE);
        buttonReintentar.setVisibility(View.GONE);
    }

    private static String texto(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }
}
