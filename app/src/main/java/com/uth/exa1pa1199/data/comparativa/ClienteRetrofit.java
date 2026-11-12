package com.uth.exa1pa1199.data.comparativa;

import android.os.SystemClock;

import com.uth.exa1pa1199.data.remote.dto.RegistroResumenDto;
import com.uth.exa1pa1199.data.repository.ApiError;
import com.uth.exa1pa1199.data.repository.Cancelable;
import com.uth.exa1pa1199.data.repository.RegistroRepository;
import com.uth.exa1pa1199.data.repository.RepositorioCallback;

import java.util.List;

public class ClienteRetrofit {
    private final RegistroRepository repositorio = new RegistroRepository();

    public Cancelable consultar(RutaPrueba ruta, ConsultaCallback callback) {
        long inicio = SystemClock.elapsedRealtime();

        RepositorioCallback<List<RegistroResumenDto>> respuesta = new RepositorioCallback<List<RegistroResumenDto>>() {
            @Override
            public void onExito(List<RegistroResumenDto> datos) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                callback.onResultado(ResultadoConsulta.exito(Libreria.RETROFIT, datos == null ? 0 : datos.size(), ms));
            }

            @Override
            public void onError(ApiError error) {
                long ms = SystemClock.elapsedRealtime() - inicio;
                String tecnico = "ApiError." + error.getTipo()
                        + (error.getCodigoHttp() > 0 ? " (HTTP " + error.getCodigoHttp() + ")" : "");
                callback.onResultado(ResultadoConsulta.error(Libreria.RETROFIT, ms,
                        error.getCodigoHttp(), tecnico, error.getMensaje()));
            }
        };

        return ruta == RutaPrueba.REGISTROS
                ? repositorio.obtenerRegistros(respuesta)
                : repositorio.obtenerRegistrosDesde(ruta.getRuta(), respuesta);
    }
}
