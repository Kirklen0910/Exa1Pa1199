package com.uth.exa1pa1199.viewmodel;

import com.uth.exa1pa1199.data.repository.ApiError;

import java.util.Collections;
import java.util.Map;

/**
 * Estado de una pantalla, expuesto por los ViewModel mediante LiveData.
 * La Activity solo lo observa y pinta la vista correspondiente.
 */
public final class UiState<T> {

    public enum Estado {
        /** Petición en curso: ProgressBar visible y botones deshabilitados. */
        CARGANDO,
        /** Respuesta correcta con datos. */
        EXITO,
        /** Respuesta correcta pero sin registros. */
        VACIO,
        /** El servidor respondió 4xx o 5xx. */
        ERROR_SERVIDOR,
        /** No hay conexión o la API está apagada (IOException). Se ofrece Reintentar. */
        SIN_CONEXION,
        /** Se agotó el tiempo de espera (SocketTimeoutException). Se ofrece Reintentar. */
        TIEMPO_AGOTADO
    }

    private final Estado estado;
    private final T datos;
    private final String mensaje;
    private final int codigoHttp;
    private final Map<String, String> erroresCampo;

    private UiState(Estado estado, T datos, String mensaje, int codigoHttp, Map<String, String> erroresCampo) {
        this.estado = estado;
        this.datos = datos;
        this.mensaje = mensaje;
        this.codigoHttp = codigoHttp;
        this.erroresCampo = erroresCampo == null ? Collections.emptyMap() : erroresCampo;
    }

    /** @param datosPrevios datos que se siguen mostrando mientras se recarga (puede ser null). */
    public static <T> UiState<T> cargando(T datosPrevios) {
        return new UiState<>(Estado.CARGANDO, datosPrevios, null, 0, null);
    }

    public static <T> UiState<T> exito(T datos) {
        return new UiState<>(Estado.EXITO, datos, null, 0, null);
    }

    public static <T> UiState<T> vacio() {
        return new UiState<>(Estado.VACIO, null, null, 0, null);
    }

    public static <T> UiState<T> error(ApiError error) {
        Estado e;
        switch (error.getTipo()) {
            case SIN_CONEXION: e = Estado.SIN_CONEXION; break;
            case TIEMPO_AGOTADO: e = Estado.TIEMPO_AGOTADO; break;
            default: e = Estado.ERROR_SERVIDOR; break;
        }
        return new UiState<>(e, null, error.getMensaje(), error.getCodigoHttp(), error.getErroresCampo());
    }

    public Estado getEstado() { return estado; }
    public T getDatos() { return datos; }
    public String getMensaje() { return mensaje; }
    public int getCodigoHttp() { return codigoHttp; }
    public Map<String, String> getErroresCampo() { return erroresCampo; }

    public boolean esCargando() { return estado == Estado.CARGANDO; }

    public boolean esError() {
        return estado == Estado.ERROR_SERVIDOR || estado == Estado.SIN_CONEXION || estado == Estado.TIEMPO_AGOTADO;
    }

    /** Solo los errores de red ofrecen el botón Reintentar. */
    public boolean permiteReintentar() {
        return estado == Estado.SIN_CONEXION || estado == Estado.TIEMPO_AGOTADO;
    }
}
