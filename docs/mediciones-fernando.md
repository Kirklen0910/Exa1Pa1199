# Mediciones – Comparativa individual

**Estudiante:** José Fernando Orellana Reyes  
**Fecha de medición:** 13/11/2026  
**Dispositivo/emulador:** Pixel 8 (AVD `Pixel_8`), Android 17 (API 37), 1080 × 2400  
**API:** `http://10.0.2.2:5080`  
**Registros devueltos por `GET /api/registros`:** 2 (`Prueba 2`, `Prueba 1`)

> **Evidencia utilizada:** todas las capturas de este documento pertenecen a la
> pantalla **Comparativa · Fernando Orellana**. Se descartaron las capturas de la
> pantalla “Comparativa de librerías” porque corresponden a otra implementación y
> no permiten comparar los clientes de esta tarea.

---

## 1. Tiempo de respuesta – 5 ejecuciones normales

| Librería | Ejec. 1 | Ejec. 2 | Ejec. 3 | Ejec. 4 | Ejec. 5 | Promedio |
|---|---:|---:|---:|---:|---:|---:|
| Volley | 16 | 16 | 16 | 16 | 15 | **15 ms** |
| OkHttp | 3 | 4 | 4 | 7 | 3 | **4 ms** |
| Retrofit | 17 | 16 | 14 | 16 | 15 | **15 ms** |

**Condiciones:** mismo emulador, misma API, mismo endpoint, sin caché, con el token
válido y el checkbox “Repetir 5 veces” marcado. Se hizo **una ejecución de
calentamiento por librería** antes de las cinco mediciones. Sin ese calentamiento,
la primera llamada de OkHttp y Retrofit tardó 550 ms y 584 ms respectivamente,
porque incluye la inicialización del cliente y de Gson; esos valores no se mezclan
con la latencia de las cinco ejecuciones comparables.

**Observaciones:**

- OkHttp fue el más rápido: **4 ms** de promedio. Volley y Retrofit quedaron en
  **15 ms**, muy cerca entre sí.
- La diferencia de unos 11 ms es pequeña frente a la latencia de una red móvil real,
  pero refleja el sobrecoste de las capas de abstracción de Volley y Retrofit.
- La app calcula el promedio con división entera; por eso OkHttp muestra 4 ms
  aunque la suma de sus cinco tiempos sea 21 ms.
- Captura: `01-normal-fernando.png`.

---

## 2. Escenarios de falla

En “Detalle técnico” se copia exactamente lo que muestra la tarjeta. En “Mensaje al
usuario” se copia el texto del Snackbar. Los tiempos son los que reporta la app con
`SystemClock.elapsedRealtime()`.

### 2.1 API detenida

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `NoConnectionError · causa=ConnectException` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 121 ms | `06-api-detenida-fernando.png` |
| OkHttp | `ConnectException: Failed to connect to /10.0.2.2:5080` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 485 ms | `06-api-detenida-fernando.png` |
| Retrofit | `ApiError.SIN_CONEXION` | No se pudo conectar con el servidor. Revisa tu conexión a internet o que la API esté encendida. | 16 ms | `06-api-detenida-fernando.png` |

> En la primera ejecución de OkHttp con la API detenida se reutilizó una conexión
> del pool y apareció `IOException: unexpected end of stream`; en la segunda
> ejecución, ya sin conexión persistente, apareció `ConnectException`. La app manejó
> ambos casos sin cerrarse. En la tabla se registra el resultado representativo.

### 2.2 Modo avión

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `NoConnectionError · causa=ConnectException` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 16 ms | `07-modo-avion-fernando.png` |
| OkHttp | `ConnectException: Failed to connect to /10.0.2.2:5080` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 2 ms | `07-modo-avion-fernando.png` |
| Retrofit | `ApiError.SIN_CONEXION` | No se pudo conectar con el servidor. Revisa tu conexión a internet o que la API esté encendida. | 18 ms | `07-modo-avion-fernando.png` |

### 2.3 Tiempo de espera agotado (`/api/pruebas/demora`, 30 s)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `TimeoutError` | El servidor tardó demasiado en responder. Inténtalo de nuevo. | 30 503 ms | `03-timeout-fernando.png` |
| OkHttp | `SocketTimeoutException: timeout` | El servidor tardó demasiado en responder. Inténtalo de nuevo. | 10 002 ms | `03-timeout-fernando.png` |
| Retrofit | `SocketTimeoutException: timeout` | El servidor tardó demasiado en responder. Inténtalo de nuevo. | 10 002 ms | `03-timeout-fernando.png` |

