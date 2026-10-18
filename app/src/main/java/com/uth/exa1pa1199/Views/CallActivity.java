package com.uth.exa1pa1199.Views;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.uth.exa1pa1199.R;

public class CallActivity extends AppCompatActivity {
    private TextView textTelefono;
    private Button buttonConfirmarLlamada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        textTelefono = findViewById(R.id.textTelefono);
        buttonConfirmarLlamada = findViewById(R.id.buttonConfirmarLlamada);

        String codigoArea = getIntent().getStringExtra("codigo_area");
        String telefono = getIntent().getStringExtra("telefono");
        String numeroCompleto = codigoArea + telefono;

        textTelefono.setText(numeroCompleto);

        buttonConfirmarLlamada.setOnClickListener(v -> {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + numeroCompleto));
            startActivity(dialIntent);
        });
    }
}
