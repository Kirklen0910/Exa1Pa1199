package com.uth.exa1pa1199.viewmodel;

import android.graphics.Bitmap;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;
import com.uth.exa1pa1199.data.session.SessionManager;
import com.uth.exa1pa1199.util.ImagenUtils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DetalleRegistroViewModel extends ViewModel {
    private final RegistroRepository repositorio;
    private final SessionManager sesion;
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<UiState<RegistroDetalleDto>> estado = new MutableLiveData<>();
    private final MutableLiveData<UiState<Void>> estadoEliminar = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> foto = new MutableLiveData<>();

    private Cancelable peticionDetalle;
    private Cancelable peticionEliminar;
    private String id;

    public DetalleRegistroViewModel() {
        this(new RegistroRepository(), SessionManager.get());
    }

    public DetalleRegistroViewModel(RegistroRepository repositorio, SessionManager sesion) {
        this.repositorio = repositorio;
        this.sesion = sesion;
    }

    public boolean puedeEliminar() { return sesion.esAdmin(); }

    public LiveData<UiState<RegistroDetalleDto>> getEstado() { return estado; }
    public LiveData<UiState<Void>> getEstadoEliminar() { return estadoEliminar; }

    public LiveData<Bitmap> getFoto() { return foto; }

    public void setId(String id) { this.id = id; }

    public void cargar() {
        if (peticionDetalle != null) peticionDetalle.cancelar();
        UiState<RegistroDetalleDto> actual = estado.getValue();
        estado.setValue(UiState.cargando(actual != null ? actual.getDatos() : null));

        peticionDetalle = repositorio.obtenerRegistro(id, new RepositorioCallback<RegistroDetalleDto>() {
            @Override
            public void onExito(RegistroDetalleDto datos) {
                if (datos == null) {
                    estado.setValue(UiState.vacio());
                    return;
                }
                estado.setValue(UiState.exito(datos));

                String base64 = datos.getFotoBase64();
                ejecutor.execute(() -> foto.postValue(ImagenUtils.decodificarBase64(base64)));
            }

            @Override
            public void onError(ApiError error) {
                estado.setValue(UiState.error(error));
            }
        });
    }

    public void eliminar() {
        if (peticionEliminar != null) peticionEliminar.cancelar();
        estadoEliminar.setValue(UiState.cargando(null));
        peticionEliminar = repositorio.eliminarRegistro(id, new RepositorioCallback<Void>() {
            @Override
            public void onExito(Void datos) {
                estadoEliminar.setValue(UiState.exito(null));
            }

            @Override
            public void onError(ApiError error) {
                estadoEliminar.setValue(UiState.error(error));
            }
        });
    }

    @Override
    protected void onCleared() {
        if (peticionDetalle != null) peticionDetalle.cancelar();
        if (peticionEliminar != null) peticionEliminar.cancelar();
        ejecutor.shutdownNow();
    }
}
