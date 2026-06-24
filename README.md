Nombre del Proyecto PichangApp

Descripción PichangApp es una red social y plataforma de emparejamiento orientada a deportistas amateur. Su objetivo es resolver la dificultad de encontrar compañeros o rivales del mismo nivel para practicar deportes como fútbol, básquetbol o boxeo. Los usuarios pueden configurar perfiles físicos, descubrir a otros deportistas, hacer match y organizar encuentros a través de un chat privado. Está dirigido a cualquier persona que busque hacer deporte y no tenga con quién jugar.

Tecnologías utilizadas Frontend: Flutter y Dart (Aplicación móvil multiplataforma). Backend: Java 21 y Spring Boot 4.0.6 (Arquitectura de Microservicios). Mensajería Asíncrona: RabbitMQ. Plataforma Cloud (BaaS): Supabase (Autenticación, WebSockets Realtime y Storage para imágenes). Bases de Datos: PostgreSQL. Infraestructura: Oracle Cloud Infrastructure (OCI), Docker y Docker Compose.

Requisitos previos Flutter SDK versión 3.0 o superior. Java Development Kit (JDK) versión 21. Apache Maven para la gestión de dependencias en Java. Docker Engine y Docker Compose instalados en el sistema.

Instalación Paso 1: Clonar el repositorio Comando: git clone https://github.com/TuUsuario/PichangApp.git

Paso 2: Instalar dependencias del Servidor Navegar a la carpeta PichangApp/PichangApp y compilar los microservicios Comando: mvn clean install -DskipTests

Paso 3: Instalar dependencias del Cliente Navegar a la carpeta PichangApp_FrontEnd/Proyecto/pichangapp Comando: flutter pub get

Configuración En el Frontend (Flutter) se configuran variables para Supabase en el archivo .env o de forma global: SUPABASE_URL: URL del proyecto alojado en Supabase SUPABASE_ANON_KEY: Clave pública de autenticación y lectura

En el Backend, las conexiones de base de datos y RabbitMQ se manejan vía la red interna en el archivo docker-compose.yml. Se inyectan variables dinámicamente como MSVC_USUARIO_URL=http://pichangapp-usuario:8001.

Uso / Ejecución Para levantar el proyecto en el servidor Oracle Cloud o instancia local: En el directorio raíz del backend, ejecutar el comando para levantar los contenedores Docker: docker-compose up -d --build Esto iniciará todos los microservicios Java y RabbitMQ simultáneamente.

Para iniciar la aplicación cliente: En el directorio PichangApp_FrontEnd/Proyecto/pichangapp, ejecutar el comando: flutter run

Arquitectura del proyecto El sistema está diseñado bajo una Arquitectura Orientada a Eventos y Microservicios. Capa Cliente (Frontend): Maneja la interfaz gráfica, los estados y peticiones HTTP usando Flutter. Capa Cloud (BaaS Supabase): Funciona como puente en la nube para almacenar fotos y sincronizar mensajes de chat vía WebSockets en tiempo real. Broker de Mensajería (RabbitMQ): Gestiona la comunicación asíncrona y alertas. Ecosistema de Microservicios (Backend): Separado en módulos independientes. El módulo msvc-usuario maneja perfiles, msvc-seguridad administra bloqueos, msvc-match procesa el sistema de emparejamiento, msvc-comunicacion configura las salas, y msvc-notificacion escucha eventos de RabbitMQ para emitir alertas. Todo alojado en Oracle Cloud.

Base de datos El modelo relacional opera en PostgreSQL. Las tablas principales son: usuarios: Gestiona la autenticación y datos básicos. perfil_deportivo: Almacena edad, deporte y parámetros como guardia en boxeo o posición. matches_sociales: Tabla pivote que registra cuando dos usuarios se aprueban mutuamente. salas_chat: Creadas automáticamente al confirmar un match. mensajes_chat: Almacena el historial de texto vinculado a cada sala.

Documentación de la API La documentación automatizada Swagger se expone en la ruta /swagger-ui.html de cada microservicio.

Endpoint de ejemplo 1: Actualizar Perfil Deportivo Método: PUT Ruta: /api/users/{id}/profile Request Body: {"edad": 24, "deportePrincipal": "BOXEO", "atributosDeportivos": {"peso": 75, "guardia": "Ortodoxa"}} Response: 200 OK (Perfil actualizado correctamente).

Endpoint de ejemplo 2: Desbloquear Usuario Método: POST Ruta: /api/safety/desbloquear Request Body: {"idUsuarioOrigen": 1, "idUsuarioBloqueado": 2} Response: 200 OK (Chat restaurado y emparejamiento reactivado).

Estructura del equipo / Autores Rodrigo Rojas -- Arquitecto Backend y Desarrollador Java Matias Segovia -- Desarrollador Frontend y Lógica de Vistas. Luciano Machuca -- Desarrollador Full-Stack e Integrador

Tests / Pruebas Para ejecutar las pruebas en el Backend Java, usar el comando: mvn test

Para ejecutar las pruebas unitarias y de interfaz en el Frontend Flutter, usar el comando: flutter test

Licencia El proyecto es de carácter académico y privado para evaluación docente.
