package com.uth.exa1pa1199.data.comparativa_fernando;

/**
 * Resultado homogéneo de una consulta, sin importar la librería que la ejecutó.
 */
public final class ResultadoComparativa {
    private final LibreriaComparativa libreria;
    private final boolean exito;
    private final int cantidad;
    private final long milisegundos;
    private final int codigoHttp;
    private final String detalleTecnico;
    private final String mensajeUsuario;

    private ResultadoComparativa(LibreriaComparativa libreria, boolean exito, int cantidad,
                                 long milisegundos, int codigoHttp, String detalleTecnico,
                                 String mensajeUsuario) {
        this.libreria = libreria;
        this.exito = exito;
        this.cantidad = cantidad;
        this.milisegundos = milisegundos;
        this.codigoHttp = codigoHttp;
        this.detalleTecnico = detalleTecnico;
        this.mensajeUsuario = mensajeUsuario;
    }

    public static ResultadoComparativa exito(LibreriaComparativa libreria, int cantidad,
                                             long milisegundos) {
        return new ResultadoComparativa(libreria, true, cantidad, milisegundos, 200, null, null);
    }

    public static ResultadoComparativa error(LibreriaComparativa libreria, long milisegundos,
                                             int codigoHttp, String detalleTecnico,
                                             String mensajeUsuario) {
        return new ResultadoComparativa(libreria, false, 0, milisegundos, codigoHttp,
                detalleTecnico, mensajeUsuario);
    }

    public LibreriaComparativa getLibreria() {
        return libreria;
    }

    public boolean isExito() {
        return exito;
    }

    public int getCantidad() {
        return cantidad;
    }

    public long getMilisegundos() {
        return milisegundos;
    }

    public int getCodigoHttp() {
        return codigoHttp;
    }

    public String getDetalleTecnico() {
        return detalleTecnico;
    }

    public String getMensajeUsuario() {
        return mensajeUsuario;
    }
}
