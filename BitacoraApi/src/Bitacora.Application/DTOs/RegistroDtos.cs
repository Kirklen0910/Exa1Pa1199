using System.ComponentModel.DataAnnotations;
using Bitacora.Application.Validation;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.DTOs;

/// <summary>Cuerpo de POST y PUT. La fecha no se recibe: la asigna el servidor.</summary>
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

    /// <summary>Opcional. Si se envía, debe ser Base64 válido y no superar 2 MB decodificados.</summary>
    [Base64Imagen]
    public string? FotoBase64 { get; set; }
}

/// <summary>Elemento de GET /api/registros (sin la foto, para que la lista sea liviana).</summary>
public record RegistroResumenDto(
    int Id,
    string Titulo,
    string Descripcion,
    DateTime FechaCreacion,
    bool TieneFoto);

/// <summary>Respuesta de GET /api/registros/{id} y POST (incluye la foto).</summary>
public record RegistroDetalleDto(
    int Id,
    string Titulo,
    string Descripcion,
    string? FotoBase64,
    DateTime FechaCreacion);
