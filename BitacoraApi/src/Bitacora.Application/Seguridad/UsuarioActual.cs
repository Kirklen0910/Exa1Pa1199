using Bitacora.Domain.Entities;

namespace Bitacora.Application.Seguridad;

public record UsuarioActual(int Id, string Rol)
{
    public bool EsAdmin => Rol == Roles.Admin;
}
