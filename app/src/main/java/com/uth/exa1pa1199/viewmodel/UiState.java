package com.uth.exa1pa1199.viewmodel;

import com.uth.exa1pa1199.data.repository.ApiError;

import java.util.Collections;
import java.util.Map;

public final class UiState<T> {
    public enum Estado {
        CARGANDO,

        EXITO,

        VACIO,

        ERROR_SERVIDOR,

        SIN_CONEXION,

        TIEMPO_AGOTADO,

        NO_AUTENTICADO,

        SIN_PERMISO
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
            case NO_AUTENTICADO: e = Estado.NO_AUTENTICADO; break;
            case PROHIBIDO: e = Estado.SIN_PERMISO; break;
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
        return estado == Estado.ERROR_SERVIDOR || estado == Estado.SIN_CONEXION || estado == Estado.TIEMPO_AGOTADO
                || estado == Estado.NO_AUTENTICADO || estado == Estado.SIN_PERMISO;
    }

    public boolean esSesionExpirada() {
        return estado == Estado.NO_AUTENTICADO;
    }

    public boolean permiteReintentar() {
        return estado == Estado.SIN_CONEXION || estado == Estado.TIEMPO_AGOTADO;
    }
}
