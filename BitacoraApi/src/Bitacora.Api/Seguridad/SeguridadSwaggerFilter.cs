using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.OpenApi.Models;
using Swashbuckle.AspNetCore.SwaggerGen;

namespace Bitacora.Api.Seguridad;

public class SeguridadSwaggerFilter : IOperationFilter
{
    public const string Esquema = "Bearer";

    public void Apply(OpenApiOperation operation, OperationFilterContext context)
    {
        var metadatos = context.ApiDescription.ActionDescriptor.EndpointMetadata;
        if (metadatos.OfType<IAllowAnonymous>().Any())
            return;

        var autorizacion = metadatos.OfType<IAuthorizeData>().ToList();
        if (autorizacion.Count == 0)
            return;

        operation.Security.Add(new OpenApiSecurityRequirement
        {
            [new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference { Type = ReferenceType.SecurityScheme, Id = Esquema }
            }] = Array.Empty<string>()
        });

        var esquemaProblema = context.SchemaGenerator.GenerateSchema(typeof(ProblemDetails), context.SchemaRepository);
        AgregarRespuesta(operation, "401", "No autenticado: falta el token, venció o no es válido.", esquemaProblema);

        if (autorizacion.Any(a => !string.IsNullOrEmpty(a.Roles)) || operation.Responses.ContainsKey("403"))
            AgregarRespuesta(operation, "403", "Autenticado, pero sin permiso para esta acción.", esquemaProblema);
    }

    private static void AgregarRespuesta(OpenApiOperation operation, string codigo, string descripcion,
        OpenApiSchema esquema)
    {
        operation.Responses[codigo] = new OpenApiResponse
        {
            Description = descripcion,
            Content = { ["application/problem+json"] = new OpenApiMediaType { Schema = esquema } }
        };
    }
}
