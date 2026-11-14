package com.uth.exa1pa1199.data.comparativa_fernando;

final class MensajesComparativa {
    static final String SIN_CONEXION =
            "No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida.";
    static final String TIEMPO_AGOTADO =
            "El servidor tardó demasiado en responder. Inténtalo de nuevo.";
    static final String FORMATO =
            "El servidor devolvió una respuesta con un formato inesperado.";

    private MensajesComparativa() {
    }

    static String porCodigo(int codigo) {
        if (codigo == 401) {
            return "Tu sesión expiró o no es válida. Inicia sesión de nuevo.";
        }
        if (codigo == 403) {
            return "No tienes permiso para realizar esta acción.";
        }
        if (codigo == 404) {
            return "El recurso solicitado no existe.";
        }
        if (codigo >= 500) {
            return "El servidor tuvo un problema (código " + codigo + "). Inténtalo más tarde.";
        }
        return "El servidor rechazó la solicitud (código " + codigo + ").";
    }
}