> **Observación sobre Volley:** la política configurada es `DefaultRetryPolicy(10_000,
> 1, 1.0f)`, es decir, 10 s por intento y un reintento. En esta medición Volley
> reportó `TimeoutError` a los 30 503 ms, un tiempo que coincide con la demora del
> endpoint; se registra el valor observado tal cual. OkHttp y Retrofit cortaron a
> los 10 002 ms.
>
> **Observación sobre Retrofit:** para los endpoints de prueba se usa `PruebasApi`
> directamente, no el repositorio de la tarea 2.3. Por eso el detalle es
> `SocketTimeoutException`; en la ruta normal `GET /api/registros`, el repositorio
> traduce el timeout a `ApiError.TIEMPO_AGOTADO`.

### 2.4 Token inválido (401)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `AuthFailureError · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 653 ms | `04-401-invalido-fernando.png` |
| OkHttp | `HTTP 401 Unauthorized` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 634 ms | `04-401-invalido-fernando.png` |
| Retrofit | `ApiError.NO_AUTENTICADO · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 616 ms | `04-401-invalido-fernando.png` |

### 2.5 Token ausente (401)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `AuthFailureError · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 16 ms | `05-401-sin-token-fernando.png` |
| OkHttp | `HTTP 401 Unauthorized` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 2 ms | `05-401-sin-token-fernando.png` |
| Retrofit | `ApiError.NO_AUTENTICADO · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 15 ms | `05-401-sin-token-fernando.png` |

### 2.6 Error interno del servidor (500)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `ServerError · HTTP 500` | El servidor tuvo un problema (código 500). Inténtalo más tarde. | 16 ms | `02-500-fernando.png` |
| OkHttp | `HTTP 500 Internal Server Error` | El servidor tuvo un problema (código 500). Inténtalo más tarde. | 4 ms | `02-500-fernando.png` |
| Retrofit | `HTTP 500 Internal Server Error` | El servidor tuvo un problema (código 500). Inténtalo más tarde. | 15 ms | `02-500-fernando.png` |

> Retrofit usa `PruebasApi` para `/api/pruebas/error`; por eso el detalle es
> `HTTP 500 Internal Server Error`. En la ruta normal, el repositorio de la tarea
> 2.3 traduce el 500 a `ApiError.SERVIDOR · HTTP 500`.

---

## 3. Comportamiento observado según el código

| Escenario | Volley | OkHttp | Retrofit |
|---|---|---|---|
| API detenida | `NoConnectionError`; mensaje “No se pudo conectar…” | `ConnectException`; mensaje “No se pudo conectar…” | `ApiError.SIN_CONEXION`; mensaje “No se pudo conectar…” |
| Modo avión | `NoConnectionError`; mensaje “No se pudo conectar…” | `ConnectException`; mensaje “No se pudo conectar…” | `ApiError.SIN_CONEXION`; mensaje “No se pudo conectar…” |
| Timeout 30 s | `TimeoutError`; 30 503 ms observados | `SocketTimeoutException`; 10 002 ms | `SocketTimeoutException`; 10 002 ms |
| 401 (inválido o ausente) | `AuthFailureError · HTTP 401` | `HTTP 401 Unauthorized` | `ApiError.NO_AUTENTICADO · HTTP 401` |
| 500 | `ServerError · HTTP 500` | `HTTP 500 Internal Server Error` | `HTTP 500 Internal Server Error` en la ruta de prueba; `ApiError.SERVIDOR` en la ruta normal |

---

## 4. Comprobación de estabilidad

- [x] La app siguió abierta después de todos los escenarios.
- [x] No apareció `FATAL EXCEPTION` en Logcat.
- [x] Se restauró el token original después de las pruebas 401.
- [x] Se desactivó el modo avión.
- [x] Se volvió a arrancar la API para dejar el entorno normal.
- [x] Se guardó el Logcat filtrado en `docs/capturas/fernando/logcat.txt`.

**Notas de estabilidad:**

- La app mostró las tarjetas de error y los Snackbars sin cerrarse; los botones
  volvieron a quedar habilitados después de cada llamada.
- El archivo `logcat.txt` se filtró con las etiquetas `ComparativaVolley`,
  `ComparativaOkHttp`, `ComparativaRetrofit` y las líneas de `AndroidRuntime`; no
  contiene `FATAL EXCEPTION`.
- La pantalla quedó en el endpoint normal y con el token restaurado al terminar.
