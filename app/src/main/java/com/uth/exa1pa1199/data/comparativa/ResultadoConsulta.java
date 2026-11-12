package com.uth.exa1pa1199.data.comparativa;

public final class ResultadoConsulta {
    private final Libreria libreria;
    private final boolean exito;
    private final int cantidad;
    private final long milisegundos;
    private final int codigoHttp;
    private final String errorTecnico;
    private final String mensajeUsuario;

    private ResultadoConsulta(Libreria libreria, boolean exito, int cantidad, long milisegundos,
                              int codigoHttp, String errorTecnico, String mensajeUsuario) {
        this.libreria = libreria;
        this.exito = exito;
        this.cantidad = cantidad;
        this.milisegundos = milisegundos;
        this.codigoHttp = codigoHttp;
        this.errorTecnico = errorTecnico;
        this.mensajeUsuario = mensajeUsuario;
    }

    public static ResultadoConsulta exito(Libreria libreria, int cantidad, long milisegundos) {
        return new ResultadoConsulta(libreria, true, cantidad, milisegundos, 200, null, null);
    }

    public static ResultadoConsulta error(Libreria libreria, long milisegundos, int codigoHttp,
                                          String errorTecnico, String mensajeUsuario) {
        return new ResultadoConsulta(libreria, false, 0, milisegundos, codigoHttp, errorTecnico, mensajeUsuario);
    }

    public Libreria getLibreria() { return libreria; }
    public boolean isExito() { return exito; }
    public int getCantidad() { return cantidad; }
    public long getMilisegundos() { return milisegundos; }
    public int getCodigoHttp() { return codigoHttp; }
    public String getErrorTecnico() { return errorTecnico; }
    public String getMensajeUsuario() { return mensajeUsuario; }
}
