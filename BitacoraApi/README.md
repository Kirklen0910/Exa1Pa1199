# Bitácora API

API REST en ASP.NET Core 8 que consume la app Android **Exa1Pa1199** (tareas 2.3-2.5).
Guarda registros de una bitácora de campo (título, descripción, foto opcional en Base64 y fecha de creación) en SQLite.
Desde la tarea 2.4 está protegida con **JWT y roles** (`Admin` y `Usuario`).

## Requisitos

- .NET SDK 8 o superior (con runtime de ASP.NET Core 8).
- Herramienta de EF Core:

  ```bash
  dotnet tool install --global dotnet-ef
  # si ya la tienes: dotnet tool update --global dotnet-ef
  ```

- Certificado HTTPS de desarrollo: `dotnet dev-certs https` (y opcionalmente `dotnet dev-certs https --trust`).

## Configurar los secretos (user-secrets)

La clave de firma del JWT y las credenciales del Admin **no están en el repositorio**. Cada integrante las configura
una sola vez en su PC con `dotnet user-secrets` (se guardan en
`%APPDATA%\Microsoft\UserSecrets\1cd95531-1539-43b3-9a83-01e0eb20930d\secrets.json`, fuera del repositorio).
Desde la carpeta `BitacoraApi/`:

```powershell
# 1. Clave de firma aleatoria (64 bytes en Base64; mínimo 32 bytes)
$bytes = New-Object byte[] 64; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
dotnet user-secrets set "Jwt:Key" ([Convert]::ToBase64String($bytes)) --project src/Bitacora.Api

# 2. Usuario Admin semilla (se crea al iniciar la API)
dotnet user-secrets set "Admin:Correo" "admin@bitacora.local" --project src/Bitacora.Api
dotnet user-secrets set "Admin:Password" "<una-contraseña-de-8-o-más-caracteres>" --project src/Bitacora.Api

# 3. Comprobar
dotnet user-secrets list --project src/Bitacora.Api
```

En Linux/macOS la clave se puede generar con `openssl rand -base64 64`.

- Sin `Jwt:Key` (o con menos de 32 bytes) la API **no arranca** y muestra el comando a ejecutar.
- Sin `Admin:Correo`/`Admin:Password` la API arranca, pero no crea el Admin (lo avisa en el log).
- User-secrets solo se cargan en el entorno *Development* (el que usa `dotnet run`). En producción se usan variables de
  entorno: `Jwt__Key`, `Admin__Correo`, `Admin__Password`.
- Si una clave llega a subirse al repositorio, hay que **cambiarla** (repetir el paso 1); borrarla en otro commit no basta.

En `appsettings.json` solo están los datos no secretos:

```json
"Jwt": { "Issuer": "Bitacora.Api", "Audience": "Bitacora.App", "ExpiraMinutos": 10 }
```

## Ejecutar

Todos los comandos se ejecutan desde la carpeta `BitacoraApi/`.

```bash
# 1. Restaurar y compilar
dotnet build Bitacora.sln

# 2. Levantar la API (aplica las migraciones pendientes y crea el Admin semilla al iniciar)
dotnet run --project src/Bitacora.Api
```

La API escucha en dos puertos:

| Desde                      | URL base                       |
|----------------------------|--------------------------------|
| La misma PC / Postman / curl | `https://localhost:7080`     |
| Emulador Android           | `http://10.0.2.2:5080`         |
| Celular físico (misma red) | `http://<IP-de-la-PC>:5080` (agregar la IP a `HostsSinRedireccionHttps`) |

Swagger UI: <https://localhost:7080/swagger>

### HTTPS y por qué se mantiene HTTP 5080

`UseHttpsRedirection` está activo: `http://localhost:5080/...` responde **307** hacia `https://localhost:7080/...`.
La excepción es la lista `HostsSinRedireccionHttps` de `appsettings.Development.json` (solo `10.0.2.2`):

- El emulador de Android llega a la PC por `10.0.2.2`, un nombre que el certificado de desarrollo (emitido para
  `localhost`) no incluye, y además no confía en ese certificado. Redirigirlo a HTTPS haría fallar todas las peticiones.
- Por eso, **solo en desarrollo**, la app usa `http://10.0.2.2:5080`, permitido únicamente para ese host en
  `app/src/main/res/xml/network_security_config.xml`.
- En producción (`ASPNETCORE_ENVIRONMENT=Production`) la lista no existe: todo se redirige a HTTPS y se envía HSTS.

Para cambiar los puertos edita `Kestrel:Endpoints` y `HttpsPort` en `src/Bitacora.Api/appsettings.json`.

> **Celular físico:** hay que permitir el puerto 5080 en el Firewall de Windows y agregar la IP de la PC a
> `HostsSinRedireccionHttps`. El emulador no lo necesita.

## Migraciones

| Migración | Cambios |
|---|---|
| `InicialRegistros` | Tabla `Registros` (`Id`, `Titulo`, `Descripcion`, `FotoBase64`, `FechaCreacion`) |
| `UsuariosYPropietario` | Tabla `Usuarios` (`Id`, `Correo` único, `PasswordHash`, `Rol`) y columna `Registros.UsuarioId` (FK al creador) |

La API aplica las migraciones pendientes al iniciar. También se pueden aplicar a mano:

```bash
dotnet ef database update --project src/Bitacora.Infrastructure --startup-project src/Bitacora.Api

# Crear una migración nueva después de cambiar una entidad o el DbContext
dotnet ef migrations add NombreDelCambio --project src/Bitacora.Infrastructure --startup-project src/Bitacora.Api --output-dir Persistence/Migrations
```

