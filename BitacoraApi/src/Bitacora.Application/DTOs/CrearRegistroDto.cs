
using System.ComponentModel.DataAnnotations;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.DTOs;

public class CrearRegistroDto
{
    [Required(ErrorMessage = "El título es obligatorio.")]
    [StringLength(
        Registro.TituloLongitudMaxima,
        MinimumLength = Registro.TituloLongitudMinima,
        ErrorMessage = "El título debe tener entre 3 y 100 caracteres.")]
    public string Titulo { get; set; } = string.Empty;

    [Required(ErrorMessage = "La descripción es obligatoria.")]
    [StringLength(
        Registro.DescripcionLongitudMaxima,
        ErrorMessage = "La descripción no puede superar los 500 caracteres.")]
    public string Descripcion { get; set; } = string.Empty;

    public string? FotoBase64 { get; set; }
}
