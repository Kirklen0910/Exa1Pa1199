package com.uth.exa1pa1199.Views;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.uth.exa1pa1199.Controllers.ContactoController;
import com.uth.exa1pa1199.Models.Contacto;
import com.uth.exa1pa1199.R;

import java.io.File;

import android.widget.ArrayAdapter;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_CONTACTO_ID = "contacto_id";

    private Spinner spinnerPais;
    private TextInputEditText editNombre, editTelefono, editNota;
    private ImageView imagePreview;
    private Button buttonElegirImagen, buttonTomarFoto, buttonGuardar, buttonVerLista;

    private Uri imagenSeleccionadaUri;
    private ContactoController controller;
    private int contactoId = -1;

    private ActivityResultLauncher<Intent> galeriaLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private Uri uriFotoTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        controller = new ContactoController(this);

        spinnerPais = findViewById(R.id.spinnerPais);
        editNombre = findViewById(R.id.editNombre);
        editTelefono = findViewById(R.id.editTelefono);
        editNota = findViewById(R.id.editNota);
        imagePreview = findViewById(R.id.imagePreview);
        buttonElegirImagen = findViewById(R.id.buttonElegirImagen);
        buttonTomarFoto = findViewById(R.id.buttonTomarFoto);
        buttonGuardar = findViewById(R.id.buttonGuardar);
        buttonVerLista = findViewById(R.id.buttonVerLista);

        configurarSpinner();

        registrarGaleriaLauncher();
        registrarCameraLauncher();

        buttonElegirImagen.setOnClickListener(v -> abrirGaleria());
        buttonTomarFoto.setOnClickListener(v -> verificarPermisoCamara());
        buttonGuardar.setOnClickListener(v -> guardarContacto());
        buttonVerLista.setOnClickListener(v -> {
            Intent intent = new Intent(this, ContactListActivity.class);
            startActivity(intent);
        });

        contactoId = getIntent().getIntExtra(EXTRA_CONTACTO_ID, -1);
        if (contactoId != -1) {
            cargarContacto(contactoId);
        }
    }

    private void configurarSpinner() {
        String[] paises = {
                "Honduras (504)",
                "Costa Rica (506)",
                "Guatemala (502)",
                "El Salvador (503)"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, paises);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPais.setAdapter(adapter);
    }

    private void registrarGaleriaLauncher() {
        galeriaLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imagenSeleccionadaUri = result.getData().getData();
                        imagePreview.setImageURI(imagenSeleccionadaUri);
                    }
                }
        );
    }

    private void registrarCameraLauncher() {
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (uriFotoTemp != null) {
                            imagenSeleccionadaUri = uriFotoTemp;
                            imagePreview.setImageURI(imagenSeleccionadaUri);
                        }
                    }
                }
        );
    }

    private void abrirGaleria() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        galeriaLauncher.launch(intent);
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, 100);
        } else {
            abrirCamara();
        }
    }

    private void abrirCamara() {
        try {
            File archivo = new File(getCacheDir(), "foto_temp.png");
            uriFotoTemp = FileProvider.getUriForFile(this,
                    getPackageName() + ".provider", archivo);

            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriFotoTemp);
            cameraLauncher.launch(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void guardarContacto() {
        String seleccion = spinnerPais.getSelectedItem().toString();
        String pais = seleccion.split(" ")[0]; // Honduras
        String codigoArea = seleccion.replaceAll("[^0-9+]", ""); // +504

        String nombre = editNombre.getText().toString().trim();
        String telefono = editTelefono.getText().toString().trim();
        String nota = editNota.getText().toString().trim();

        // 👉 Validaciones individuales
        if (nombre.isEmpty()) {
            Toast.makeText(this, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show();
            return;
        }
        if (telefono.isEmpty() || telefono.length() != 8) {
            Toast.makeText(this, "El teléfono debe tener exactamente 8 dígitos", Toast.LENGTH_SHORT).show();
            return;
        }
        if (nota.isEmpty()) {
            Toast.makeText(this, "La nota no puede estar vacía", Toast.LENGTH_SHORT).show();
            return;
        }

        Contacto contacto = new Contacto(pais, codigoArea, nombre, telefono, nota,
                imagenSeleccionadaUri != null ? imagenSeleccionadaUri.toString() : null);

        if (contactoId == -1) {
            controller.guardarContacto(contacto);
            Toast.makeText(this, "Contacto guardado", Toast.LENGTH_SHORT).show();
        } else {
            contacto.setId(contactoId);
            controller.actualizarContacto(contacto);
            Toast.makeText(this, "Contacto actualizado", Toast.LENGTH_SHORT).show();
        }

        limpiarCampos(); // 👉 limpia todo después de guardar
    }

    private void limpiarCampos() {
        spinnerPais.setSelection(0);
        editNombre.setText("");
        editTelefono.setText("");
        editNota.setText("");
        imagePreview.setImageResource(android.R.drawable.ic_menu_gallery);
        imagenSeleccionadaUri = null;
        contactoId = -1;
    }

    private void cargarContacto(int id) {
        Contacto contacto = controller.obtenerContactoPorId(id);
        if (contacto != null) {
            String seleccion = contacto.getPais() + " (" + contacto.getCodigoArea() + ")";
            for (int i = 0; i < spinnerPais.getCount(); i++) {
                if (spinnerPais.getItemAtPosition(i).toString().equals(seleccion)) {
                    spinnerPais.setSelection(i);
                    break;
                }
            }

            editNombre.setText(contacto.getNombre());
            editTelefono.setText(contacto.getTelefono());
            editNota.setText(contacto.getNota());
            if (contacto.getImagenUri() != null) {
                imagenSeleccionadaUri = Uri.parse(contacto.getImagenUri());
                imagePreview.setImageURI(imagenSeleccionadaUri);
            }
        }
    }
}
