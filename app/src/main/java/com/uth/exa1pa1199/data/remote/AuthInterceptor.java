package com.uth.exa1pa1199.data.remote;

import androidx.annotation.NonNull;

import com.uth.exa1pa1199.data.session.SessionManager;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {
    static final String RUTA_AUTH = "/api/auth/";

    private final SessionManager sesion;

    public AuthInterceptor(SessionManager sesion) {
        this.sesion = sesion;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        if (original.url().encodedPath().startsWith(RUTA_AUTH)) {
            return chain.proceed(original);
        }

        String token = sesion.getToken();
        Request request = token == null
                ? original
                : original.newBuilder().header("Authorization", "Bearer " + token).build();

        Response response = chain.proceed(request);
        if (response.code() == 401 && token != null && token.equals(sesion.getToken())) {
            sesion.cerrar();
        }
        return response;
    }
}
