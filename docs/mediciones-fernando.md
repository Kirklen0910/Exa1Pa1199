# Mediciones – Comparativa individual

**Estudiante:** José Fernando Orellana Reyes  
**Fecha de medición:** 13/11/2026  
**Dispositivo/emulador:** Pixel 8 (AVD `Pixel_8`), Android 17 (API 37), 1080 × 2400  
**API:** `http://10.0.2.2:5080`  
**Registros devueltos por `GET /api/registros`:** 2 (`Prueba 2`, `Prueba 1`)

> **Evidencia utilizada:** todas las capturas de este documento pertenecen a la
> pantalla **Comparativa · Fernando Orellana** y se tomaron con la versión final de
> la app (commit de esta rama). Las capturas de la pantalla “Comparativa de
> librerías” no se usan: corresponden a otra implementación.

---

## 1. Tiempo de respuesta – 5 ejecuciones normales

| Librería | Ejec. 1 | Ejec. 2 | Ejec. 3 | Ejec. 4 | Ejec. 5 | Promedio |
|---|---:|---:|---:|---:|---:|---:|
| Volley | 17 | 15 | 15 | 16 | 15 | **15 ms** |
| OkHttp | 10 | 4 | 3 | 3 | 3 | **4 ms** |
| Retrofit | 19 | 12 | 14 | 11 | 12 | **13 ms** |

**Condiciones:** mismo emulador, misma API, mismo endpoint, sin caché, con el token
válido y el checkbox “Repetir 5 veces” marcado. Antes de las cinco mediciones se hizo
**una ejecución de calentamiento por librería**. La primera llamada después de instalar
la app tardó 517 ms (Volley), 994 ms (OkHttp) y 976 ms (Retrofit); esos valores no se
incluyen en el promedio porque mezclan la inicialización de los clientes con la latencia.

**Observaciones:**

- OkHttp fue el más rápido: **4 ms** de promedio. Retrofit quedó en **13 ms** y Volley
  en **15 ms**.
- La diferencia de unos 10 ms es pequeña frente a la latencia de una red móvil real.
- La app calcula el promedio con división entera. Por ejemplo, Volley suma 78 ms en
  cinco ejecuciones y muestra 15 ms (78 / 5 = 15,6).
- Captura: `01-normal-fernando.png`.

---

## 2. Escenarios de falla

En “Detalle técnico” se copia exactamente lo que muestra la tarjeta. En “Mensaje al
usuario” se copia el texto del Snackbar. Los tiempos son los que reporta la app con
`SystemClock.elapsedRealtime()`.

### 2.1 API detenida

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `NoConnectionError · causa=ConnectException` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 433 ms | `06-api-detenida-fernando.png` |
| OkHttp | `ConnectException: Failed to connect to /10.0.2.2:5080` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 409 ms | `06-api-detenida-fernando.png` |
| Retrofit | `ApiError.SIN_CONEXION` | No se pudo conectar con el servidor. Revisa tu conexión a internet o que la API esté encendida. | 373 ms | `06-api-detenida-fernando.png` |

### 2.2 Modo avión

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `NoConnectionError · causa=ConnectException` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 19 ms | `07-modo-avion-fernando.png` |
| OkHttp | `SocketException: Software caused connection abort` | No se pudo conectar con el servidor. Revisa tu conexión o que la API esté encendida. | 2 ms | `07-modo-avion-fernando.png` |
| Retrofit | `ApiError.SIN_CONEXION` | No se pudo conectar con el servidor. Revisa tu conexión a internet o que la API esté encendida. | 18 ms | `07-modo-avion-fernando.png` |

> En modo avión OkHttp no reportó `ConnectException`, sino `SocketException`: el
> sistema cortó la conexión antes de establecerla. El mensaje para el usuario es el
> mismo.

### 2.3 Tiempo de espera agotado (`/api/pruebas/demora`, 30 s)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `TimeoutError` | El servidor tardó demasiado en responder. Inténtalo de nuevo. | 20 502 ms | `03-timeout-fernando.png` |
| OkHttp | `SocketTimeoutException: timeout` | El servidor tardó demasiado en responder. Inténtalo de nuevo. | 10 002 ms | `03-timeout-fernando.png` |
| Retrofit | `SocketTimeoutException: timeout` | El servidor tardó demasiado en responder. Inténtalo de nuevo. | 10 002 ms | `03-timeout-fernando.png` |

