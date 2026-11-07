package com.uth.exa1pa1199.viewmodel;

import android.app.Application;
import android.graphics.Bitmap;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroRequestDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;
import com.uth.exa1pa1199.util.ImagenUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FormularioRegistroViewModel extends AndroidViewModel {
    public static final String CAMPO_TITULO = "titulo";
    public static final String CAMPO_DESCRIPCION = "descripcion";
    public static final String CAMPO_FOTO = "fotobase64";

    public static final int TITULO_MIN = 3;
    public static final int TITULO_MAX = 100;
    public static final int DESCRIPCION_MIN = 1;
    public static final int DESCRIPCION_MAX = 500;

    private final RegistroRepository repositorio;
    private final ExecutorService ejecutor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<UiState<RegistroDetalleDto>> estadoCarga = new MutableLiveData<>();

    private final MutableLiveData<UiState<Void>> estadoGuardar = new MutableLiveData<>();

    private final MutableLiveData<Map<String, String>> erroresValidacion = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> fotoPreview = new MutableLiveData<>();
    private final MutableLiveData<Boolean> procesandoFoto = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorFoto = new MutableLiveData<>();

    private String id;
    private String fotoBase64;
    private boolean datosAplicados;
    private RegistroRequestDto ultimaSolicitud;
    private Cancelable peticionCarga;
    private Cancelable peticionGuardar;

    public FormularioRegistroViewModel(@NonNull Application application) {
        super(application);
        this.repositorio = new RegistroRepository();
    }

    public LiveData<UiState<RegistroDetalleDto>> getEstadoCarga() { return estadoCarga; }
    public LiveData<UiState<Void>> getEstadoGuardar() { return estadoGuardar; }
    public LiveData<Map<String, String>> getErroresValidacion() { return erroresValidacion; }
    public LiveData<Bitmap> getFotoPreview() { return fotoPreview; }
    public LiveData<Boolean> getProcesandoFoto() { return procesandoFoto; }
    public LiveData<String> getErrorFoto() { return errorFoto; }

    public boolean esEdicion() { return id != null; }

    public void iniciar(String id) {
        if (this.id != null || estadoCarga.getValue() != null) return;
        this.id = id;
        if (id != null) cargarRegistro();
    }

    public boolean consumirDatosParaCampos() {
        if (datosAplicados) return false;
        datosAplicados = true;
        return true;
    }

    public void cargarRegistro() {
        if (peticionCarga != null) peticionCarga.cancelar();
        estadoCarga.setValue(UiState.cargando(null));
        peticionCarga = repositorio.obtenerRegistro(id, new RepositorioCallback<RegistroDetalleDto>() {
            @Override
            public void onExito(RegistroDetalleDto datos) {
                if (datos == null) {
                    estadoCarga.setValue(UiState.vacio());
                    return;
                }
                fotoBase64 = datos.getFotoBase64();
                String base64 = fotoBase64;
                ejecutor.execute(() -> fotoPreview.postValue(ImagenUtils.decodificarBase64(base64)));
                estadoCarga.setValue(UiState.exito(datos));
            }

            @Override
            public void onError(ApiError error) {
                estadoCarga.setValue(UiState.error(error));
            }
        });
    }

    public void procesarFoto(Uri uri) {
        procesandoFoto.setValue(true);
        ejecutor.execute(() -> {
            try {
                ImagenUtils.FotoProcesada r = ImagenUtils.reducirYCodificar(
                        getApplication().getContentResolver(), uri);
                fotoBase64 = r.base64;
                fotoPreview.postValue(r.bitmap);
            } catch (IOException | RuntimeException e) {
                errorFoto.postValue("No se pudo procesar la foto: " + e.getMessage());
            } finally {
                procesandoFoto.postValue(false);
            }
        });
    }

    public void limpiarErrorFoto() {
        errorFoto.setValue(null);
    }

    public static Map<String, String> validar(String titulo, String descripcion) {
        Map<String, String> errores = new HashMap<>();
        int lt = titulo == null ? 0 : titulo.trim().length();
        int ld = descripcion == null ? 0 : descripcion.trim().length();

        if (lt == 0) {
            errores.put(CAMPO_TITULO, "El título es obligatorio.");
        } else if (lt < TITULO_MIN || lt > TITULO_MAX) {
            errores.put(CAMPO_TITULO, "El título debe tener entre " + TITULO_MIN + " y " + TITULO_MAX
                    + " caracteres (tiene " + lt + ").");
        }

        if (ld < DESCRIPCION_MIN) {
            errores.put(CAMPO_DESCRIPCION, "La descripción es obligatoria.");
        } else if (ld > DESCRIPCION_MAX) {
            errores.put(CAMPO_DESCRIPCION, "La descripción no puede superar " + DESCRIPCION_MAX
                    + " caracteres (tiene " + ld + ").");
        }
        return errores;
    }

    public void guardar(String titulo, String descripcion) {
        Map<String, String> errores = validar(titulo, descripcion);
        erroresValidacion.setValue(errores);
        if (!errores.isEmpty()) return;

        ultimaSolicitud = new RegistroRequestDto(titulo.trim(), descripcion.trim(), fotoBase64);
        enviar();
    }

    public void reintentarGuardar() {
        if (ultimaSolicitud != null) enviar();
    }

    private void enviar() {
        if (peticionGuardar != null) peticionGuardar.cancelar();
        estadoGuardar.setValue(UiState.cargando(null));

        if (id == null) {
            peticionGuardar = repositorio.crearRegistro(ultimaSolicitud, new RepositorioCallback<RegistroDetalleDto>() {
                @Override
                public void onExito(RegistroDetalleDto datos) {
                    estadoGuardar.setValue(UiState.exito(null));
                }

                @Override
                public void onError(ApiError error) {
                    estadoGuardar.setValue(UiState.error(error));
                }
            });
        } else {
            peticionGuardar = repositorio.actualizarRegistro(id, ultimaSolicitud, new RepositorioCallback<Void>() {
                @Override
                public void onExito(Void datos) {
                    estadoGuardar.setValue(UiState.exito(null));
                }

                @Override
                public void onError(ApiError error) {
                    estadoGuardar.setValue(UiState.error(error));
                }
            });
        }
    }

    @Override
    protected void onCleared() {
        if (peticionCarga != null) peticionCarga.cancelar();
        if (peticionGuardar != null) peticionGuardar.cancelar();
        ejecutor.shutdownNow();
    }
}
