package com.uth.exa1pa1199;

import android.app.Application;

import com.uth.exa1pa1199.data.session.SessionManager;

public class BitacoraApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SessionManager.inicializar(this);
    }
}
