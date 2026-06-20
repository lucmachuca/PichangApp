# Etapa 1: Construcción (Build)
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copiar el wrapper de maven y la configuración
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Copiar el código fuente de todos los microservicios
COPY msvc-comunicacion msvc-comunicacion
COPY msvc-match msvc-match
COPY msvc-notificacion msvc-notificacion
COPY msvc-seguridad msvc-seguridad
COPY msvc-usuario msvc-usuario

# Dar permisos de ejecución al wrapper
RUN chmod +x ./mvnw

# Recibimos el nombre del módulo a compilar
ARG MODULE_NAME
# Construimos solo el módulo especificado para optimizar el tiempo
RUN ./mvnw clean package -pl ${MODULE_NAME} -am -DskipTests

# Etapa 2: Ejecución (Run)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG MODULE_NAME
# Definimos la variable de entorno para saber qué módulo está corriendo
ENV MODULE=${MODULE_NAME}

# Copiamos el .jar generado en la etapa anterior (ignorando el archivo jar.original)
COPY --from=builder /app/${MODULE_NAME}/target/*.jar app.jar

# Comando de ejecución
ENTRYPOINT ["sh", "-c", "java -jar app.jar"]
