# Exa1Pa1199 – Bitácora de campo

Proyecto grupal de Programación Móvil I.

- **Primer parcial:** aplicación Android con fotografía (SQLite).
- **Tarea 2.2:** API REST en C# con ASP.NET Core y arquitectura limpia → carpeta `BitacoraApi/`.
- **Tarea 2.3:** aplicación Android con MVVM, Retrofit y OkHttp que consume la API → carpeta `app/`.
-   **Enlace Video Explicando como la APP consume la API** https://drive.google.com/file/d/12ksSMyVUCVZAQfG_djKlr1rZZes7mq7P/view?usp=sharing

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

## 1. Ejecutar la API (tarea 2.2)

```powershell
cd BitacoraApi/src/Bitacora.Api
dotnet ef database update --project ../Bitacora.Infrastructure
dotnet run
```

- La API queda escuchando en `http://0.0.0.0:5080`.
- Documentación Swagger: `http://localhost:5080/swagger`.
- Colección de Postman: `BitacoraApi/postman/` (importar la colección y el environment **Bitácora - Local**).
- Más detalles en `BitacoraApi/README.md`.

## 2. Ejecutar la app Android (tarea 2.3)

1. Con la API en ejecución, abrir la carpeta raíz del repositorio en Android Studio.
2. Esperar la sincronización de Gradle.
3. Elegir el emulador y presionar **Run ▶**.
4. Al abrir la app por primera vez, conceder el permiso de **red local** (Android 17).

La app se conecta a `http://10.0.2.2:5080/`, que en el emulador corresponde a la computadora local. La URL está en `app/build.gradle.kts` (`BuildConfig.API_BASE_URL`).

### Probar en un celular físico

1. Conectar la computadora y el celular a la misma red WiFi.
2. Cambiar `API_BASE_URL` por la IP de la computadora (por ejemplo, `http://192.168.1.50:5080/`).
3. Agregar esa IP en `app/src/main/res/xml/network_security_config.xml`.
4. Permitir el puerto 5080 en el firewall de Windows.

## Estructura

```
Exa1Pa1199/
├── app/            → App Android (ui, viewmodel, data/remote, data/repository, util)
├── BitacoraApi/    → API REST (Domain, Application, Infrastructure, Api), Postman y docs
└── docs/           → Diagrama del flujo Android y reparto del trabajo
```
