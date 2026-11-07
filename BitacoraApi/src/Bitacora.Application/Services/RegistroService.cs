using Bitacora.Application.DTOs;
using Bitacora.Application.Interfaces;
using Bitacora.Application.Validation;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Services;

public class RegistroService : IRegistroService
{
    private readonly IRegistroRepository _repositorio;

    public RegistroService(IRegistroRepository repositorio)
    {
        _repositorio = repositorio;
    }

    public Task<IReadOnlyList<RegistroResumenDto>> ListarAsync(CancellationToken ct = default) =>
        _repositorio.ListarResumenAsync(ct);

    public async Task<RegistroDetalleDto?> ObtenerAsync(int id, CancellationToken ct = default)
    {
        var registro = await _repositorio.ObtenerPorIdAsync(id, ct);
        return registro is null ? null : ADetalle(registro);
    }

    public async Task<RegistroDetalleDto> CrearAsync(RegistroInputDto input, CancellationToken ct = default)
    {
        var registro = new Registro { FechaCreacion = DateTime.UtcNow };
        Aplicar(input, registro);
        await _repositorio.AgregarAsync(registro, ct);
        return ADetalle(registro);
    }

    public async Task<bool> ActualizarAsync(int id, RegistroInputDto input, CancellationToken ct = default)
    {
        var registro = await _repositorio.ObtenerPorIdAsync(id, ct);
        if (registro is null) return false;

        Aplicar(input, registro);
        await _repositorio.ActualizarAsync(registro, ct);
        return true;
    }

    public async Task<bool> EliminarAsync(int id, CancellationToken ct = default)
    {
        var registro = await _repositorio.ObtenerPorIdAsync(id, ct);
        if (registro is null) return false;

        await _repositorio.EliminarAsync(registro, ct);
        return true;
    }

    private static void Aplicar(RegistroInputDto input, Registro registro)
    {
        registro.Titulo = input.Titulo;
        registro.Descripcion = input.Descripcion;

        registro.FotoBase64 = input.FotoBase64 is null
            ? null
            : Base64Util.Normalizar(input.FotoBase64)
              ?? throw new ArgumentException("FotoBase64 no es Base64 válido.", nameof(input));
    }

    private static RegistroDetalleDto ADetalle(Registro r) =>
        new(r.Id, r.Titulo, r.Descripcion, r.FotoBase64, r.FechaCreacion);
}
