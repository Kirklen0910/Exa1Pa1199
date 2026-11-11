using Bitacora.Application.DTOs;
using Bitacora.Application.Interfaces;
using Bitacora.Application.Seguridad;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Services;

public class AuthService : IAuthService
{
    private readonly IUsuarioRepository _usuarios;
    private readonly IHashContrasena _hash;
    private readonly IGeneradorToken _tokens;
    private static string? _hashFicticio;

    public AuthService(IUsuarioRepository usuarios, IHashContrasena hash, IGeneradorToken tokens)
    {
        _usuarios = usuarios;
        _hash = hash;
        _tokens = tokens;
    }

    public static string NormalizarCorreo(string correo) => correo.Trim().ToLowerInvariant();

    public async Task<UsuarioDto?> RegistrarAsync(RegistroUsuarioDto input, CancellationToken ct = default)
    {
        var correo = NormalizarCorreo(input.Correo);
        if (await _usuarios.ExisteCorreoAsync(correo, ct))
            return null;

        var usuario = new Usuario { Correo = correo, Rol = Roles.Usuario };
        usuario.PasswordHash = _hash.Generar(usuario, input.Password);
        await _usuarios.AgregarAsync(usuario, ct);
        return new UsuarioDto(usuario.Id, usuario.Correo, usuario.Rol);
    }

    public async Task<TokenDto?> LoginAsync(LoginDto input, CancellationToken ct = default)
    {
        var usuario = await _usuarios.ObtenerPorCorreoAsync(NormalizarCorreo(input.Correo), ct);
        if (usuario is null)
        {
            var ficticio = new Usuario();
            _hashFicticio ??= _hash.Generar(ficticio, Guid.NewGuid().ToString());
            _hash.Verificar(ficticio, _hashFicticio, input.Password);
            return null;
        }

        return _hash.Verificar(usuario, usuario.PasswordHash, input.Password)
            ? _tokens.Generar(usuario)
            : null;
    }

    public async Task AsegurarAdminAsync(string correo, string password, CancellationToken ct = default)
    {
        var normalizado = NormalizarCorreo(correo);
        var existente = await _usuarios.ObtenerPorCorreoAsync(normalizado, ct);
        if (existente is null)
        {
            var admin = new Usuario { Correo = normalizado, Rol = Roles.Admin };
            admin.PasswordHash = _hash.Generar(admin, password);
            await _usuarios.AgregarAsync(admin, ct);
            return;
        }

        var cambio = false;
        if (existente.Rol != Roles.Admin)
        {
            existente.Rol = Roles.Admin;
            cambio = true;
        }
        if (!_hash.Verificar(existente, existente.PasswordHash, password))
        {
            existente.PasswordHash = _hash.Generar(existente, password);
            cambio = true;
        }
        if (cambio)
            await _usuarios.ActualizarAsync(existente, ct);
    }
}
