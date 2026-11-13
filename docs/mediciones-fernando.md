# Mediciones – Comparativa individual

**Estudiante:** José Fernando Orellana Reyes  
**Fecha de medición:** ____________________  
**Dispositivo/emulador:** Pixel 8, Android ____, API ____  
**API:** `http://10.0.2.2:5080`  
**Registros devueltos por `GET /api/registros`:** ____

> Completa esta tabla con los tiempos que muestra la app. La app usa
> `SystemClock.elapsedRealtime()`, así que los milisegundos incluyen la
> codificación de la petición, la red, la respuesta y el parseo.

---

## 1. Tiempo de respuesta – 5 ejecuciones normales

| Librería | Ejec. 1 | Ejec. 2 | Ejec. 3 | Ejec. 4 | Ejec. 5 | Promedio |
|---|---:|---:|---:|---:|---:|---:|
| Volley | | | | | | |
| OkHttp | | | | | | |
| Retrofit | | | | | | |

**Condiciones:** mismo emulador, misma API, mismo endpoint, sin caché, con el token
válido y el checkbox “Repetir 5 veces” marcado. Se reinició la pantalla entre
librerías para que ninguna se beneficiara del cliente HTTP de otra.

**Observaciones:**

- ______________________________________________________________
- ______________________________________________________________

---

## 2. Escenarios de falla

Completa una fila por librería y escenario. En “Detalle técnico” copia exactamente
lo que muestra la tarjeta; en “Mensaje al usuario” copia el texto del Snackbar.

### 2.1 API detenida

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | | | | `06-api-detenida-volley.png` |
| OkHttp | | | | `07-api-detenida-okhttp.png` |
| Retrofit | | | | `08-api-detenida-retrofit.png` |

### 2.2 Modo avión

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | | | | `09-modo-avion-volley.png` |
| OkHttp | | | | `10-modo-avion-okhttp.png` |
| Retrofit | | | | `11-modo-avion-retrofit.png` |

### 2.3 Tiempo de espera agotado (`/api/pruebas/demora`, 30 s)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | | | | `12-timeout-volley.png` |
| OkHttp | | | | `13-timeout-okhttp.png` |
| Retrofit | | | | `14-timeout-retrofit.png` |

### 2.4 Token inválido (401)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | | | | `15-401-volley.png` |
| OkHttp | | | | `16-401-okhttp.png` |
| Retrofit | | | | `17-401-retrofit.png` |

### 2.5 Token ausente (401)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | | | | `18-sin-token-volley.png` |
| OkHttp | | | | `19-sin-token-okhttp.png` |
| Retrofit | | | | `20-sin-token-retrofit.png` |

### 2.6 Error interno del servidor (500)

| Librería | Detalle técnico | Mensaje al usuario | Tiempo | Captura |
|---|---|---|---:|---|
| Volley | | | | `21-500-volley.png` |
| OkHttp | | | | `22-500-okhttp.png` |
| Retrofit | | | | `23-500-retrofit.png` |

---

## 3. Comportamiento esperado según el código

Estos son los valores que deberían aparecer. Si alguno cambia, anótalo: es
evidencia útil para el informe.

| Escenario | Volley | OkHttp | Retrofit |
|---|---|---|---|
| API detenida | `NoConnectionError`; mensaje “No se pudo conectar…” | `ConnectException`; mensaje “No se pudo conectar…” | `ApiError.SIN_CONEXION`; mensaje “No se pudo conectar…” |
| Modo avión | `NoConnectionError`; mensaje “No se pudo conectar…” | `UnknownHostException` o `ConnectException`; mensaje “No se pudo conectar…” | `ApiError.SIN_CONEXION`; mensaje “No se pudo conectar…” |
| Timeout 30 s | `TimeoutError`; ~20 s por 1 reintento | `SocketTimeoutException`; ~10 s | `ApiError.TIEMPO_AGOTADO`; ~10 s |
| 401 | `AuthFailureError · HTTP 401` | `HTTP 401 Unauthorized` | `ApiError.NO_AUTENTICADO · HTTP 401` |
| 500 | `ServerError · HTTP 500` (puede reintentar) | `HTTP 500 Internal Server Error` | `ApiError.SERVIDOR · HTTP 500` |

---

## 4. Comprobación de estabilidad

- [ ] La app siguió abierta después de los cinco escenarios.
- [ ] No apareció `FATAL EXCEPTION` en Logcat.
- [ ] Se restauró el token original después de las pruebas 401.
- [ ] Se desactivó el modo avión.
- [ ] Se volvió a arrancar la API para dejar el entorno normal.

**Notas de estabilidad:**

- ______________________________________________________________
- ______________________________________________________________
