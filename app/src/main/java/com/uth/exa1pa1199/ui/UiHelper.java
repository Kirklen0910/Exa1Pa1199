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

/** Utilidades de presentación compartidas por las Activities. */
final class UiHelper {

    private UiHelper() { }

    /**
     * Activa edge-to-edge en todas las versiones (en Android 15+ con targetSdk 35+ es obligatorio) y ajusta la
     * pantalla para que nada quede tapado. Llamar en onCreate antes de setContentView.
     */
    static void activarEdgeToEdge(AppCompatActivity activity) {
        EdgeToEdge.enable(activity);
    }

    /**
     * Configura la barra de título (R.id.toolbar de view_toolbar.xml) y aplica los WindowInsets. Llamar justo
     * después de setContentView y antes de setTitle. Requiere que el layout raíz tenga R.id.root y R.id.appBar.
     *
     * <ul>
     *   <li>AppBarLayout: padding superior = barra de estado / notch, así el título queda debajo de ella.</li>
     *   <li>Raíz: padding izquierdo/derecho/inferior = barra de navegación o teclado (el mayor de los dos),
     *       así el FAB, los botones y los campos del formulario no quedan debajo de ellos.</li>
     * </ul>
     * Como el contenido va debajo de la AppBar dentro de un LinearLayout vertical, siempre empieza bajo el título.
     */
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
            // getInsets con varios tipos devuelve el máximo por lado: con el teclado abierto, su altura.
            Insets abajo = insets.getInsets(WindowInsetsCompat.Type.systemBars()
                    | WindowInsetsCompat.Type.displayCutout()
                    | WindowInsetsCompat.Type.ime());

            appBar.setPadding(0, barras.top, 0, 0);
            v.setPadding(barras.left, 0, barras.right, abajo.bottom);
            // Ya se aplicaron aquí: consumirlos evita que algún hijo los vuelva a sumar.
            return WindowInsetsCompat.CONSUMED;
        });
    }

    /** Título comprensible para cada tipo de error. */
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

    /**
     * Pinta la vista view_estado_error. Reintentar solo aparece en Sin conexión y Tiempo agotado.
     */
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
