Unidades 7 y 9 del sílabo: organización del cliente Android, introducción a MVVM y consumo de API REST con Retrofit y OkHttp.
Contexto

En esta tarea la aplicación del primer parcial (fotografía y voz a texto) deja de guardar solo en SQLite y pasa a trabajar con la API de la Bitácora de campo creada en la tarea 2.2. Se mantiene la captura de la fotografía y la descripción por voz, pero ahora el registro se envía al servidor y se consulta desde él.
Objetivo general

Desarrollar una aplicación Android en Java (o Kotlin), organizada con MVVM y un repositorio, que consuma la API REST de la tarea 2.2 mediante Retrofit y OkHttp, mostrando correctamente los estados de carga, error y falta de conexión.
Objetivos específicos

    Separar el proyecto en capas de interfaz, lógica de pantalla y acceso a servicios.
    Definir la interfaz del servicio con Retrofit y serializar los datos en JSON.
    Configurar OkHttp con tiempos de espera e interceptor de registro.
    Enviar al servidor la fotografía en Base64 y la descripción obtenida por voz.
    Integrar las respuestas de la API con las vistas.
    Manejar los estados de carga, éxito, lista vacía, error del servidor, tiempo de espera y falta de conexión.

Requerimientos técnicos
1. Organización del proyecto

    ui: Activities o Fragments y adaptador del RecyclerView. Solo muestran datos y reciben eventos.
    viewmodel: clases que extienden ViewModel y exponen el estado de la pantalla con LiveData (o StateFlow en Kotlin).
    data/remote: interfaz RegistroApi de Retrofit, configuración del cliente y DTO.
    data/repository: clase RegistroRepository, que es la única que llama a la API.

Ninguna Activity debe llamar directamente a Retrofit.
2. Cliente HTTP

    Interfaz de Retrofit con las anotaciones @GET, @POST, @PUT, @DELETE, @Path y @Body para los cinco endpoints de la tarea 2.2.
    Conversión JSON con Gson (o Moshi).
    OkHttpClient con connectTimeout y readTimeout definidos y un HttpLoggingInterceptor activo solo en modo de depuración.
    La URL base debe estar en una sola constante o en BuildConfig. En el emulador, la máquina local se accede con 10.0.2.2.
    Permiso INTERNET en el manifiesto. Si usan HTTP sin cifrar durante el desarrollo, deben limitarlo con un archivo network_security_config y explicarlo en el documento.

3. Pantallas y funciones

    Listado: RecyclerView con los registros obtenidos por GET, con opción de actualizar.
    Detalle: muestra la foto decodificada desde Base64, el título, la descripción y la fecha.
    Formulario para crear y editar: captura de foto con la cámara y descripción por voz a texto (reutilizar lo hecho en el primer parcial). Antes de convertir a Base64, la imagen debe reducirse (por ejemplo, a un máximo de 1024 px y JPEG con calidad 70) para no superar el límite de la API.
    Eliminar con un diálogo de confirmación.
    Validar en el cliente los mismos límites que valida la API (título y descripción), y aun así mostrar los mensajes de error 400 que devuelva el servidor.

4. Estados de la pantalla

La aplicación debe mostrar de forma clara cada uno de estos casos:

    Cargando (ProgressBar visible y botones deshabilitados).
    Lista vacía.
    Error del servidor (4xx y 5xx) con un mensaje comprensible.
    Sin conexión o tiempo de espera agotado, con un botón Reintentar.

Entregables

    Enlace al repositorio de GitHub (puede ser el mismo de la tarea 2.2, en una carpeta separada para Android, o uno nuevo).
    Video de 3 a 5 minutos donde se vea el CRUD completo funcionando contra la API, y los casos de API apagada y modo avión.
    Documento PDF breve con el diagrama del flujo Vista → ViewModel → Repositorio → Retrofit → API, capturas de Logcat con las peticiones y respuestas, y la distribución del trabajo.

Criterios de evaluación
Criterio 	Puntaje
Organización en capas y uso de ViewModel y repositorio 	1%
Consumo correcto de los cinco endpoints con Retrofit 	1%
Fotografía en Base64 y descripción por voz enviadas a la API 	1%
Manejo de estados de carga, error, tiempo de espera y falta de conexión 	1%
Video, documento y participación en GitHub 	1%
Total 	5%
Indicaciones finales

    La API de la tarea 2.2 debe estar en ejecución durante las pruebas; si cambian algún endpoint, actualicen también la colección de Postman.
    Pueden apoyarse en Codex o Claude, pero cada integrante debe poder explicar el flujo completo de una petición.
