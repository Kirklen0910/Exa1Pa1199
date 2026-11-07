using Bitacora.Application.DTOs;

namespace Bitacora.Application.Services;

public interface IRegistroService
{
    Task<IReadOnlyList<RegistroResumenDto>> ListarAsync(CancellationToken ct = default);
    Task<RegistroDetalleDto?> ObtenerAsync(int id, CancellationToken ct = default);
    Task<RegistroDetalleDto> CrearAsync(RegistroInputDto input, CancellationToken ct = default);

    Task<bool> ActualizarAsync(int id, RegistroInputDto input, CancellationToken ct = default);

    Task<bool> EliminarAsync(int id, CancellationToken ct = default);
}
