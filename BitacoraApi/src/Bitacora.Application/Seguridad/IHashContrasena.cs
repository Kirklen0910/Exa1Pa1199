using Bitacora.Domain.Entities;

namespace Bitacora.Application.Seguridad;

public interface IHashContrasena
{
    string Generar(Usuario usuario, string password);
    bool Verificar(Usuario usuario, string passwordHash, string password);
}
