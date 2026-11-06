package com.uth.exa1pa1199.ui;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.uth.exa1pa1199.R;
import com.uth.exa1pa1199.viewmodel.UiState;

final class UiHelper {

    private UiHelper() { }

    static void activarEdgeToEdge(AppCompatActivity activity) {
        EdgeToEdge.enable(activity);
    }

    static void configurarPantalla(AppCompatActivity activity, boolean conBotonVolver) {
        MaterialToolbar toolbar = activity.findViewById(R.id.toolbar);
        activity.setSupportActionBar(toolbar);
        if (conBotonVolver && activity.getSupportActionBar() != null) {
            activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        View root = activity.findViewById(R.id.root);
        View appBar = activity.findViewById(R.id.appBar);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets barras = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout());
            Insets abajo = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());

            appBar.setPadding(0, barras.top, 0, 0);
            v.setPadding(barras.left, 0, barras.right, abajo.bottom);
            return WindowInsetsCompat.CONSUMED;
        });
    }

    static String tituloError(UiState<?> estado) {
        switch (estado.getEstado()) {
            case SIN_CONEXION: return "Sin conexión";
            case TIEMPO_AGOTADO: return "Tiempo de espera agotado";
            default:
                return estado.getCodigoHttp() > 0
                        ? "Error del servidor (" + estado.getCodigoHttp() + ")"
                        : "Error del servidor";
        }
    }

    static void mostrarError(View layoutError, UiState<?> estado, Runnable reintentar) {
        TextView titulo = layoutError.findViewById(R.id.textErrorTitulo);
        TextView mensaje = layoutError.findViewById(R.id.textErrorMensaje);
        Button boton = layoutError.findViewById(R.id.buttonReintentar);

        layoutError.setVisibility(View.VISIBLE);
        titulo.setText(tituloError(estado));
        mensaje.setText(estado.getMensaje());
        boton.setVisibility(estado.permiteReintentar() ? View.VISIBLE : View.GONE);
        boton.setOnClickListener(v -> reintentar.run());
    }
}
