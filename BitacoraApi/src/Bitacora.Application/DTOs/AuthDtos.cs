using System.ComponentModel.DataAnnotations;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.DTOs;

public class RegistroUsuarioDto
{
    [Required(ErrorMessage = "El correo es obligatorio.")]
    [EmailAddress(ErrorMessage = "El correo no tiene un formato válido.")]
    [StringLength(Usuario.CorreoLongitudMaxima, ErrorMessage = "El correo no puede superar {1} caracteres.")]
    public string Correo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La contraseña es obligatoria.")]
    [StringLength(Usuario.PasswordLongitudMaxima, MinimumLength = Usuario.PasswordLongitudMinima,
        ErrorMessage = "La contraseña debe tener entre {2} y {1} caracteres.")]
    public string Password { get; set; } = string.Empty;
}

public class LoginDto
{
    [Required(ErrorMessage = "El correo es obligatorio.")]
    public string Correo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La contraseña es obligatoria.")]
    public string Password { get; set; } = string.Empty;
}

public record UsuarioDto(int Id, string Correo, string Rol);

public record TokenDto(string Token, DateTime Expiracion, string Rol, string Correo);
