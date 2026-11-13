# Guion del video individual – Tarea 2.5

**Estudiante:** José Fernando Orellana Reyes  
**Duración objetivo:** 3 min 40 s (máximo 4 min)  
**Formato:** cámara encendida + pantalla del emulador/Android Studio  
**Requisito:** explicar el código de Volley y OkHttp y mostrar al menos dos escenarios de falla.

---

## Antes de grabar

1. API encendida y con al menos 2 registros.
2. App instalada y sesión iniciada como Admin.
3. Pantalla **Comparativa Fernando** abierta.
4. Token válido (si hiciste pruebas de 401, toca **Restaurar**).
5. Modo avión desactivado.
6. Android Studio abierto con estos archivos listos para mostrar:
   - `ClienteVolleyFernando.java`
   - `ClienteOkHttpFernando.java`
   - `ComparativaFernandoActivity.java`
7. Logcat abierto y filtrado por `ComparativaVolley|ComparativaOkHttp|ComparativaRetrofit`.
8. Graba con la cámara en un recuadro y la pantalla completa; prueba 10 segundos
   antes para verificar audio y que se lea el código.

---

## 0:00 – 0:20 · Presentación

**En cámara:**

> “Buenas tardes. Soy José Fernando Orellana Reyes, cuenta 202210010352. En este video
> presento la comparativa individual de Volley, OkHttp y Retrofit para la Tarea 2.5.
> La pantalla que ven es Comparativa Fernando, disponible desde el menú de la lista de
> registros.”

**En pantalla:** muestra la pantalla con los tres botones y el selector de endpoint.

---

## 0:20 – 0:50 · Qué hace la pantalla

**En pantalla:** señala los controles.

> “La pantalla permite ejecutar la misma consulta protegida, GET /api/registros, con
> tres clientes distintos. Cada botón muestra la cantidad de registros y el tiempo en
> milisegundos. El checkbox repite cinco veces y calcula el promedio. El selector
> permite probar el endpoint de demora de 30 segundos y el endpoint de error 500.
> Abajo están los botones para probar un token inválido, un token ausente y restaurar
> el token original.”

---

## 0:50 – 1:40 · Código de Volley

**En pantalla:** abre `ClienteVolleyFernando.java` y resalta:

- `new JsonArrayRequest(...)`
- `@Override public Map<String, String> getHeaders()`
- `new DefaultRetryPolicy(10_000, 1, ...)`
- el callback `respuesta -> { ... }`

> “Volley usa JsonArrayRequest. La petición se ejecuta en segundo plano y la respuesta
> llega directamente al hilo principal, por eso puedo actualizar la interfaz sin usar
> un Handler. El token se envía sobrescribiendo getHeaders: armo un Map, agrego Accept
> y, si hay token en SessionManager, agrego Authorization con el esquema Bearer. El
> tiempo de espera se configura con DefaultRetryPolicy: diez segundos por intento y un
> reintento. En la prueba de demora, Volley reportó TimeoutError a los 30.5 segundos,
> mientras OkHttp y Retrofit cortaron a los 10 segundos; ese es el resultado que se
> documenta. La respuesta JSON se convierte con Gson a una lista de RegistroResumenDto.”

**En pantalla:** muestra la línea del `DefaultRetryPolicy` y el callback.

---

## 1:40 – 2:30 · Código de OkHttp

**En pantalla:** abre `ClienteOkHttpFernando.java` y resalta:

- `new Request.Builder()`
- `.header("Authorization", "Bearer " + token)`
- `llamada.enqueue(new Callback() { ... })`
- `principal.post(() -> callback.onResultado(resultado))`
- `gson.fromJson(json, TIPO_LISTA)`

> “OkHttp construye el Request manualmente con Request.Builder. El token se agrega con
> header. La llamada se ejecuta con enqueue, que es asíncrono. A diferencia de Volley,
> el callback de OkHttp llega en un hilo de fondo, así que no puedo tocar la interfaz
> directamente. Por eso tengo un Handler asociado a Looper.getMainLooper y el método
> publicar hace principal.post para entregar el resultado en el hilo principal. OkHttp
> no convierte JSON: yo leo el cuerpo con response.body().string() y lo paso a Gson.
> También configuré timeouts de diez segundos y retryOnConnectionFailure en false para
> que la medición sea determinista.”

**En pantalla:** muestra el `Handler` y el `enqueue`.

---

## 2:30 – 3:10 · Dos escenarios de falla

### Escenario A: API detenida

1. Detén la API en la terminal (`Ctrl + C`).
2. En la app, desmarca “Repetir 5 veces”.
3. Toca **Volley**, espera el error y muestra la tarjeta.
4. Toca **OkHttp** y muestra el error.

> “Con la API detenida, Volley reporta NoConnectionError y OkHttp ConnectException. En
> los dos casos la app muestra un mensaje claro y sigue funcionando; no se cierra.”

### Escenario B: token inválido (401)

1. Vuelve a arrancar la API.
2. Toca **Token inválido**.
3. Toca **Retrofit** y muestra el 401.
4. Toca **Restaurar**.

> “Para el 401 altero la firma del token. Retrofit recibe ApiError.NO_AUTENTICADO con
> HTTP 401 y muestra el mensaje de sesión expirada. Después toco Restaurar y el token
> original vuelve a funcionar. La app nunca se cierra.”

> Si prefieres un segundo escenario más visual, usa el error 500: selecciona
> GET /api/pruebas/error, toca las tres librerías y muestra el mensaje “El servidor
> tuvo un problema”.

**En pantalla:** deja visible Logcat sin `FATAL EXCEPTION`.

---

## 3:10 – 3:40 · Resultados y recomendación

**En pantalla:** muestra la pantalla con los promedios de las 5 ejecuciones.

> “En las cinco ejecuciones, los promedios fueron: Volley 15 milisegundos, OkHttp 4
> milisegundos y Retrofit 15 milisegundos. La diferencia es de pocos milisegundos y en
> una red real es despreciable. Por mantenibilidad, conversión automática de JSON,
> token centralizado en un interceptor y reutilización del repositorio de la tarea 2.3,
> recomiendo Retrofit para el proyecto final. OkHttp es la mejor opción cuando se
> necesita control total del protocolo, y Volley sirve para peticiones simples que se
> benefician del hilo principal.”

---

## 3:40 – 4:00 · Cierre

**En cámara:**

> “El código está en la rama feature/comparativa-fernando-orellana y el pull request es
> el número 23 del repositorio. Ahí están los tres clientes, la pantalla, la guía de
> mediciones y el informe. Gracias.”

---

## Lista de comprobación antes de subir el video

- [ ] Se ve la cámara al inicio y al final.
- [ ] Se explica `JsonArrayRequest`, `getHeaders()` y `DefaultRetryPolicy`.
- [ ] Se explica `Request.Builder`, `enqueue()` y el `Handler`.
- [ ] Se muestran al menos dos escenarios de falla.
- [ ] Se ve que la app sigue abierta después de los errores.
- [ ] Se muestran los promedios de las 5 ejecuciones.
- [ ] Se dice la recomendación y se justifica.
- [ ] Dura entre 3 y 4 minutos.
- [ ] El audio es claro y la pantalla se lee.
