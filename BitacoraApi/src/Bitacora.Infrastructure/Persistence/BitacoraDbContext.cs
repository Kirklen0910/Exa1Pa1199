using Bitacora.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Bitacora.Infrastructure.Persistence;

public class BitacoraDbContext : DbContext
{
    public BitacoraDbContext(DbContextOptions<BitacoraDbContext> options) : base(options)
    {
    }

    public DbSet<Registro> Registros => Set<Registro>();
    public DbSet<Usuario> Usuarios => Set<Usuario>();

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<Usuario>(e =>
        {
            e.ToTable("Usuarios");
            e.HasKey(u => u.Id);
            e.Property(u => u.Correo).IsRequired().HasMaxLength(Usuario.CorreoLongitudMaxima);
            e.HasIndex(u => u.Correo).IsUnique();
            e.Property(u => u.PasswordHash).IsRequired();
            e.Property(u => u.Rol).IsRequired().HasMaxLength(Usuario.RolLongitudMaxima);
        });

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

            e.HasOne(r => r.Usuario)
                .WithMany(u => u.Registros)
                .HasForeignKey(r => r.UsuarioId)
                .IsRequired()
                .OnDelete(DeleteBehavior.Restrict);
        });
    }
}
