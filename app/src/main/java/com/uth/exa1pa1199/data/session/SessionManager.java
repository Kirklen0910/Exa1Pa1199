package com.uth.exa1pa1199.data.session;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.uth.exa1pa1199.util.FechaUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;

public final class SessionManager {
    public static final String ROL_ADMIN = "Admin";

    static final String ARCHIVO = "sesion_segura";
    private static final String CLAVE_TOKEN = "token";
    private static final String CLAVE_EXPIRACION = "expiracion";
    private static final String CLAVE_ROL = "rol";
    private static final String CLAVE_CORREO = "correo";

    private static volatile SessionManager instancia;

    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        this.prefs = crearPreferencias(context.getApplicationContext());
    }

    public static void inicializar(Context context) {
        if (instancia == null) {
            synchronized (SessionManager.class) {
                if (instancia == null) instancia = new SessionManager(context);
            }
        }
    }

    public static SessionManager get() {
        if (instancia == null) {
            throw new IllegalStateException("SessionManager no fue inicializado en BitacoraApp.");
        }
        return instancia;
    }

    private static SharedPreferences crearPreferencias(Context context) {
        try {
            return abrir(context);
        } catch (GeneralSecurityException | IOException e) {
            context.deleteSharedPreferences(ARCHIVO);
            try {
                return abrir(context);
            } catch (GeneralSecurityException | IOException e2) {
                throw new IllegalStateException("No se pudo abrir el almacenamiento cifrado de la sesión.", e2);
            }
        }
    }

    private static SharedPreferences abrir(Context context) throws GeneralSecurityException, IOException {
        MasterKey clave = new MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build();
        return EncryptedSharedPreferences.create(
                context,
                ARCHIVO,
                clave,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
    }

    public synchronized void guardar(String token, String expiracionIso, String rol, String correo) {
        prefs.edit()
                .putString(CLAVE_TOKEN, token)
                .putLong(CLAVE_EXPIRACION, FechaUtils.aMilisegundos(expiracionIso))
                .putString(CLAVE_ROL, rol)
                .putString(CLAVE_CORREO, correo)
                .commit();
    }

    public synchronized void cerrar() {
        prefs.edit().clear().commit();
    }

    public String getToken() { return prefs.getString(CLAVE_TOKEN, null); }
    public long getExpiracion() { return prefs.getLong(CLAVE_EXPIRACION, -1); }
    public String getRol() { return prefs.getString(CLAVE_ROL, null); }
    public String getCorreo() { return prefs.getString(CLAVE_CORREO, null); }

    public boolean esAdmin() { return ROL_ADMIN.equals(getRol()); }

    public boolean haySesion() { return getToken() != null; }

    public boolean estaVencida() {
        long expiracion = getExpiracion();
        return expiracion > 0 && System.currentTimeMillis() >= expiracion;
    }

    public boolean haySesionValida() { return haySesion() && !estaVencida(); }
}
