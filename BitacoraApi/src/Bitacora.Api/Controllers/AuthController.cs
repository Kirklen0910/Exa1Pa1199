using Bitacora.Api.Seguridad;
using Bitacora.Application.DTOs;
using Bitacora.Application.Services;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace Bitacora.Api.Controllers;

[ApiController]
[Route("api/auth")]
[AllowAnonymous]
[Produces("application/json")]
public class AuthController : ControllerBase
{
    private readonly IAuthService _auth;

    public AuthController(IAuthService auth)
    {
        _auth = auth;
    }

    [HttpPost("registro")]
    [Consumes("application/json")]
    [ProducesResponseType(typeof(UsuarioDto), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status409Conflict)]
    public async Task<ActionResult<UsuarioDto>> Registrar([FromBody] RegistroUsuarioDto input, CancellationToken ct)
    {
        var usuario = await _auth.RegistrarAsync(input, ct);
        if (usuario is null)
            return Problem(statusCode: StatusCodes.Status409Conflict,
                title: "El correo ya está registrado.",
                detail: "Use otro correo o inicie sesión.");

        return StatusCode(StatusCodes.Status201Created, usuario);
    }

    [HttpPost("login")]
    [Consumes("application/json")]
    [ProducesResponseType(typeof(TokenDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(ValidationProblemDetails), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(ProblemDetails), StatusCodes.Status401Unauthorized)]
    public async Task<ActionResult<TokenDto>> Login([FromBody] LoginDto input, CancellationToken ct)
    {
        var token = await _auth.LoginAsync(input, ct);
        if (token is null)
            return RespuestasSeguridad.Problema(this, StatusCodes.Status401Unauthorized,
                RespuestasSeguridad.Tipo401,
                RespuestasSeguridad.TituloCredencialesInvalidas,
                RespuestasSeguridad.DetalleCredencialesInvalidas);

        return Ok(token);
    }
}
