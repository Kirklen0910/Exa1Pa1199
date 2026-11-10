using System.Diagnostics;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Middleware;

/// <summary>
/// Captura cualquier excepción no controlada, la registra en el log y responde
/// un ProblemDetails 500 genérico (sin mensaje ni stack trace de la excepción).
/// </summary>
public class ExceptionHandlingMiddleware
{
    private readonly RequestDelegate _next;
    private readonly ILogger<ExceptionHandlingMiddleware> _logger;

    public ExceptionHandlingMiddleware(RequestDelegate next, ILogger<ExceptionHandlingMiddleware> logger)
    {
        _next = next;
        _logger = logger;
    }

    public async Task InvokeAsync(HttpContext context)
    {
        try
        {
            await _next(context);
        }
        catch (OperationCanceledException) when (context.RequestAborted.IsCancellationRequested)
        {
        }
        catch (Exception ex)
        {
            var traceId = Activity.Current?.Id ?? context.TraceIdentifier;
            _logger.LogError(ex, "Error no controlado. TraceId: {TraceId}", traceId);

            if (context.Response.HasStarted)
                throw;

            context.Response.Clear();
            context.Response.StatusCode = StatusCodes.Status500InternalServerError;

            var problema = new ProblemDetails
            {
                Type = "https://tools.ietf.org/html/rfc9110#section-15.6.1",
                Title = "Ocurrió un error interno en el servidor.",
                Status = StatusCodes.Status500InternalServerError,
                Instance = context.Request.Path
            };
            problema.Extensions["traceId"] = traceId;

            await context.Response.WriteAsJsonAsync(problema, (System.Text.Json.JsonSerializerOptions?)null,
                contentType: "application/problem+json");
        }
    }
}
