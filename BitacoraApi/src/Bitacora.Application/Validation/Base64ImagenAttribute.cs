using System.ComponentModel.DataAnnotations;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Validation;

[AttributeUsage(AttributeTargets.Property | AttributeTargets.Field | AttributeTargets.Parameter)]
public sealed class Base64ImagenAttribute : ValidationAttribute
{
    public int MaxBytes { get; }

    public Base64ImagenAttribute(int maxBytes = Registro.FotoTamanoMaximoBytes)
    {
        MaxBytes = maxBytes;
    }

    protected override ValidationResult? IsValid(object? value, ValidationContext validationContext)
    {
        if (value is null)
            return ValidationResult.Success; // la foto es opcional

        var miembro = validationContext.MemberName is null ? null : new[] { validationContext.MemberName };

        if (value is not string texto || string.IsNullOrWhiteSpace(texto))
            return new ValidationResult("Si se envía la foto no puede estar vacía (omítela o envía null).", miembro);

        var bytes = Base64Util.Decodificar(texto);

        if (bytes is null)
            return new ValidationResult("La foto no es una cadena Base64 válida.", miembro);

        if (bytes.Length == 0)
            return new ValidationResult("La foto está vacía.", miembro);

        if (bytes.Length > MaxBytes)
            return new ValidationResult(
                $"La foto excede el tamaño máximo de {MaxBytes / (1024 * 1024)} MB ({bytes.Length} bytes recibidos).",
                miembro);

        return ValidationResult.Success;
    }
}

public static class Base64Util
{
    public static byte[]? Decodificar(string texto)
    {
        var limpio = texto.Trim();
        var coma = limpio.IndexOf(',');
        if (limpio.StartsWith("data:", StringComparison.OrdinalIgnoreCase) && coma > 0)
            limpio = limpio[(coma + 1)..];

        var buffer = new byte[(limpio.Length * 3 + 3) / 4];
        return Convert.TryFromBase64String(limpio, buffer, out var escritos)
            ? buffer[..escritos]
            : null;
    }

    public static string? Normalizar(string texto)
    {
        var bytes = Decodificar(texto);
        return bytes is null ? null : Convert.ToBase64String(bytes);
    }
}
