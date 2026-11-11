# Arquitectura

La solución `Bitacora.sln` sigue una arquitectura por capas (estilo *Clean Architecture*):
**todas las dependencias apuntan hacia el dominio**, y el dominio no depende de nadie.

## Capas y dependencias entre proyectos

```mermaid
flowchart TB
    subgraph Api["Bitacora.Api (presentación)"]
        C[RegistrosController<br/>Authorize + Roles Admin en DELETE]
        AC[AuthController<br/>registro y login AllowAnonymous]
        M[ExceptionHandlingMiddleware]
        SEG[RespuestasSeguridad<br/>401/403 ProblemDetails]
        P["Program.cs<br/>DI, JwtBearer, Swagger Bearer,<br/>HTTPS 7080 + HTTP 5080"]
    end

    subgraph Infra["Bitacora.Infrastructure (persistencia)"]
        DB[BitacoraDbContext<br/>EF Core + SQLite]
        R[RegistroRepository]
        UR[UsuarioRepository]
        HC[HashContrasena<br/>PasswordHasher]
        GT[GeneradorTokenJwt<br/>JwtOpciones]
        MG[Migrations]
    end

    subgraph App["Bitacora.Application (casos de uso)"]
        S[RegistroService / IRegistroService]
        AS[AuthService / IAuthService]
        IR[IRegistroRepository]
        IU[IUsuarioRepository]
        IS[IHashContrasena, IGeneradorToken]
        D[DTOs: RegistroInputDto,<br/>RegistroResumenDto, RegistroDetalleDto]
        V[Base64ImagenAttribute<br/>opcional, Base64 válido, máx. 2 MB]
    end

    subgraph Dom["Bitacora.Domain (núcleo)"]
        E["Entidad Registro<br/>Id, Titulo, Descripcion,<br/>FotoBase64?, FechaCreacion, UsuarioId<br/>+ constantes de las reglas"]
        EU["Entidad Usuario<br/>Id, Correo, PasswordHash, Rol<br/>Roles: Admin, Usuario"]
    end

    Api --> App
    Api --> Infra
    Infra --> App
    Infra --> Dom
    App --> Dom

    R -. implementa .-> IR
    S -. usa .-> IR
    UR -. implementa .-> IU
    HC -. implementa .-> IS
    GT -. implementa .-> IS
    AS -. usa .-> IU
    AS -. usa .-> IS
```

| Proyecto                  | Referencia a                       | Contiene |
|---------------------------|------------------------------------|----------|
| `Bitacora.Domain`         | (ninguno)                          | Entidades `Registro` y `Usuario`, constantes `Roles` y las reglas del negocio (longitudes, tamaño máximo de la foto, contraseña mínima). Sin interfaces ni dependencias |
| `Bitacora.Application`    | Domain                             | DTOs, interfaces `IRegistroRepository`, `IUsuarioRepository`, `IHashContrasena`, `IGeneradorToken`, validación, `RegistroService` (regla del dueño) y `AuthService` (registro, login, Admin semilla) |
| `Bitacora.Infrastructure` | Application, Domain                | `BitacoraDbContext`, repositorios, `HashContrasena` (`PasswordHasher`), `GeneradorTokenJwt`, `JwtOpciones`, migraciones, `AddInfraestructura()` |
| `Bitacora.Api`            | Application, Infrastructure        | Controladores, `JwtBearer`, respuestas 401/403, middleware de errores, Swagger con Bearer, HTTPS, composición de dependencias |

`Bitacora.Api` referencia a Infrastructure solo para registrar las implementaciones en el contenedor de DI
(*composition root*). El controlador trabaja únicamente con `IRegistroService`.

## Flujo de una petición

```mermaid
sequenceDiagram
    participant A as App Android
    participant MW as ExceptionHandlingMiddleware
    participant C as RegistrosController
    participant S as RegistroService
    participant R as RegistroRepository
    participant DB as SQLite

    A->>MW: POST /api/registros (titulo, descripcion, fotoBase64?)
    MW->>C: siguiente middleware
    Note over C: [ApiController] valida el DTO.<br/>Si falla responde 400 con errores por campo
    C->>S: CrearAsync(dto)
    S->>S: asigna FechaCreacion = UtcNow<br/>y normaliza el Base64
    S->>R: AgregarAsync(registro)
    R->>DB: INSERT
    DB-->>R: Id
    R-->>S: ok
    S-->>C: RegistroDetalleDto
    C-->>A: 201 Created + Location: /api/registros/{id}
    Note over MW: Si algo lanza una excepción<br/>responde 500 ProblemDetails genérico
```

## Decisiones

- **`IRegistroRepository` vive en Application**, que es quien la necesita. Así el dominio no conoce
  la persistencia ni depende de nadie. Infrastructure la implementa (por eso referencia a Application).
- **Foto como `FotoBase64` (TEXT, opcional)**, igual que en la entidad del enunciado. Si se envía, se valida que sea
  Base64 válido, no vacío y de 2 MB o menos decodificados. Se guarda en forma canónica (sin el prefijo `data:`).
- **La lista no devuelve la foto.** El repositorio proyecta las columnas sin leer `FotoBase64` y solo calcula
  `tieneFoto`, así la lista es liviana para el `RecyclerView` de Android. La foto se pide con `GET /api/registros/{id}`.
- **Errores en formato ProblemDetails (RFC 9110):** 400 con `errors` por campo, 404 y 500 sin detalles internos.
- **`FechaCreacion` en UTC** asignada por el servidor. No está en el DTO de entrada y PUT no la modifica.
- **Seguridad detrás de interfaces (tarea 2.4).** Application solo conoce `IHashContrasena` e `IGeneradorToken`;
  las implementaciones con `PasswordHasher` y `System.IdentityModel.Tokens.Jwt` están en Infrastructure. La regla
  "un Usuario solo modifica sus registros" vive en `RegistroService`, no en el controlador.
- **El usuario actual sale del token.** El controlador lee `sub` y `role` (`ClaimsPrincipalExtensions`) y los pasa al
  servicio como `UsuarioActual`. Detalle completo en [../../docs/seguridad-jwt.md](../../docs/seguridad-jwt.md).
