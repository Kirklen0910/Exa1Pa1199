package com.uth.exa1pa1199.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.session.SessionManager;
import com.uth.exa1pa1199.viewmodel.LoginViewModel;
import com.uth.exa1pa1199.viewmodel.UiState;

import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_MENSAJE = "mensaje";
    private static final String KEY_MENSAJE = "mensajeSesion";

    private LoginViewModel viewModel;

    private TextInputLayout layoutCorreo, layoutPassword;
    private TextInputEditText editCorreo, editPassword;
    private TextView textMensaje, textErrorGeneral;
    private Button buttonIniciar, buttonCrearCuenta, buttonReintentar;
    private ProgressBar progressBar;
    private String mensajeSesion;

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

        if (SessionManager.get().haySesionValida()) {
            abrirLista();
            return;
        }

        setContentView(R.layout.activity_login);
        UiHelper.configurarPantalla(this, false);
        setTitle(R.string.titulo_login);

        layoutCorreo = findViewById(R.id.layoutCorreo);
        layoutPassword = findViewById(R.id.layoutPassword);
        editCorreo = findViewById(R.id.editCorreo);
        editPassword = findViewById(R.id.editPassword);
        textMensaje = findViewById(R.id.textMensaje);
        textErrorGeneral = findViewById(R.id.textErrorGeneral);
        buttonIniciar = findViewById(R.id.buttonIniciar);
        buttonCrearCuenta = findViewById(R.id.buttonCrearCuenta);
        buttonReintentar = findViewById(R.id.buttonReintentar);
        progressBar = findViewById(R.id.progressBar);

        mensajeSesion = savedInstanceState != null
                ? savedInstanceState.getString(KEY_MENSAJE)
                : getIntent().getStringExtra(EXTRA_MENSAJE);
        if (mensajeSesion != null) {
            textMensaje.setText(mensajeSesion);
            textMensaje.setVisibility(View.VISIBLE);
        }

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        viewModel.getEstado().observe(this, this::render);
        viewModel.getErroresValidacion().observe(this, this::mostrarErroresCampo);

        buttonIniciar.setOnClickListener(v -> {
            if (PermisoRedLocal.concedido(this)) enviar();
            else permisoRedLocal.launch(PermisoRedLocal.PERMISO);
        });
        buttonReintentar.setOnClickListener(v -> viewModel.reintentar());
        buttonCrearCuenta.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroUsuarioActivity.class)));
        editPassword.setOnEditorActionListener((v, actionId, event) -> {
            buttonIniciar.performClick();
            return true;
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mensajeSesion != null) outState.putString(KEY_MENSAJE, mensajeSesion);
    }

    private void enviar() {
        layoutCorreo.setError(null);
        layoutPassword.setError(null);
        ocultarErrorGeneral();
        viewModel.iniciarSesion(texto(editCorreo), texto(editPassword));
    }

    private void render(UiState<Void> estado) {
        boolean cargando = estado.esCargando();
        progressBar.setVisibility(cargando ? View.VISIBLE : View.GONE);
        buttonIniciar.setEnabled(!cargando);
        buttonCrearCuenta.setEnabled(!cargando);
        buttonReintentar.setEnabled(!cargando);
        editCorreo.setEnabled(!cargando);
        editPassword.setEnabled(!cargando);

        if (estado.getEstado() == UiState.Estado.EXITO) {
            abrirLista();
        } else if (estado.esError()) {
            textMensaje.setVisibility(View.GONE);
            mostrarErrorTexto(estado.getMensaje(), estado.permiteReintentar());
            if (estado.getCodigoHttp() == 401) editPassword.setText("");
        }
    }

    private void mostrarErroresCampo(Map<String, String> errores) {
        layoutCorreo.setError(errores.get(LoginViewModel.CAMPO_CORREO));
        layoutPassword.setError(errores.get(LoginViewModel.CAMPO_PASSWORD));
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

    private void abrirLista() {
        Intent i = new Intent(this, ListaRegistrosActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(i);
        finish();
    }

    private static String texto(TextInputEditText e) {
        return e.getText() == null ? "" : e.getText().toString();
    }
}
