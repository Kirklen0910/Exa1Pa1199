# Arquitectura

La solución `Bitacora.sln` sigue una arquitectura por capas (estilo *Clean Architecture*):
**todas las dependencias apuntan hacia el dominio**, y el dominio no depende de nadie.

## Capas y dependencias entre proyectos

```mermaid
flowchart TB
    subgraph Api["Bitacora.Api (presentación)"]
        C[RegistrosController]
        M[ExceptionHandlingMiddleware]
        P["Program.cs<br/>DI, Swagger, Kestrel 0.0.0.0:5080"]
    end

    subgraph Infra["Bitacora.Infrastructure (persistencia)"]
        DB[BitacoraDbContext<br/>EF Core + SQLite]
        R[RegistroRepository]
        MG[Migrations]
    end

    subgraph App["Bitacora.Application (casos de uso)"]
        S[RegistroService / IRegistroService]
        IR[IRegistroRepository]
        D[DTOs: RegistroInputDto,<br/>RegistroResumenDto, RegistroDetalleDto]
        V[Base64ImagenAttribute<br/>opcional, Base64 válido, máx. 2 MB]
    end

    subgraph Dom["Bitacora.Domain (núcleo)"]
        E["Entidad Registro<br/>Id, Titulo, Descripcion,<br/>FotoBase64?, FechaCreacion<br/>+ constantes de las reglas"]
    end

    Api --> App
    Api --> Infra
    Infra --> App
    Infra --> Dom
    App --> Dom

    R -. implementa .-> IR
    S -. usa .-> IR
```

| Proyecto                  | Referencia a                       | Contiene |
|---------------------------|------------------------------------|----------|
| `Bitacora.Domain`         | (ninguno)                          | Entidad `Registro` y las reglas del negocio (longitudes de título y descripción, tamaño máximo de la foto) como constantes. Sin interfaces ni dependencias |
| `Bitacora.Application`    | Domain                             | DTOs, interfaz `IRegistroRepository`, validación (Data Annotations + `Base64Imagen`), `RegistroService` |
| `Bitacora.Infrastructure` | Application, Domain                | `BitacoraDbContext`, `RegistroRepository`, migraciones, `AddInfraestructura()` |
| `Bitacora.Api`            | Application, Infrastructure        | Controlador, middleware de errores, Swagger, composición de dependencias |

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
