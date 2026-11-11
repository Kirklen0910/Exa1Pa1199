using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using Bitacora.Application.DTOs;
using Bitacora.Application.Seguridad;
using Bitacora.Domain.Entities;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;

namespace Bitacora.Infrastructure.Seguridad;

public class GeneradorTokenJwt : IGeneradorToken
{
    private readonly JwtOpciones _opciones;

    public GeneradorTokenJwt(IOptions<JwtOpciones> opciones)
    {
        _opciones = opciones.Value;
    }

    public TokenDto Generar(Usuario usuario)
    {
        var ahora = DateTime.UtcNow;
        var expiracion = ahora.AddMinutes(_opciones.ExpiraMinutos);

        var claims = new[]
        {
            new Claim(JwtOpciones.ClaimId, usuario.Id.ToString()),
            new Claim(JwtOpciones.ClaimCorreo, usuario.Correo),
            new Claim(JwtOpciones.ClaimRol, usuario.Rol),
            new Claim(JwtRegisteredClaimNames.Jti, Guid.NewGuid().ToString("N"))
        };

        var credenciales = new SigningCredentials(_opciones.CrearClave(), SecurityAlgorithms.HmacSha256);

        var token = new JwtSecurityToken(
            issuer: _opciones.Issuer,
            audience: _opciones.Audience,
            claims: claims,
            notBefore: ahora,
            expires: expiracion,
            signingCredentials: credenciales);

        var texto = new JwtSecurityTokenHandler().WriteToken(token);
        return new TokenDto(texto, expiracion, usuario.Rol, usuario.Correo);
    }
}
