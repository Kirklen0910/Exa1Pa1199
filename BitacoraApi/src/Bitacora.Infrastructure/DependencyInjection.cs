using Bitacora.Application.Interfaces;
using Bitacora.Application.Seguridad;
using Bitacora.Infrastructure.Persistence;
using Bitacora.Infrastructure.Repositories;
using Bitacora.Infrastructure.Seguridad;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace Bitacora.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfraestructura(this IServiceCollection services, string connectionString)
    {
        services.AddDbContext<BitacoraDbContext>(o => o.UseSqlite(connectionString));
        services.AddScoped<IRegistroRepository, RegistroRepository>();
        services.AddScoped<IUsuarioRepository, UsuarioRepository>();
        services.AddSingleton<IHashContrasena, HashContrasena>();
        services.AddSingleton<IGeneradorToken, GeneradorTokenJwt>();
        return services;
    }

    public static async Task AplicarMigracionesAsync(this IServiceProvider services, CancellationToken ct = default)
    {
        using var scope = services.CreateScope();
        var db = scope.ServiceProvider.GetRequiredService<BitacoraDbContext>();
        await db.Database.MigrateAsync(ct);
    }
}
