using Bitacora.Application.DTOs;

namespace Bitacora.Application.Services;

public interface IAuthService
{
    Task<UsuarioDto?> RegistrarAsync(RegistroUsuarioDto input, CancellationToken ct = default);
    Task<TokenDto?> LoginAsync(LoginDto input, CancellationToken ct = default);
    Task AsegurarAdminAsync(string correo, string password, CancellationToken ct = default);
}
