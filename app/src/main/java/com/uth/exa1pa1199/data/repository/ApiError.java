package com.uth.exa1pa1199.data.repository;

import java.util.Collections;
import java.util.Map;

public class ApiError {
    public enum Tipo {
        SERVIDOR,

        SIN_CONEXION,

        TIEMPO_AGOTADO,

        NO_AUTENTICADO,

        PROHIBIDO
    }

    private final Tipo tipo;
    private final int codigoHttp;
    private final String mensaje;

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