**Si vienes de la tarea 2.3, borra `src/Bitacora.Api/bitacora.db`** antes de iniciar: los registros viejos no tienen
`UsuarioId`. El archivo está en `.gitignore`: cada integrante genera el suyo.

## Endpoints

### Autenticación (`[AllowAnonymous]`)

| Método | Ruta                  | Cuerpo | Éxito | Errores |
|--------|-----------------------|--------|-------|---------|
| POST   | `/api/auth/registro`  | `{ "correo", "password" }` | 201 `{ id, correo, rol: "Usuario" }` | 400 validación (correo válido, contraseña de 8 a 100), 409 correo ya registrado |
| POST   | `/api/auth/login`     | `{ "correo", "password" }` | 200 `{ token, expiracion, rol, correo }` | 400 validación, **401** credenciales inválidas (mismo mensaje si falla el correo o la contraseña) |

El registro siempre crea el rol **Usuario**. El único Admin es el semilla configurado con user-secrets.

### Registros (`[Authorize]`, encabezado `Authorization: Bearer <token>`)

| Método | Ruta                  | Quién | Éxito | Errores |
|--------|-----------------------|-------|-------|---------|
| GET    | `/api/registros`      | Usuario, Admin | 200 lista **sin** la foto (incluye `tieneFoto` y `usuarioId`) | 401, 406 |
| GET    | `/api/registros/{id}` | Usuario, Admin | 200 registro con `fotoBase64` (o `null`) | 401, 404 |
| POST   | `/api/registros`      | Usuario, Admin | 201 + `Location`; el creador queda en `usuarioId` | 400, 401 |
| PUT    | `/api/registros/{id}` | Dueño o Admin | 204 | 400, 401, **403** si el registro es de otro usuario, 404 |
| DELETE | `/api/registros/{id}` | Solo Admin (`[Authorize(Roles = "Admin")]`) | 204 | 401, **403** si no es Admin, 404 |

- **401** (no autenticado): falta el token, venció (10 minutos, sin tolerancia) o fue alterado. Incluye
  `WWW-Authenticate: Bearer ...`.
- **403** (prohibido): el token es válido pero el rol o el dueño no lo permiten.
- Ambos se devuelven como `ProblemDetails` (`application/problem+json`):

```json
{
  "type": "https://tools.ietf.org/html/rfc9110#section-15.5.4",
  "title": "Acceso prohibido.",
  "status": 403,
  "detail": "Solo puede modificar los registros que usted creó.",
  "traceId": "00-656e3ca154c32f13c539694d2568c686-4591d0b1f8391f11-00"
}
```

Cualquier error no controlado devuelve **500** como `application/problem+json`, con mensaje genérico y `traceId`,
sin exponer detalles de la excepción. El detalle queda en el log del servidor.

### Probar desde Swagger

1. Abrir <https://localhost:7080/swagger>.
2. Ejecutar `POST /api/auth/login` y copiar el valor de `token`.
3. Botón **Authorize** → pegar el token (sin la palabra `Bearer`) → **Authorize**.
4. Los endpoints con candado ya envían el token.

### Cuerpo de POST / PUT de registros

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
| `fechaCreacion` | **No se envía.** La asigna el servidor al crear (UTC, ISO-8601 con `Z`) y no cambia con PUT |
| `usuarioId`     | **No se envía.** Se toma del claim `sub` del token al crear y no cambia con PUT |

## Postman

Importa los dos archivos de `postman/`:

- `Bitacora.postman_collection.json`: la colección.
- `Bitacora.postman_environment.json`: el entorno **Bitácora - Local** con `baseUrl` (`https://localhost:7080`) y las
  variables secretas `adminCorreo`, `adminPassword` y `tokenVencido`.

Antes de ejecutar:

1. Selecciona el entorno **Bitácora - Local** y escribe en **Current value** de `adminCorreo` y `adminPassword` los
   valores que configuraste con user-secrets (el *Current value* no se exporta ni se sube al repositorio).
2. Si Postman muestra un error de certificado, desactiva *Settings → General → SSL certificate verification*
   o confía en el certificado con `dotnet dev-certs https --trust`.

Ejecuta la colección completa en orden con el **Collection Runner**:

| Carpeta | Contenido |
|---|---|
| 0. Auth | Login del Admin y registro + login de los usuarios A y B. Guarda `tokenAdmin`, `tokenUsuario` y `tokenUsuarioB` |
| 1–4 | CRUD de la tarea 2.2. Heredan `Bearer {{tokenUsuario}}` de la colección |
| 5. Pruebas de seguridad (obligatorias) | Sin token → 401, Usuario DELETE → 403, Admin DELETE → 204, Usuario modifica ajeno → 403, token alterado → 401, token vencido → 401, login incorrecto → 401 |
| 6. DELETE | Eliminación con `tokenAdmin` |

La prueba **5b (token vencido)** guarda el token actual en `tokenVencido` y se omite hasta que pasen 10 minutos;
vuelve a ejecutarla después para ver el 401 con `error_description="The token expired"`.

Desde la terminal con el Postman CLI:

```bash
postman collection run postman/Bitacora.postman_collection.json -e postman/Bitacora.postman_environment.json \
  --env-var "adminCorreo=admin@bitacora.local" --env-var "adminPassword=<contraseña>" --insecure
```

## Documentación

- [docs/arquitectura.md](docs/arquitectura.md): capas y dependencias (diagrama Mermaid).
- [docs/reparto.md](docs/reparto.md): reparto del trabajo de la tarea 2.2.
- [../docs/seguridad-jwt.md](../docs/seguridad-jwt.md): configuración JWT, protección de la clave, 401/403 y OAuth 2.0.
- [../docs/reparto-2.4.md](../docs/reparto-2.4.md): reparto de la tarea 2.4.
