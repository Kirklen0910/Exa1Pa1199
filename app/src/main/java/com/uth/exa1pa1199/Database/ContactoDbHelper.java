package com.uth.exa1pa1199.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uth.exa1pa1199.Models.Contacto;

import java.util.ArrayList;
import java.util.List;

public class ContactoDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "contactos.db";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_CONTACTOS = "contactos";
    public static final String COL_ID = "id";
    public static final String COL_PAIS = "pais";
    public static final String COL_CODIGO_AREA = "codigo_area";
    public static final String COL_NOMBRE = "nombre";
    public static final String COL_TELEFONO = "telefono";
    public static final String COL_NOTA = "nota";
    public static final String COL_IMAGEN = "imagen_uri";

    public ContactoDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_CONTACTOS + " ("
                + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COL_PAIS + " TEXT NOT NULL, "
                + COL_CODIGO_AREA + " TEXT NOT NULL, "
                + COL_NOMBRE + " TEXT NOT NULL, "
                + COL_TELEFONO + " TEXT NOT NULL, "
                + COL_NOTA + " TEXT, "
                + COL_IMAGEN + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONTACTOS);
        onCreate(db);
    }

    public long insertarContacto(Contacto contacto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PAIS, contacto.getPais());
        values.put(COL_CODIGO_AREA, contacto.getCodigoArea());
        values.put(COL_NOMBRE, contacto.getNombre());
        values.put(COL_TELEFONO, contacto.getTelefono());
        values.put(COL_NOTA, contacto.getNota());
        values.put(COL_IMAGEN, contacto.getImagenUri());
        return db.insert(TABLE_CONTACTOS, null, values);
    }

    public int actualizarContacto(Contacto contacto) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PAIS, contacto.getPais());
        values.put(COL_CODIGO_AREA, contacto.getCodigoArea());
        values.put(COL_NOMBRE, contacto.getNombre());
        values.put(COL_TELEFONO, contacto.getTelefono());
        values.put(COL_NOTA, contacto.getNota());
        values.put(COL_IMAGEN, contacto.getImagenUri());
        return db.update(TABLE_CONTACTOS, values, COL_ID + "=?", new String[]{String.valueOf(contacto.getId())});
    }

    public int eliminarContacto(int id) {
        SQLiteDatabase db = getWritableDatabase();
        return db.delete(TABLE_CONTACTOS, COL_ID + "=?", new String[]{String.valueOf(id)});
    }

    public Contacto obtenerContactoPorId(int id) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTOS, null, COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        Contacto contacto = null;
        if (cursor.moveToFirst()) {
            contacto = mapCursor(cursor);
        }
        cursor.close();
        return contacto;
    }

    public List<Contacto> obtenerContactos() {
        List<Contacto> contactos = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CONTACTOS, null, null, null, null, null, COL_NOMBRE + " COLLATE NOCASE ASC");
        try {
            while (cursor.moveToNext()) {
                contactos.add(mapCursor(cursor));
            }
        } finally {
            cursor.close();
        }
        return contactos;
    }

    private Contacto mapCursor(Cursor cursor) {
        Contacto contacto = new Contacto();
        contacto.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)));
        contacto.setPais(cursor.getString(cursor.getColumnIndexOrThrow(COL_PAIS)));
        contacto.setCodigoArea(cursor.getString(cursor.getColumnIndexOrThrow(COL_CODIGO_AREA)));
        contacto.setNombre(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOMBRE)));
        contacto.setTelefono(cursor.getString(cursor.getColumnIndexOrThrow(COL_TELEFONO)));
        contacto.setNota(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTA)));
        contacto.setImagenUri(cursor.getString(cursor.getColumnIndexOrThrow(COL_IMAGEN)));
        return contacto;
    }
}
