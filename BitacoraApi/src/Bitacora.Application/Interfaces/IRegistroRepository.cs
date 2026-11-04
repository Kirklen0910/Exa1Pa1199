using Bitacora.Application.DTOs;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Interfaces;

/// <summary>
/// Persistencia que necesitan los casos de uso. La implementa Bitacora.Infrastructure.
/// </summary>
public interface IRegistroRepository
{
    /// <summary>Lista los registros SIN leer la foto (solo indica si tienen una).</summary>
    Task<IReadOnlyList<RegistroResumenDto>> ListarResumenAsync(CancellationToken ct = default);
    Task<Registro?> ObtenerPorIdAsync(int id, CancellationToken ct = default);
    Task AgregarAsync(Registro registro, CancellationToken ct = default);
    Task ActualizarAsync(Registro registro, CancellationToken ct = default);
    Task EliminarAsync(Registro registro, CancellationToken ct = default);
}
