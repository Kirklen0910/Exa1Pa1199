using Bitacora.Domain.Entities;

namespace Bitacora.Application.Interfaces;

public interface IUsuarioRepository
{
    Task<Usuario?> ObtenerPorCorreoAsync(string correo, CancellationToken ct = default);
    Task<bool> ExisteCorreoAsync(string correo, CancellationToken ct = default);
    Task AgregarAsync(Usuario usuario, CancellationToken ct = default);
    Task ActualizarAsync(Usuario usuario, CancellationToken ct = default);
}
