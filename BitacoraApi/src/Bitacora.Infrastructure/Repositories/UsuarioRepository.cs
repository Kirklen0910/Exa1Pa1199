using Bitacora.Application.Interfaces;
using Bitacora.Domain.Entities;
using Bitacora.Infrastructure.Persistence;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Repositories;

public class UsuarioRepository : IUsuarioRepository
{
    private readonly BitacoraDbContext _db;

    public UsuarioRepository(BitacoraDbContext db)
    {
        _db = db;
    }

    public Task<Usuario?> ObtenerPorCorreoAsync(string correo, CancellationToken ct = default) =>
        _db.Usuarios.FirstOrDefaultAsync(u => u.Correo == correo, ct);

    public Task<bool> ExisteCorreoAsync(string correo, CancellationToken ct = default) =>
        _db.Usuarios.AnyAsync(u => u.Correo == correo, ct);

    public async Task AgregarAsync(Usuario usuario, CancellationToken ct = default)
    {
        _db.Usuarios.Add(usuario);
        await _db.SaveChangesAsync(ct);
    }

    public async Task ActualizarAsync(Usuario usuario, CancellationToken ct = default)
    {
        _db.Usuarios.Update(usuario);
        await _db.SaveChangesAsync(ct);
    }
}
