package com.uth.exa1pa1199.Controllers;

import android.content.Context;
import com.uth.exa1pa1199.Database.ContactoDbHelper;
import com.uth.exa1pa1199.Models.Contacto;  // CORREGIDO: Models con M mayúscula
import java.util.List;

public class ContactoController {
    private final ContactoDbHelper dbHelper;

    public ContactoController(Context context) {
        dbHelper = new ContactoDbHelper(context);
    }

    public long guardarContacto(Contacto contacto) {
        return dbHelper.insertarContacto(contacto);
    }

    public int actualizarContacto(Contacto contacto) {
        return dbHelper.actualizarContacto(contacto);
    }

    public int eliminarContacto(int id) {
        return dbHelper.eliminarContacto(id);
    }

    public Contacto obtenerContactoPorId(int id) {
        return dbHelper.obtenerContactoPorId(id);
    }

    public List<Contacto> obtenerContactos() {
        return dbHelper.obtenerContactos();
    }
}
