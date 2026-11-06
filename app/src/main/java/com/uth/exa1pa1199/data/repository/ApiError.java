package com.uth.exa1pa1199.data.repository;

import java.util.Collections;
import java.util.Map;

/** Error ya traducido a algo que la interfaz puede mostrar. */
public class ApiError {

    public enum Tipo {
        /** El servidor respondió con 4xx o 5xx. */
        SERVIDOR,
        /** No se pudo conectar (API apagada, modo avión, sin red). */
        SIN_CONEXION,
        /** Se agotó el tiempo de espera de conexión o lectura. */
        TIEMPO_AGOTADO
    }

    private final Tipo tipo;
    private final int codigoHttp;
    private final String mensaje;
    /** Errores de validación por campo (claves en minúscula: "titulo", "descripcion", "fotobase64"). */
    private final Map<String, String> erroresCampo;

    public ApiError(Tipo tipo, int codigoHttp, String mensaje, Map<String, String> erroresCampo) {
        this.tipo = tipo;
        this.codigoHttp = codigoHttp;
        this.mensaje = mensaje;
        this.erroresCampo = erroresCampo == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(erroresCampo);
    }

    public Tipo getTipo() { return tipo; }
    public int getCodigoHttp() { return codigoHttp; }
    public String getMensaje() { return mensaje; }
    public Map<String, String> getErroresCampo() { return erroresCampo; }
}
