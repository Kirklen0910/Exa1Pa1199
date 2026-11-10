namespace Bitacora.Domain.Entities;

public class Usuario
{
    public const int CorreoLongitudMaxima = 256;
    public const int PasswordLongitudMinima = 8;
    public const int PasswordLongitudMaxima = 100;
    public const int RolLongitudMaxima = 20;

    public int Id { get; set; }
    public string Correo { get; set; } = string.Empty;
    public string PasswordHash { get; set; } = string.Empty;
    public string Rol { get; set; } = Roles.Usuario;

    public List<Registro> Registros { get; set; } = new();
}
