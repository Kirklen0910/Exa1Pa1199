Unidades 7 y 8 del sílabo: arquitectura limpia y desarrollo de API REST en C# con ASP.NET Core.
Contexto

En el primer parcial cada equipo construyó una aplicación que guardaba en SQLite registros con fotografía y una descripción obtenida mediante voz a texto. En el segundo parcial esa información pasará a un servidor. En esta tarea el equipo construirá la API REST que recibirá esos registros; en las tareas 2.3, 2.4 y 2.5 la aplicación Android la consumirá y se protegerá. El recurso de trabajo será una Bitácora de campo: cada registro tiene un título, una descripción, una fotografía en Base64 y una fecha.
Objetivo general

Desarrollar una API REST en C# con ASP.NET Core, organizada por capas según los principios de arquitectura limpia, que permita administrar los registros de la bitácora y que esté documentada y probada con OpenAPI y Postman.
Objetivos específicos

    Organizar la solución en las capas de dominio, aplicación, infraestructura y presentación.
    Definir la entidad, los modelos de entrada y salida, la interfaz del repositorio y los casos de uso.
    Implementar los endpoints CRUD con los métodos y códigos de respuesta HTTP correctos.
    Validar las solicitudes y aplicar reglas de negocio.
    Centralizar el manejo de errores.
    Documentar y probar los endpoints con OpenAPI (Swagger) y Postman.
    Registrar el avance del equipo en GitHub.

Requerimientos técnicos
1. Estructura de la solución

Crear una solución con cuatro proyectos (por ejemplo, con dotnet new sln, dotnet new classlib y dotnet new webapi):

    Bitacora.Domain: entidad Registro y reglas propias del negocio. No depende de ningún otro proyecto.
    Bitacora.Application: modelos de entrada y salida (DTO), interfaz IRegistroRepository y servicios o casos de uso (crear, listar, consultar, actualizar y eliminar).
    Bitacora.Infrastructure: acceso a la base de datos con Entity Framework Core (SQLite o SQL Server) e implementación del repositorio.
    Bitacora.Api: controladores, configuración, inyección de dependencias y middleware.

Las dependencias deben apuntar hacia el dominio: la API conoce a la aplicación y a la infraestructura, pero el dominio no conoce a nadie.
2. Entidad Registro

    Id (entero o GUID)
    Titulo (obligatorio, de 3 a 100 caracteres)
    Descripcion (obligatoria, máximo 500 caracteres; en Android se obtendrá por voz a texto)
    FotoBase64 (opcional; si se envía, debe ser Base64 válido y no superar 2 MB)
    FechaCreacion (la asigna el servidor, no el cliente)

3. Endpoints
Método 	Ruta 	Acción 	Respuestas esperadas
GET 	/api/registros 	Listar registros (sin la foto completa, para aligerar la respuesta) 	200
GET 	/api/registros/{id} 	Consultar un registro con su foto 	200, 404
POST 	/api/registros 	Crear un registro 	201 (con encabezado Location), 400
PUT 	/api/registros/{id} 	Actualizar un registro 	200 o 204, 400, 404
DELETE 	/api/registros/{id} 	Eliminar un registro 	204, 404
4. Validación y manejo de errores

    Validar los modelos de entrada (Data Annotations o FluentValidation) y responder 400 con el detalle de los campos inválidos.
    Implementar un middleware o manejador global de excepciones que responda 500 con un objeto ProblemDetails, sin exponer el detalle interno del error.
    No devolver la entidad de base de datos directamente: usar los DTO de salida.

5. Documentación y pruebas

    Habilitar OpenAPI y mostrar la documentación con Swagger UI (o Scalar).
    Crear una colección de Postman con una variable de entorno baseUrl y al menos una prueba exitosa y una fallida por endpoint.

6. GitHub

    Repositorio del equipo con .gitignore para .NET (sin carpetas bin ni obj).
    Cada integrante debe tener commits propios. Se recomienda trabajar con ramas y solicitudes de integración.
    Archivo README con los pasos para ejecutar la API y aplicar las migraciones (dotnet ef database update).

Entregables

    Enlace al repositorio de GitHub.
    Colección de Postman exportada (.json).
    Documento PDF breve con: diagrama de capas y dependencias, captura de Swagger, capturas de las pruebas en Postman (exitosas y con error) y la distribución del trabajo entre los integrantes.

Criterios de evaluación
Criterio 	Puntaje
Estructura por capas y dependencias correctas 	1%
Endpoints CRUD con métodos y códigos HTTP correctos 	1%
Validación de entradas y manejo centralizado de errores 	1%
Documentación OpenAPI y pruebas en Postman 	1%
Repositorio GitHub, README y participación de los integrantes 	1%
Total 	5%
Indicaciones finales

    Esta API será la base de las tareas 2.3, 2.4 y 2.5; conviene dejarla funcionando y bien documentada.
    Pueden apoyarse en Codex o Claude para comprender errores o proponer pruebas, pero cada integrante debe poder explicar cualquier parte del código entregado.
    El equipo puede repartirse el trabajo: mientras unos construyen la API, otros pueden adelantar las pantallas Android de la tarea 2.3.
