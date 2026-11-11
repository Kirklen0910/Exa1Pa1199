using Bitacora.Api.Seguridad;
using Bitacora.Application.DTOs;
using Bitacora.Application.Services;
using Bitacora.Domain.Entities;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

[ApiController]
[Route("api/registros")]
[Authorize]
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
        var creado = await _servicio.CrearAsync(input, User.ObtenerUsuarioActual(), ct);
        return CreatedAtAction(nameof(ObtenerPorId), new { id = creado.Id }, creado);
    }

    [HttpPut("{id:int}")]
    [Consumes("application/json")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Actualizar(int id, [FromBody] RegistroInputDto input, CancellationToken ct)
    {
        var resultado = await _servicio.ActualizarAsync(id, input, User.ObtenerUsuarioActual(), ct);
        return resultado switch
        {
            ResultadoOperacion.Exito => NoContent(),
            ResultadoOperacion.Prohibido => RespuestasSeguridad.Problema(this, StatusCodes.Status403Forbidden,
                RespuestasSeguridad.Tipo403,
                RespuestasSeguridad.TituloProhibido,
                "Solo puede modificar los registros que usted creó."),
            _ => NotFound()
        };
    }

    [HttpDelete("{id:int}")]
    [Authorize(Roles = Roles.Admin)]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Eliminar(int id, CancellationToken ct)
    {
        return await _servicio.EliminarAsync(id, ct) ? NoContent() : NotFound();
    }
}
