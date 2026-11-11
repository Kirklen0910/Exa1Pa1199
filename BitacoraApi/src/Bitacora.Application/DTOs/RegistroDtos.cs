using System.ComponentModel.DataAnnotations;
using Bitacora.Application.Validation;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.DTOs;

public class RegistroInputDto
{
    [Required(ErrorMessage = "El título es obligatorio.")]
    [StringLength(Registro.TituloLongitudMaxima, MinimumLength = Registro.TituloLongitudMinima,
        ErrorMessage = "El título debe tener entre {2} y {1} caracteres.")]
    public string Titulo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La descripción es obligatoria.")]
    [StringLength(Registro.DescripcionLongitudMaxima,
        ErrorMessage = "La descripción no puede superar {1} caracteres.")]
    public string Descripcion { get; set; } = string.Empty;

    [Base64Imagen]
    public string? FotoBase64 { get; set; }
}

public record RegistroResumenDto(
    int Id,
    string Titulo,
    string Descripcion,
    DateTime FechaCreacion,
    bool TieneFoto,
    int UsuarioId);

public record RegistroDetalleDto(
    int Id,
    string Titulo,
    string Descripcion,
    string? FotoBase64,
    DateTime FechaCreacion,
    int UsuarioId);
