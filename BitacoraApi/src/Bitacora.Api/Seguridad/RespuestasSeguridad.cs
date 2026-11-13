using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;
using Microsoft.IdentityModel.Tokens;

namespace Bitacora.Api.Seguridad;

public static class RespuestasSeguridad
{
    public const string Tipo401 = "https://tools.ietf.org/html/rfc9110#section-15.5.2";
    public const string Tipo403 = "https://tools.ietf.org/html/rfc9110#section-15.5.4";

    public const string TituloNoAutenticado = "No autenticado.";
    public const string TituloProhibido = "Acceso prohibido.";
    public const string TituloCredencialesInvalidas = "Credenciales inválidas.";
    public const string DetalleCredencialesInvalidas = "El correo o la contraseña no son correctos.";

    public static JsonResult Problema(ControllerBase controlador, int status, string tipo, string titulo,
        string detalle)
    {
        var problema = controlador.ProblemDetailsFactory.CreateProblemDetails(
            controlador.HttpContext, status, titulo, tipo, detalle, controlador.HttpContext.Request.Path);
        return new JsonResult(problema) { StatusCode = status, ContentType = "application/problem+json" };
    }

    public static Task EscribirNoAutenticadoAsync(HttpContext context, Exception? fallo)
    {
        string detalle;
        string encabezado;

        switch (fallo)
        {
            case null:
                detalle = "Debe iniciar sesión y enviar el token en el encabezado Authorization: Bearer <token>.";
                encabezado = "Bearer";
                break;
            case SecurityTokenExpiredException:
                detalle = "El token venció. Inicie sesión de nuevo.";
                encabezado = "Bearer error=\"invalid_token\", error_description=\"The token expired\"";
                break;
            default:
                detalle = "El token no es válido.";
                encabezado = "Bearer error=\"invalid_token\"";
                break;
        }

        context.Response.Headers.WWWAuthenticate = encabezado;
        return EscribirAsync(context, StatusCodes.Status401Unauthorized, Tipo401, TituloNoAutenticado, detalle);
    }

    public static Task EscribirProhibidoAsync(HttpContext context) =>
        EscribirAsync(context, StatusCodes.Status403Forbidden, Tipo403, TituloProhibido,
            "No tiene permiso para realizar esta acción.");

    private static Task EscribirAsync(HttpContext context, int status, string tipo, string titulo, string detalle)
    {
        var problema = new ProblemDetails
        {
            Type = tipo,
            Title = titulo,
            Status = status,
            Detail = detalle,
            Instance = context.Request.Path
        };
        problema.Extensions["traceId"] = Activity.Current?.Id ?? context.TraceIdentifier;

        context.Response.StatusCode = status;
        return context.Response.WriteAsJsonAsync(problema, (System.Text.Json.JsonSerializerOptions?)null,
            contentType: "application/problem+json");
    }
}
