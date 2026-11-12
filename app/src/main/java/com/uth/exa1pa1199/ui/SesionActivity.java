package com.uth.exa1pa1199.ui;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;

import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.data.session.SessionManager;
import com.uth.exa1pa1199.viewmodel.UiState;

public abstract class SesionActivity extends AppCompatActivity {
    @Override
    protected void onStart() {
        super.onStart();
        SessionManager sesion = SessionManager.get();
        if (!sesion.haySesion()) {
            irALogin(null);
        } else if (sesion.estaVencida()) {
            sesion.cerrar();
            irALogin(getString(R.string.sesion_expirada));
        }
    }

    protected boolean sesionActiva() {
        return !isFinishing() && SessionManager.get().haySesionValida();
    }

    protected boolean manejarSesionExpirada(UiState<?> estado) {
        if (estado == null || !estado.esSesionExpirada()) return false;
        SessionManager.get().cerrar();
        irALogin(estado.getMensaje());
        return true;
    }

    protected void irALogin(String mensaje) {
        if (isFinishing()) return;
        Intent i = new Intent(this, LoginActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (mensaje != null) i.putExtra(LoginActivity.EXTRA_MENSAJE, mensaje);
        startActivity(i);
        finish();
    }
}
