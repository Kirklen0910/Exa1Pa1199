# Reparto del trabajo Android (6 integrantes, por capas)

Cada integrante trabaja en una rama `feature/...` y abre un Pull Request hacia `main`.
El orden de merge está pensado para que **después de cada merge el proyecto compile**
(`.\gradlew assembleDebug`): cada paso solo depende de lo que ya está en `main`.

| # | Integrante | Rama | Capa / archivos | Depende de |
|---|---|---|---|---|
| 1 | Integrante 1 | `feature/config-red` | `gradle/libs.versions.toml` (Retrofit, converter-gson, logging-interceptor, lifecycle, recyclerview, swiperefreshlayout), `app/build.gradle.kts` (`buildConfig = true`, `API_BASE_URL`), `AndroidManifest.xml` (INTERNET, CAMERA, RECORD_AUDIO, `networkSecurityConfig`), `res/xml/network_security_config.xml`, `README.md` | — |
| 2 | Integrante 2 | `feature/data-remote` | `data/remote/RegistroApi.java`, `data/remote/ApiClient.java`, `data/remote/dto/*` (`RegistroResumenDto`, `RegistroDetalleDto`, `RegistroRequestDto`, `ProblemDetailsDto`), regla en `keepRules/rules.keep` | 1 (dependencias y `BuildConfig`) |
| 3 | Integrante 3 | `feature/data-repository` | `data/repository/RegistroRepository.java`, `ApiError.java`, `RepositorioCallback.java`, `Cancelable.java` (mapeo 4xx/5xx, `SocketTimeoutException` vs `IOException`, errores 400 por campo) | 2 |
| 4 | Integrante 4 | `feature/viewmodel` | `viewmodel/UiState.java`, `ListaRegistrosViewModel`, `DetalleRegistroViewModel`, `FormularioRegistroViewModel`, `util/ImagenUtils.java` (1024 px, JPEG 70, Base64), `util/FechaUtils.java` | 3 |
| 5 | Integrante 5 | `feature/ui-formulario` | `ui/UiHelper.java` (incluye edge-to-edge / `WindowInsets`), `res/layout/view_estado_error.xml`, `res/layout/view_toolbar.xml`, tema `NoActionBar` en `res/values*/themes.xml`, `ui/FormularioRegistroActivity.java`, `res/layout/activity_formulario_registro.xml` (cámara con FileProvider, voz con `RecognizerIntent`), sus strings y su `<activity>` en el manifiesto | 4 |
| 6 | Integrante 6 | `feature/ui-lista-detalle` | `ui/RegistroAdapter.java`, `ui/ListaRegistrosActivity.java`, `ui/DetalleRegistroActivity.java`, `item_registro.xml`, `activity_lista_registros.xml`, `activity_detalle_registro.xml`, cambio del LAUNCHER a `ListaRegistrosActivity`, **eliminación del código viejo de Contactos** (SQLite, `CallActivity`, layouts, permiso `CALL_PHONE`), `docs/android-flujo.md` | 5 (usa `UiHelper` y abre `FormularioRegistroActivity`) |

## Orden de merge y por qué compila en cada paso

1. **config-red** → solo agrega dependencias, `BuildConfig` y permisos. El código viejo de Contactos sigue
   compilando y funcionando.
2. **data-remote** → usa Retrofit/OkHttp/Gson y `BuildConfig.API_BASE_URL`, que ya existen tras el paso 1.
   Nadie lo usa todavía, pero compila.
3. **data-repository** → solo depende de `data/remote`.
4. **viewmodel** → depende del repositorio y de lifecycle (paso 1).
5. **ui-formulario** → depende de los ViewModel; declara su propia `<activity>`. Todavía no es la pantalla de
   inicio, así que la app vieja sigue abriendo.
6. **ui-lista-detalle** → depende de `UiHelper` y `FormularioRegistroActivity` (paso 5). En este mismo PR se
   cambia el LAUNCHER y se borra el código de Contactos, de modo que nunca queda un manifiesto apuntando a una
   clase inexistente.

## Reglas para evitar conflictos

- `strings.xml` y `AndroidManifest.xml` los tocan varios: agregar líneas al final de su sección y hacer
  `git pull --rebase origin main` antes de abrir el PR.
- Antes de pedir revisión, ejecutar `.\gradlew assembleDebug` en la rama ya rebasada.
- Cada PR lo revisa al menos otro integrante (idealmente el de la capa siguiente).
- Todos deben poder explicar el flujo completo: Vista → ViewModel → Repositorio → Retrofit → API
  (ver `docs/android-flujo.md`).

## Video y documento

| Tarea | Responsables |
|---|---|
| Grabar CRUD completo contra la API | Integrantes 5 y 6 |
| Casos de API apagada (Sin conexión), tiempo agotado y modo avión | Integrantes 3 y 4 |
| Capturas de Logcat (`okhttp.OkHttpClient`) para el PDF | Integrante 2 |
| Armar el PDF (diagrama + capturas + reparto) | Integrante 1 |
