package com.uth.exa1pa1199.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.AuthRepository;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;
import com.uth.exa1pa1199.data.session.SessionManager;

import java.util.List;

public class ListaRegistrosViewModel extends ViewModel {
    private final RegistroRepository repositorio;
    private final AuthRepository authRepositorio;
    private final SessionManager sesion;
    private final MutableLiveData<UiState<List<RegistroResumenDto>>> estado = new MutableLiveData<>();
    private Cancelable peticion;

    public ListaRegistrosViewModel() {
        this(new RegistroRepository(), new AuthRepository(), SessionManager.get());
    }

    public ListaRegistrosViewModel(RegistroRepository repositorio, AuthRepository authRepositorio,
                                   SessionManager sesion) {
        this.repositorio = repositorio;
        this.authRepositorio = authRepositorio;
        this.sesion = sesion;
    }

    public LiveData<UiState<List<RegistroResumenDto>>> getEstado() {
        return estado;
    }

    public String getCorreo() { return sesion.getCorreo(); }
    public String getRol() { return sesion.getRol(); }

    public void cerrarSesion() {
        if (peticion != null) peticion.cancelar();
        authRepositorio.cerrarSesion();
    }

    public void cargar() {
        if (peticion != null) peticion.cancelar();
        UiState<List<RegistroResumenDto>> actual = estado.getValue();
        estado.setValue(UiState.cargando(actual != null ? actual.getDatos() : null));

        peticion = repositorio.obtenerRegistros(new RepositorioCallback<List<RegistroResumenDto>>() {
            @Override
            public void onExito(List<RegistroResumenDto> datos) {
                if (datos == null || datos.isEmpty()) {
                    estado.setValue(UiState.vacio());
                } else {
                    estado.setValue(UiState.exito(datos));
                }
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
