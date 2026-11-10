using Bitacora.Application.DTOs;
using Bitacora.Application.Services;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

[ApiController]
[Route("api/registros")]
[Produces("application/json")]
public class RegistrosController : ControllerBase
{
    private readonly IRegistroService _servicio;

    public RegistrosController(IRegistroService servicio)
    {
        _servicio = servicio;
    }

    [HttpGet]
    [ProducesResponseType(typeof(IReadOnlyList<RegistroResumenDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status406NotAcceptable)]
    public async Task<ActionResult<IReadOnlyList<RegistroResumenDto>>> Listar(CancellationToken ct)
    {
        return Ok(await _servicio.ListarAsync(ct));
    }

    [HttpGet("{id:int}")]
    [ProducesResponseType(typeof(RegistroDetalleDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<ActionResult<RegistroDetalleDto>> ObtenerPorId(int id, CancellationToken ct)
    {
        var registro = await _servicio.ObtenerAsync(id, ct);
        return registro is null ? NotFound() : Ok(registro);
    }

    [HttpPost]
    [Consumes("application/json")]
    [ProducesResponseType(typeof(RegistroDetalleDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    public async Task<ActionResult<RegistroDetalleDto>> Crear([FromBody] RegistroInputDto input, CancellationToken ct)
    {
        var creado = await _servicio.CrearAsync(input, ct);
        return CreatedAtAction(nameof(ObtenerPorId), new { id = creado.Id }, creado);
    }

    [HttpPut("{id:int}")]
    [Consumes("application/json")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Actualizar(int id, [FromBody] RegistroInputDto input, CancellationToken ct)
    {
        return await _servicio.ActualizarAsync(id, input, ct) ? NoContent() : NotFound();
    }

    [HttpDelete("{id:int}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Eliminar(int id, CancellationToken ct)
    {
        return await _servicio.EliminarAsync(id, ct) ? NoContent() : NotFound();
    }
}
