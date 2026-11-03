namespace Bitacora.Domain.Entities;

public class Registro
{
    public const int TituloLongitudMinima = 3;
    public const int TituloLongitudMaxima = 100;
    public const int DescripcionLongitudMaxima = 500;
    public const int FotoTamanoMaximoBytes = 2 * 1024 * 1024; // 2 MB decodificados

    public int Id { get; set; }
    public string Titulo { get; set; } = string.Empty;
    public string Descripcion { get; set; } = string.Empty;

    public string? FotoBase64 { get; set; }

    public DateTime FechaCreacion { get; set; }
}
