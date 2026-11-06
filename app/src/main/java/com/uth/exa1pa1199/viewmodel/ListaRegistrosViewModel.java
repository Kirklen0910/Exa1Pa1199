package com.uth.exa1pa1199.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;

import java.util.List;

public class ListaRegistrosViewModel extends ViewModel {

    private final RegistroRepository repositorio;
    private final MutableLiveData<UiState<List<RegistroResumenDto>>> estado = new MutableLiveData<>();
    private Cancelable peticion;

    public ListaRegistrosViewModel() {
        this(new RegistroRepository());
    }

    public ListaRegistrosViewModel(RegistroRepository repositorio) {
        this.repositorio = repositorio;
    }

    public LiveData<UiState<List<RegistroResumenDto>>> getEstado() {
        return estado;
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
