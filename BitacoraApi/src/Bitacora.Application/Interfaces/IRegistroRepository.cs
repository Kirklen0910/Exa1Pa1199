using Bitacora.Application.DTOs;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Interfaces;

public interface IRegistroRepository
{
    Task<IReadOnlyList<RegistroResumenDto>> ListarResumenAsync(CancellationToken ct = default);
    Task<Registro?> ObtenerPorIdAsync(int id, CancellationToken ct = default);
    Task AgregarAsync(Registro registro, CancellationToken ct = default);
    Task ActualizarAsync(Registro registro, CancellationToken ct = default);
    Task EliminarAsync(Registro registro, CancellationToken ct = default);
}
