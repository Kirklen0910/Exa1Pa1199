namespace Bitacora.Domain.Entities;

/// <summary>
/// Entrada de la bitácora de campo: título, descripción (dictada por voz en Android),
/// foto opcional en Base64 y fecha de creación asignada por el servidor.
/// </summary>
public class Registro
{
    // Reglas del negocio. Las reutilizan la validación de los DTO y la configuración de EF Core.
    public const int TituloLongitudMinima = 3;
    public const int TituloLongitudMaxima = 100;
    public const int DescripcionLongitudMaxima = 500;
    public const int FotoTamanoMaximoBytes = 2 * 1024 * 1024; // 2 MB decodificados

    public int Id { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string Descripcion { get; set; } = string.Empty;

    /// <summary>Imagen codificada en Base64 (sin prefijo data:). Null si el registro no tiene foto.</summary>
    public string? FotoBase64 { get; set; }

    /// <summary>Fecha UTC asignada por el servidor al crear el registro.</summary>
    public DateTime FechaCreacion { get; set; }
}
