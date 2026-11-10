package com.uth.exa1pa1199.data.repository;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.uth.exa1pa1199.data.remote.dto.ProblemDetailsDto;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Response;

final class ApiErrorMapper {
    static final String MENSAJE_SESION_EXPIRADA =
            "Tu sesión expiró o no es válida. Inicia sesión de nuevo.";
    static final String MENSAJE_SIN_PERMISO = "No tienes permiso para realizar esta acción.";
    static final String MENSAJE_CREDENCIALES = "Correo o contraseña incorrectos.";

    private final Gson gson = new Gson();

    ApiError desdeRespuesta(Response<?> response, boolean esAutenticacion) {
        int codigo = response.code();
        ProblemDetailsDto problema = leerProblema(response.errorBody());

        Map<String, String> erroresCampo = new HashMap<>();
        if (problema != null && problema.getErrors() != null) {
            for (Map.Entry<String, List<String>> e : problema.getErrors().entrySet()) {
                if (e.getValue() == null || e.getValue().isEmpty()) continue;
                erroresCampo.put(normalizarCampo(e.getKey()), String.join("\n", e.getValue()));
            }
        }

        if (codigo == 401) {
            return esAutenticacion
                    ? new ApiError(ApiError.Tipo.SERVIDOR, codigo, MENSAJE_CREDENCIALES, null)
                    : new ApiError(ApiError.Tipo.NO_AUTENTICADO, codigo, MENSAJE_SESION_EXPIRADA, null);
        }
        if (codigo == 403) {
            String detalle = problema != null && problema.getDetail() != null
                    ? problema.getDetail() : MENSAJE_SIN_PERMISO;
            return new ApiError(ApiError.Tipo.PROHIBIDO, codigo, detalle, null);
        }
        if (esAutenticacion && codigo == 409) {
            String titulo = problema != null && problema.getTitle() != null
                    ? problema.getTitle() : "El correo ya está registrado.";
            Map<String, String> campo = new HashMap<>();
            campo.put("correo", titulo);
            return new ApiError(ApiError.Tipo.SERVIDOR, codigo, titulo, campo);
        }
        return new ApiError(ApiError.Tipo.SERVIDOR, codigo, mensajePorCodigo(codigo, problema), erroresCampo);
    }

    ApiError desdeExcepcion(Throwable t) {
        if (t instanceof SocketTimeoutException) {
            return new ApiError(ApiError.Tipo.TIEMPO_AGOTADO, 0,
                    "El servidor tardó demasiado en responder. Verifica que la API esté en ejecución e inténtalo de nuevo.",
                    null);
        }
        if (t instanceof JsonParseException) {
            return new ApiError(ApiError.Tipo.SERVIDOR, 0,
                    "El servidor devolvió una respuesta con un formato inesperado.", null);
        }
        if (t instanceof IOException) {
            return new ApiError(ApiError.Tipo.SIN_CONEXION, 0,
                    "No se pudo conectar con el servidor. Revisa tu conexión a internet o que la API esté encendida.",
                    null);
        }
        return new ApiError(ApiError.Tipo.SERVIDOR, 0,
                "Ocurrió un error inesperado: " + t.getClass().getSimpleName(), null);
    }

    private ProblemDetailsDto leerProblema(ResponseBody cuerpo) {
        if (cuerpo == null) return null;
        try (ResponseBody b = cuerpo) {
            String json = b.string();
            if (json.trim().isEmpty()) return null;
            return gson.fromJson(json, ProblemDetailsDto.class);
        } catch (IOException | RuntimeException e) {
            return null;
        }
    }

    private static String normalizarCampo(String clave) {
        if (clave == null) return "";
        String c = clave.startsWith("$.") ? clave.substring(2) : clave;
        return c.toLowerCase(Locale.ROOT);
    }

    private static String mensajePorCodigo(int codigo, ProblemDetailsDto problema) {
        String detalle = problema != null && problema.getDetail() != null ? " " + problema.getDetail() : "";
        switch (codigo) {
            case 400:
                return "Los datos enviados no son válidos. Revisa los campos marcados." + detalle;
            case 404:
                return "El registro no existe o ya fue eliminado.";
            case 409:
                return "El registro fue modificado por otra persona. Actualiza e inténtalo de nuevo.";
            case 413:
                return "La foto es demasiado grande para el servidor.";
            case 415:
                return "El servidor no aceptó el formato de los datos enviados.";
            default:
                if (codigo >= 500) {
                    return "El servidor tuvo un problema (código " + codigo + "). Inténtalo más tarde." + detalle;
                }
                return "El servidor rechazó la solicitud (código " + codigo + ")." + detalle;
        }
    }
}
