using Bitacora.Application.DTOs;
using Bitacora.Application.Seguridad;

namespace Bitacora.Application.Services;

public interface IRegistroService
{
    Task<IReadOnlyList<RegistroResumenDto>> ListarAsync(CancellationToken ct = default);
    Task<RegistroDetalleDto?> ObtenerAsync(int id, CancellationToken ct = default);
    Task<RegistroDetalleDto> CrearAsync(RegistroInputDto input, UsuarioActual usuario, CancellationToken ct = default);

    Task<ResultadoOperacion> ActualizarAsync(int id, RegistroInputDto input, UsuarioActual usuario,
        CancellationToken ct = default);

    Task<bool> EliminarAsync(int id, CancellationToken ct = default);
}
