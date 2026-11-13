package com.uth.exa1pa1199.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.exa1pa1199.data.remote.dto.TokenDto;
import com.uth.exa1pa1199.data.remote.dto.UsuarioDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.AuthRepository;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;

import java.util.HashMap;
import java.util.Map;

public class RegistroUsuarioViewModel extends ViewModel {
    public static final String CAMPO_CORREO = "correo";
    public static final String CAMPO_PASSWORD = "password";
    public static final String CAMPO_CONFIRMACION = "confirmacion";

    public static final int PASSWORD_MIN = 8;
    public static final int PASSWORD_MAX = 100;
    public static final int CORREO_MAX = 256;

    private final AuthRepository repositorio;
    private final MutableLiveData<UiState<Void>> estado = new MutableLiveData<>();
    private final MutableLiveData<Map<String, String>> erroresValidacion = new MutableLiveData<>();
    private Cancelable peticion;
    private String correo;
    private String password;
    private boolean cuentaCreada;

    public RegistroUsuarioViewModel() {
        this(new AuthRepository());
    }

    public RegistroUsuarioViewModel(AuthRepository repositorio) {
        this.repositorio = repositorio;
    }

    public LiveData<UiState<Void>> getEstado() { return estado; }
    public LiveData<Map<String, String>> getErroresValidacion() { return erroresValidacion; }

    public static Map<String, String> validar(String correo, String password, String confirmacion) {
        Map<String, String> errores = new HashMap<>();
        if (correo == null || correo.trim().isEmpty()) {
            errores.put(CAMPO_CORREO, "El correo es obligatorio.");
        } else if (correo.trim().length() > CORREO_MAX) {
            errores.put(CAMPO_CORREO, "El correo no puede superar " + CORREO_MAX + " caracteres.");
        } else if (!LoginViewModel.correoValido(correo)) {
            errores.put(CAMPO_CORREO, "El correo no tiene un formato válido.");
        }

        int lp = password == null ? 0 : password.length();
        if (lp == 0) {
            errores.put(CAMPO_PASSWORD, "La contraseña es obligatoria.");
        } else if (lp < PASSWORD_MIN || lp > PASSWORD_MAX) {
            errores.put(CAMPO_PASSWORD, "La contraseña debe tener entre " + PASSWORD_MIN + " y "
                    + PASSWORD_MAX + " caracteres (tiene " + lp + ").");
        }

        if (confirmacion == null || !confirmacion.equals(password)) {
            errores.put(CAMPO_CONFIRMACION, "Las contraseñas no coinciden.");
        }
        return errores;
    }

    public void registrar(String correo, String password, String confirmacion) {
        Map<String, String> errores = validar(correo, password, confirmacion);
        erroresValidacion.setValue(errores);
        if (!errores.isEmpty()) return;

        this.correo = correo.trim();
        this.password = password;
        this.cuentaCreada = false;
        enviar();
    }

    public void reintentar() {
        if (correo != null && password != null) enviar();
    }

    private void enviar() {
        if (peticion != null) peticion.cancelar();
        estado.setValue(UiState.cargando(null));
        if (cuentaCreada) {
            iniciarSesion();
            return;
        }
        peticion = repositorio.registrar(correo, password, new RepositorioCallback<UsuarioDto>() {
            @Override
            public void onExito(UsuarioDto datos) {
                cuentaCreada = true;
                iniciarSesion();
            }

            @Override
            public void onError(ApiError error) {
                estado.setValue(UiState.error(error));
            }
        });
    }

    private void iniciarSesion() {
        peticion = repositorio.iniciarSesion(correo, password, new RepositorioCallback<TokenDto>() {
            @Override
            public void onExito(TokenDto datos) {
                password = null;
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
