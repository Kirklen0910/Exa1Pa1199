# Bitácora API

API REST en ASP.NET Core 8 que consume la app Android **Exa1Pa1199** (tareas 2.3-2.5).
Guarda registros de una bitácora de campo (título, descripción, foto opcional en Base64 y fecha de creación) en SQLite.

## Requisitos

- .NET SDK 8 o superior (con runtime de ASP.NET Core 8).
- Herramienta de EF Core:

  ```bash
  dotnet tool install --global dotnet-ef
  # si ya la tienes: dotnet tool update --global dotnet-ef
  ```

## Ejecutar

Todos los comandos se ejecutan desde la carpeta `BitacoraApi/`.

```bash
# 1. Restaurar y compilar
dotnet build Bitacora.sln

# 2. Crear/actualizar la base de datos SQLite (src/Bitacora.Api/bitacora.db)
dotnet ef database update --project src/Bitacora.Infrastructure --startup-project src/Bitacora.Api

# 3. Levantar la API
dotnet run --project src/Bitacora.Api
```

La API escucha en `http://0.0.0.0:5080`, es decir, en todas las interfaces:

| Desde                     | URL base                      |
|---------------------------|-------------------------------|
| La misma PC / Postman     | `http://localhost:5080`       |
| Emulador Android          | `http://10.0.2.2:5080`        |
| Celular físico (misma red)| `http://<IP-de-la-PC>:5080`   |

Swagger UI: <http://localhost:5080/swagger>

> **Celular físico:** hay que permitir el puerto 5080 en el Firewall de Windows. El emulador no lo necesita.
> **Android:** como la API es HTTP (sin TLS), la app necesita `android:usesCleartextTraffic="true"` o un `network_security_config` que permita `10.0.2.2`.

Para cambiar el puerto, edita `Kestrel:Endpoints:Http:Url` en `src/Bitacora.Api/appsettings.json`.

## Migraciones

La migración inicial (`InicialRegistros`) ya está incluida en `src/Bitacora.Infrastructure/Persistence/Migrations`.
Crea la tabla `Registros` con `Id`, `Titulo`, `Descripcion`, `FotoBase64` (TEXT, opcional) y `FechaCreacion`.

```bash
# Aplicar migraciones pendientes
dotnet ef database update --project src/Bitacora.Infrastructure --startup-project src/Bitacora.Api

# Crear una migración nueva después de cambiar la entidad o el DbContext
dotnet ef migrations add NombreDelCambio --project src/Bitacora.Infrastructure --startup-project src/Bitacora.Api --output-dir Persistence/Migrations

# Empezar con la base vacía
#   borra src/Bitacora.Api/bitacora.db y vuelve a ejecutar database update
```

El archivo `bitacora.db` está en `.gitignore`: cada integrante genera el suyo con `database update`.

## Endpoints

Base: `/api/registros`

| Método | Ruta                  | Éxito                               | Errores                         |
|--------|-----------------------|-------------------------------------|---------------------------------|
| GET    | `/api/registros`      | 200 lista **sin** la foto (incluye `tieneFoto`) | 406 si se pide un formato que no es JSON |
| GET    | `/api/registros/{id}` | 200 registro con `fotoBase64` (o `null`) | 404 no existe              |
| POST   | `/api/registros`      | 201 + cabecera `Location` + registro | 400 validación                 |
| PUT    | `/api/registros/{id}` | 204 sin cuerpo                      | 400 validación, 404 no existe   |
| DELETE | `/api/registros/{id}` | 204 sin cuerpo                      | 404 no existe                   |

Cualquier error no controlado devuelve **500** como `application/problem+json`, con mensaje genérico y `traceId`, sin exponer detalles de la excepción. El detalle queda en el log del servidor.

### Cuerpo de POST / PUT

```json
{
  "titulo": "Visita de campo",
  "descripcion": "Texto dictado por voz en Android",
  "fotoBase64": "iVBORw0KGgoAAAANSUhEUg..."
}
```

| Campo           | Reglas                                                                    |
|-----------------|---------------------------------------------------------------------------|
| `titulo`        | Obligatorio, 3-100 caracteres                                             |
| `descripcion`   | Obligatoria, máximo 500 caracteres                                        |
| `fotoBase64`    | Opcional (omitir o `null`). Si se envía: Base64 válido, no vacío, máximo **2 MB** decodificado. Acepta el prefijo `data:image/...;base64,` (se guarda sin él) |
| `fechaCreacion` | **No se envía.** La asigna el servidor al crear (UTC, ISO-8601 con `Z`) y no cambia con PUT. Si el cliente la manda, se ignora |

El PUT reemplaza `titulo`, `descripcion` y `fotoBase64`: si se omite la foto, el registro queda sin foto.

Respuesta de `GET /api/registros/{id}` y `POST`:

```json
{
  "id": 1,
  "titulo": "Visita de campo",
  "descripcion": "Texto dictado por voz en Android",
  "fotoBase64": "iVBORw0KGgoAAAANSUhEUg...",
  "fechaCreacion": "2026-11-02T03:34:38.2578513Z"
}
```

Elemento de `GET /api/registros`:

```json
{ "id": 1, "titulo": "Visita de campo", "descripcion": "...", "fechaCreacion": "2026-11-02T03:34:38.2578513Z", "tieneFoto": true }
```

Ejemplo de respuesta 400:

```json
{
  "title": "One or more validation errors occurred.",
  "status": 400,
  "errors": {
    "Titulo": ["El título es obligatorio."],
    "Descripcion": ["La descripción es obligatoria."],
    "FotoBase64": ["La foto no es una cadena Base64 válida."]
  }
}
```

## Postman

Importa los dos archivos de `postman/`:

- `Bitacora.postman_collection.json`: la colección.
- `Bitacora.postman_environment.json`: el entorno **Bitácora - Local**, con la variable de entorno `baseUrl`
  (`http://localhost:5080`). Selecciónalo arriba a la derecha antes de ejecutar.

Ejecuta la colección completa en orden con el **Collection Runner**: el POST exitoso guarda `registroId`
para las peticiones siguientes. Cada endpoint tiene al menos una prueba exitosa y una fallida, cada una con
un test del código de estado.

También se puede correr desde la terminal con el Postman CLI:

```bash
postman collection run postman/Bitacora.postman_collection.json -e postman/Bitacora.postman_environment.json
```

## Documentación

- [docs/arquitectura.md](docs/arquitectura.md): capas y dependencias (diagrama Mermaid).
- [docs/reparto.md](docs/reparto.md): propuesta de ramas y reparto del trabajo.
