using Bitacora.Application.Interfaces;
using Bitacora.Infrastructure.Persistence;
using Bitacora.Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;

namespace Bitacora.Infrastructure;

public static class DependencyInjection
{
    public static IServiceCollection AddInfraestructura(this IServiceCollection services, string connectionString)
    {
        services.AddDbContext<BitacoraDbContext>(o => o.UseSqlite(connectionString));
        services.AddScoped<IRegistroRepository, RegistroRepository>();
        return services;
    }
}
