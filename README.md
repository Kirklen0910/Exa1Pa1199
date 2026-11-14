# Exa1Pa1199 – Bitácora de campo

Proyecto grupal de Programación Móvil I.

- **Primer parcial:** aplicación Android con fotografía (SQLite).
- **Tarea 2.2:** API REST en C# con ASP.NET Core y arquitectura limpia → carpeta `BitacoraApi/`.
- **Tarea 2.3:** aplicación Android con MVVM, Retrofit y OkHttp que consume la API → carpeta `app/`.
- **Tarea 2.4:** seguridad con JWT, roles (`Admin`/`Usuario`), HTTPS y manejo de sesión en Android
  → ver [docs/seguridad-jwt.md](docs/seguridad-jwt.md).
- **Enlace Video Explicando como la APP consume la API Tarea 2.2 y 2.3** https://drive.google.com/file/d/12ksSMyVUCVZAQfG_djKlr1rZZes7mq7P/view?usp=sharing
- **Enlace Video Explicando como la APP consume la API de la Tarea 2.4** https://drive.google.com/file/d/17IP-fYvSHA_tFcfhcVSDuTilTAjb2s8P/view?usp=sharing
  
## Integrantes

| Nombre | Número de cuenta |
|---|---|
| Carlos Roberto Cárdenas Martínez | 202430010599 |
| Jennifer Mariela Márquez Chacón | 202330010411 |
| José Fernando Orellana Reyes | 202210010352 |
| Michael Abad Díaz Castro | 202310040165 |
| Romel Márquez | 202410010359 |
| Fernando Abad Diaz Castro | 202410040257 |

## Requisitos

- .NET SDK 8 o superior y la herramienta `dotnet-ef` (`dotnet tool install --global dotnet-ef`).
- Android Studio con un emulador (Pixel con API 37 recomendado).
- Git.

## 1. Configurar los secretos de la API (una vez por PC)

La clave de firma del JWT y el usuario Admin **no están en el repositorio**: cada integrante los guarda en su PC
con `dotnet user-secrets`. Desde la carpeta `BitacoraApi/`:

```powershell
cd BitacoraApi
$bytes = New-Object byte[] 64; [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
dotnet user-secrets set "Jwt:Key" ([Convert]::ToBase64String($bytes)) --project src/Bitacora.Api
dotnet user-secrets set "Admin:Correo" "admin@bitacora.local" --project src/Bitacora.Api
dotnet user-secrets set "Admin:Password" "<una-contraseña-de-8-o-más-caracteres>" --project src/Bitacora.Api
dotnet user-secrets list --project src/Bitacora.Api
```

Sin `Jwt:Key` la API no arranca. Con `Admin:Correo` y `Admin:Password` la API crea ese usuario con rol **Admin**
al iniciar. Los demás usuarios se registran desde la app (rol **Usuario**).

## 2. Ejecutar la API (tareas 2.2 y 2.4)

```powershell
cd BitacoraApi
dotnet run --project src/Bitacora.Api
```

- Al iniciar aplica las migraciones y crea el Admin semilla. Si vienes de la tarea 2.3, borra antes
  `BitacoraApi/src/Bitacora.Api/bitacora.db`.
- Escucha en `https://localhost:7080` (Swagger: <https://localhost:7080/swagger>, botón **Authorize**) y en
  `http://0.0.0.0:5080`. Las peticiones HTTP se redirigen a HTTPS, salvo las del emulador (`10.0.2.2`) en desarrollo.
- Colección de Postman: `BitacoraApi/postman/` (importar la colección y el environment **Bitácora - Local** y escribir
  `adminCorreo` y `adminPassword` en *Current value*).
- Más detalles en `BitacoraApi/README.md`.

## 3. Ejecutar la app Android (tareas 2.3 y 2.4)

1. Con la API en ejecución, abrir la carpeta raíz del repositorio en Android Studio.
2. Esperar la sincronización de Gradle.
3. Elegir el emulador y presionar **Run ▶**.
4. Al abrir la app por primera vez, conceder el permiso de **red local** (Android 17).
5. La app abre en **Iniciar sesión**: entrar con el Admin semilla o tocar **Regístrate** para crear un usuario.

La app se conecta a `http://10.0.2.2:5080/`, que en el emulador corresponde a la computadora local. La URL está en `app/build.gradle.kts` (`BuildConfig.API_BASE_URL`).

Manejo de sesión:

- El token, su expiración, el rol y el correo se guardan cifrados con `EncryptedSharedPreferences`.
- Un interceptor de OkHttp agrega `Authorization: Bearer <token>` a cada petición.
- Si la API responde **401** (token vencido o inválido), la app borra la sesión y vuelve al login con un mensaje.
  Si responde **403**, muestra que no tiene permiso y la sesión sigue abierta.
- El botón **Eliminar** solo aparece para el rol Admin. **Cerrar sesión** está en el menú de la lista.

### Probar en un celular físico

1. Conectar la computadora y el celular a la misma red WiFi.
2. Cambiar `API_BASE_URL` por la IP de la computadora (por ejemplo, `http://192.168.1.50:5080/`).
3. Agregar esa IP en `app/src/main/res/xml/network_security_config.xml` y en `HostsSinRedireccionHttps` de
   `BitacoraApi/src/Bitacora.Api/appsettings.Development.json`.
4. Permitir el puerto 5080 en el firewall de Windows.

## Estructura

```
Exa1Pa1199/
├── app/            → App Android (ui, viewmodel, data/remote, data/repository, data/session, util)
├── BitacoraApi/    → API REST (Domain, Application, Infrastructure, Api), Postman y docs
└── docs/           → Flujo Android, seguridad JWT/OAuth 2.0 y reparto del trabajo
```
