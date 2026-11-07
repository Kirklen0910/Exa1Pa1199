using Bitacora.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Persistence;

public class BitacoraDbContext : DbContext
{
    public BitacoraDbContext(DbContextOptions<BitacoraDbContext> options) : base(options)
    {
    }

    public DbSet<Registro> Registros => Set<Registro>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Registro>(e =>
        {
            e.ToTable("Registros");
            e.HasKey(r => r.Id);
            e.Property(r => r.Titulo).IsRequired().HasMaxLength(Registro.TituloLongitudMaxima);
            e.Property(r => r.Descripcion).IsRequired().HasMaxLength(Registro.DescripcionLongitudMaxima);
            e.Property(r => r.FotoBase64);

            e.Property(r => r.FechaCreacion).IsRequired().HasConversion(
                v => v.ToUniversalTime(),
                v => DateTime.SpecifyKind(v, DateTimeKind.Utc));
            e.HasIndex(r => r.FechaCreacion);
        });
    }
}
