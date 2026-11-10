using Bitacora.Application.Seguridad;
using Bitacora.Domain.Entities;
using Microsoft.AspNetCore.Identity;

namespace Bitacora.Infrastructure.Seguridad;

public class HashContrasena : IHashContrasena
{
    private readonly PasswordHasher<Usuario> _hasher = new();

    public string Generar(Usuario usuario, string password) => _hasher.HashPassword(usuario, password);

    public bool Verificar(Usuario usuario, string passwordHash, string password)
    {
        try
        {
            return _hasher.VerifyHashedPassword(usuario, passwordHash, password)
                is not PasswordVerificationResult.Failed;
        }
        catch (FormatException)
        {
            return false;
        }
    }
}
