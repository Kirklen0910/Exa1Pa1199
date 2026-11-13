package com.uth.exa1pa1199.viewmodel;

import android.util.Patterns;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.exa1pa1199.data.remote.dto.TokenDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.AuthRepository;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;

import java.util.HashMap;
import java.util.Map;

public class LoginViewModel extends ViewModel {
    public static final String CAMPO_CORREO = "correo";
    public static final String CAMPO_PASSWORD = "password";

    private final AuthRepository repositorio;
    private final MutableLiveData<UiState<Void>> estado = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> erroresValidacion = new MutableLiveData<>();
    private Cancelable peticion;
    private String ultimoCorreo;
    private String ultimaPassword;

    public LoginViewModel() {
        this(new AuthRepository());
    }

    public LoginViewModel(AuthRepository repositorio) {
        this.repositorio = repositorio;
    }

    public LiveData<UiState<Void>> getEstado() { return estado; }
    public LiveData<Map<String, String>> getErroresValidacion() { return erroresValidacion; }

    public static boolean correoValido(String correo) {
        return correo != null && Patterns.EMAIL_ADDRESS.matcher(correo.trim()).matches();
    }

    public static Map<String, String> validar(String correo, String password) {
        Map<String, String> errores = new HashMap<>();
        if (correo == null || correo.trim().isEmpty()) {
            errores.put(CAMPO_CORREO, "El correo es obligatorio.");
        } else if (!correoValido(correo)) {
            errores.put(CAMPO_CORREO, "El correo no tiene un formato válido.");
        }
        if (password == null || password.isEmpty()) {
            errores.put(CAMPO_PASSWORD, "La contraseña es obligatoria.");
        }
        return errores;
    }

    public void iniciarSesion(String correo, String password) {
        Map<String, String> errores = validar(correo, password);
        erroresValidacion.setValue(errores);
        if (!errores.isEmpty()) return;

        ultimoCorreo = correo.trim();
        ultimaPassword = password;
        enviar();
    }

    public void reintentar() {
        if (ultimoCorreo != null) enviar();
    }

    private void enviar() {
        if (peticion != null) peticion.cancelar();
        estado.setValue(UiState.cargando(null));
        peticion = repositorio.iniciarSesion(ultimoCorreo, ultimaPassword, new RepositorioCallback<TokenDto>() {
            @Override
            public void onExito(TokenDto datos) {
                ultimaPassword = null;
                estado.setValue(UiState.exito(null));
            }

            @Override
            public void onError(ApiError error) {
                estado.setValue(UiState.error(error));
            }
        });
    }

    @Override
    protected void onCleared() {
        if (peticion != null) peticion.cancelar();
    }
}
