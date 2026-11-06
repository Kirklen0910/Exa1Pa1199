package com.uth.exa1pa1199.data.repository;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.uth.exa1pa1199.data.remote.ApiClient;
import com.uth.exa1pa1199.data.remote.RegistroApi;
import com.uth.exa1pa1199.data.remote.dto.ProblemDetailsDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroDetalleDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroRequestDto;
import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistroRepository {

    private final RegistroApi api;
    private final Gson gson = new Gson();

    public RegistroRepository() {
        this(ApiClient.getApi());
    }

    public RegistroRepository(RegistroApi api) {
        this.api = api;
    }

    public Cancelable obtenerRegistros(RepositorioCallback<List<RegistroResumenDto>> callback) {
        return ejecutar(api.listar(), callback);
    }

    public Cancelable obtenerRegistro(String id, RepositorioCallback<RegistroDetalleDto> callback) {
        return ejecutar(api.obtener(id), callback);
    }

    public Cancelable crearRegistro(RegistroRequestDto registro, RepositorioCallback<RegistroDetalleDto> callback) {
        return ejecutar(api.crear(registro), callback);
    }

    public Cancelable actualizarRegistro(String id, RegistroRequestDto registro, RepositorioCallback<Void> callback) {
        return ejecutar(api.actualizar(id, registro), callback);
    }

    public Cancelable eliminarRegistro(String id, RepositorioCallback<Void> callback) {
        return ejecutar(api.eliminar(id), callback);
    }

    private <T> Cancelable ejecutar(Call<T> call, RepositorioCallback<T> callback) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> c, @NonNull Response<T> response) {
                if (response.isSuccessful()) {
                    callback.onExito(response.body());
                } else {
                    callback.onError(errorDesdeRespuesta(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<T> c, @NonNull Throwable t) {
                if (c.isCanceled()) return; // el ViewModel ya no espera la respuesta
                callback.onError(errorDesdeExcepcion(t));
            }
        });
        return call::cancel;
    }

    private ApiError errorDesdeRespuesta(Response<?> response) {
        int codigo = response.code();
        ProblemDetailsDto problema = leerProblema(response.errorBody());

        Map<String, String> erroresCampo = new HashMap<>();
        if (problema != null && problema.getErrors() != null) {
            for (Map.Entry<String, List<String>> e : problema.getErrors().entrySet()) {
                if (e.getValue() == null || e.getValue().isEmpty()) continue;
                erroresCampo.put(normalizarCampo(e.getKey()), String.join("\n", e.getValue()));
            }
        }
        return new ApiError(ApiError.Tipo.SERVIDOR, codigo, mensajePorCodigo(codigo, problema), erroresCampo);
    }

    private ApiError errorDesdeExcepcion(Throwable t) {
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
            return null; // cuerpo vacío o que no es ProblemDetails
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
