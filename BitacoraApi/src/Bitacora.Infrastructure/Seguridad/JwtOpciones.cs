using System.Text;
using Microsoft.IdentityModel.Tokens;

namespace Bitacora.Infrastructure.Seguridad;

public class JwtOpciones
{
    public const string Seccion = "Jwt";
    public const int LongitudMinimaClaveBytes = 32;

    public const string ClaimId = "sub";
    public const string ClaimCorreo = "email";
    public const string ClaimRol = "role";

    public string Issuer { get; set; } = string.Empty;
    public string Audience { get; set; } = string.Empty;
    public string Key { get; set; } = string.Empty;
    public int ExpiraMinutos { get; set; } = 10;

    public SymmetricSecurityKey CrearClave() => new(Encoding.UTF8.GetBytes(Key));

    public void Validar()
    {
        if (string.IsNullOrWhiteSpace(Key) || Encoding.UTF8.GetByteCount(Key) < LongitudMinimaClaveBytes)
            throw new InvalidOperationException(
                $"Falta la clave de firma JWT (Jwt:Key) o tiene menos de {LongitudMinimaClaveBytes} bytes. " +
                "Configúrala con: dotnet user-secrets set \"Jwt:Key\" \"<clave>\" --project src/Bitacora.Api " +
                "o con la variable de entorno Jwt__Key.");
        if (string.IsNullOrWhiteSpace(Issuer) || string.IsNullOrWhiteSpace(Audience))
            throw new InvalidOperationException("Faltan Jwt:Issuer o Jwt:Audience en appsettings.json.");
        if (ExpiraMinutos <= 0)
            throw new InvalidOperationException("Jwt:ExpiraMinutos debe ser mayor que cero.");
    }
}
