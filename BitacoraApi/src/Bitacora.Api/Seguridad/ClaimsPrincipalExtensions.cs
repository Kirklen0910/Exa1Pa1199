using System.Security.Claims;
using Bitacora.Application.Seguridad;
using Bitacora.Infrastructure.Seguridad;

namespace Bitacora.Api.Seguridad;

public static class ClaimsPrincipalExtensions
{
    public static UsuarioActual ObtenerUsuarioActual(this ClaimsPrincipal principal)
    {
        var id = principal.FindFirstValue(JwtOpciones.ClaimId);
        var rol = principal.FindFirstValue(JwtOpciones.ClaimRol);
        if (!int.TryParse(id, out var usuarioId) || string.IsNullOrEmpty(rol))
            throw new InvalidOperationException("El token no contiene los claims de id y rol.");
        return new UsuarioActual(usuarioId, rol);
    }
}
