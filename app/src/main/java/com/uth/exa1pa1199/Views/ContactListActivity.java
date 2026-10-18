package com.uth.exa1pa1199.Views;

import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.uth.exa1pa1199.Adapters.ContactoAdapter;
import com.uth.exa1pa1199.Database.ContactoDbHelper;
import com.uth.exa1pa1199.Models.Contacto;
import com.uth.exa1pa1199.R;

import java.util.List;

public class ContactListActivity extends AppCompatActivity {
    private ListView listViewContactos;
    private TextView textEmpty;
    private ContactoDbHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_list);

        listViewContactos = findViewById(R.id.listViewContactos);
        textEmpty = findViewById(R.id.textEmpty);
        dbHelper = new ContactoDbHelper(this);

        cargarContactos();
    }

    private void cargarContactos() {
        List<Contacto> contactos = dbHelper.obtenerContactos();
        if (contactos.isEmpty()) {
            textEmpty.setVisibility(TextView.VISIBLE);
        } else {
            textEmpty.setVisibility(TextView.GONE);
            ContactoAdapter adapter = new ContactoAdapter(this, contactos);
            listViewContactos.setAdapter(adapter);
        }
    }
}
