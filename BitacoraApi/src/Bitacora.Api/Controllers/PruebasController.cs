using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

[ApiController]
[Route("api/pruebas")]
[Authorize]
[Produces("application/json")]
public class PruebasController : ControllerBase
{
    private static readonly TimeSpan Demora = TimeSpan.FromSeconds(30);

    private readonly IWebHostEnvironment _entorno;

    public PruebasController(IWebHostEnvironment entorno)
    {
        _entorno = entorno;
    }

    [HttpGet("demora")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> Demorar(CancellationToken ct)
    {
        if (!_entorno.IsDevelopment()) return NotFound();
        await Task.Delay(Demora, ct);
        return Ok(Array.Empty<object>());
    }

    [HttpGet("error")]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status500InternalServerError)]
    public IActionResult Fallar()
    {
        if (!_entorno.IsDevelopment()) return NotFound();
        throw new InvalidOperationException("Error provocado para la prueba de la tarea 2.5.");
    }
}
