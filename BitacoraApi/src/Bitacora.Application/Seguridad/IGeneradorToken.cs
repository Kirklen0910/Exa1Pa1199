using Bitacora.Application.DTOs;
using Bitacora.Domain.Entities;

namespace Bitacora.Application.Seguridad;

public interface IGeneradorToken
{
    TokenDto Generar(Usuario usuario);
}
