# Checklist de entrega – Tarea 2.5

**Estudiante:** José Fernando Orellana Reyes  
**Rama:** `feature/comparativa-fernando-orellana`  
**Pull request:** https://github.com/Kirklen0910/Exa1Pa1199/pull/23

## Código y repositorio

- [x] Rama personal creada desde `main`.
- [x] Cliente Volley con `JsonArrayRequest`, `getHeaders()` y `DefaultRetryPolicy`.
- [x] Cliente OkHttp con `Request` manual, `enqueue()` y Gson.
- [x] Cliente Retrofit reutilizando `RegistroRepository` de la tarea 2.3.
- [x] Pantalla de laboratorio con tres botones, tiempos y promedios.
- [x] Selector de endpoints de demora y error 500.
- [x] Botones para token inválido, token ausente y restaurar.
- [x] Actualización de UI en el hilo principal.
- [x] Cancelación de llamadas en `onDestroy()`.
- [x] `bash gradlew assembleDebug` compila sin errores.
- [x] Rama publicada en GitHub.
- [x] Pull request abierto hacia `main`.

## Documentación

- [x] Informe en Markdown: `docs/comparativa-fernando-orellana.md`.
- [x] Guía de ejecución y capturas: `docs/mediciones-fernando.md`.
- [x] Guion del video: `docs/guion-video-fernando.md`.
- [x] Generador del PDF: `docs/generar_informe.py`.
- [x] Tabla de 5 ejecuciones completada con los tiempos definitivos.
- [x] Capturas insertadas en el anexo del PDF.
- [x] PDF final revisado: `Informe_Tarea_2.5_Fernando_Orellana.pdf`.

## Evidencias

- [x] `00-menu-fernando.png`: menú con la opción Comparativa Fernando.
- [x] `01-normal-fernando.png`: medición normal de las tres librerías.
- [x] `02-500-fernando.png`: error 500.
- [x] `03-timeout-fernando.png`: timeout.
- [x] `04-401-invalido-fernando.png`: token inválido (401).
- [x] `05-401-sin-token-fernando.png`: token ausente (401).
- [x] `06-api-detenida-fernando.png`: API detenida.
- [x] `07-modo-avion-fernando.png`: modo avión.
- [x] `logcat.txt` sin `FATAL EXCEPTION`.

Guardar todo en `docs/capturas/fernando/`. Las capturas de la pantalla
“Comparativa de librerías” no se usan porque pertenecen a otra implementación.

## Video

- [ ] Grabar con cámara encendida.
- [ ] Explicar Volley (`JsonArrayRequest`, `getHeaders`, `DefaultRetryPolicy`).
- [ ] Explicar OkHttp (`Request.Builder`, `enqueue`, Gson, `Handler`).
- [ ] Mostrar al menos dos escenarios de falla.
- [ ] Mostrar los promedios de las 5 ejecuciones.
- [ ] Decir la recomendación y justificarla.
- [ ] Duración entre 3 y 4 minutos.

## Revisión

- [ ] Otro integrante revisa el pull request.
- [ ] Se atienden los comentarios de la revisión.
- [ ] Se confirma que el PR queda listo para integrarse a `main`.