> **Volley: corrección aplicada.** En la primera prueba Volley tardó ~30 s. Causa: el
> código usaba `DEFAULT_BACKOFF_MULT` (1.0), y Volley multiplica el timeout del
> reintento por ese valor (10 s y luego 20 s). El log mostraba
> `socket-retry [timeout=10000]` seguido de `socket-timeout-giveup [timeout=20000]`.
> Se cambió el multiplicador a 0. La tabla muestra la medición con la corrección:
> cada intento dura 10 s y el corte ocurre a los 20 502 ms.
>
> **Retrofit:** para los endpoints de prueba se usa `PruebasApi` directamente, no el
> repositorio de la tarea 2.3. Por eso el detalle es `SocketTimeoutException`; en la
> ruta normal, el repositorio traduce el timeout a `ApiError.TIEMPO_AGOTADO`.

### 2.4 Token inválido (401)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `AuthFailureError · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 543 ms | `04-401-invalido-fernando.png` |
| OkHttp | `HTTP 401 Unauthorized` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 524 ms | `04-401-invalido-fernando.png` |
| Retrofit | `ApiError.NO_AUTENTICADO · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 506 ms | `04-401-invalido-fernando.png` |

### 2.5 Token ausente (401)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `AuthFailureError · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 17 ms | `05-401-sin-token-fernando.png` |
| OkHttp | `HTTP 401 Unauthorized` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 2 ms | `05-401-sin-token-fernando.png` |
| Retrofit | `ApiError.NO_AUTENTICADO · HTTP 401` | Tu sesión expiró o no es válida. Inicia sesión de nuevo. | 18 ms | `05-401-sin-token-fernando.png` |

### 2.6 Error interno del servidor (500)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | `ServerError · HTTP 500` | El servidor tuvo un problema (código 500). Inténtalo más tarde. | 17 ms | `02-500-fernando.png` |
| OkHttp | `HTTP 500 Internal Server Error` | El servidor tuvo un problema (código 500). Inténtalo más tarde. | 3 ms | `02-500-fernando.png` |
| Retrofit | `HTTP 500 Internal Server Error` | El servidor tuvo un problema (código 500). Inténtalo más tarde. | 17 ms | `02-500-fernando.png` |

> Retrofit usa `PruebasApi` para `/api/pruebas/error`; por eso el detalle es
> `HTTP 500 Internal Server Error`. En la ruta normal, el repositorio de la tarea
> 2.3 traduce el 500 a `ApiError.SERVIDOR · HTTP 500`.

---

## 3. Comportamiento observado según el código

| Escenario | Volley | OkHttp | Retrofit |
|---|---|---|---|
| API detenida | `NoConnectionError`; mensaje “No se pudo conectar…” | `ConnectException`; mensaje “No se pudo conectar…” | `ApiError.SIN_CONEXION`; mensaje “No se pudo conectar…” |
| Modo avión | `NoConnectionError`; mensaje “No se pudo conectar…” | `SocketException`; mensaje “No se pudo conectar…” | `ApiError.SIN_CONEXION`; mensaje “No se pudo conectar…” |
| Timeout 30 s | `TimeoutError`; 20 502 ms (10 s + 1 reintento) | `SocketTimeoutException`; 10 002 ms | `SocketTimeoutException`; 10 002 ms |
| 401 (inválido o ausente) | `AuthFailureError · HTTP 401` | `HTTP 401 Unauthorized` | `ApiError.NO_AUTENTICADO · HTTP 401` |
| 500 | `ServerError · HTTP 500` | `HTTP 500 Internal Server Error` | `HTTP 500 Internal Server Error` en la ruta de prueba; `ApiError.SERVIDOR` en la ruta normal |

---

## 4. Comprobación de estabilidad

- [x] La app siguió abierta después de todos los escenarios.
- [x] No apareció `FATAL EXCEPTION` ni cierre del proceso `com.uth.exa1pa1199` en Logcat.
- [x] Se restauró el token original después de las pruebas 401 (botón **Restaurar**).
- [x] Se desactivó el modo avión.
- [x] Se volvió a arrancar la API (puertos 5080 y 7080 escuchando).
- [x] Se guardó el Logcat de toda la sesión en `docs/capturas/fernando/logcat.txt`.

**Notas de estabilidad:**

- Las tarjetas de error, los Snackbars y los botones se comportaron correctamente en
  todos los escenarios.
- El archivo `logcat.txt` incluye los tags `ComparativaVolley`, `ComparativaOkHttp`,
  `ComparativaRetrofit` y los mensajes `Volley` con el detalle de los reintentos.
- Después de las pruebas el token venció (10 minutos). La consulta normal devuelve 401
  hasta que se vuelva a iniciar sesión; no afecta a las mediciones, que se tomaron con
  el token válido.
