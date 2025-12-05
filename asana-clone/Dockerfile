# ===================================================================
# MULTI-STAGE BUILD PARA OPTIMIZAR TAMAÑO DE IMAGEN
# ===================================================================

# Etapa 1: Build
FROM maven:3.9-eclipse-temurin-17-alpine AS build

# Directorio de trabajo
WORKDIR /app

# Copiar archivos de configuración de Maven
COPY pom.xml .
COPY src ./src

# Compilar y empaquetar la aplicación
# Saltamos los tests para un build más rápido (puedes quitarlo si quieres ejecutar tests)
RUN mvn clean package -DskipTests

# ===================================================================
# Etapa 2: Runtime
FROM eclipse-temurin:17-jre-alpine

# Crear usuario no-root para mayor seguridad
RUN addgroup -S spring && adduser -S spring -G spring

# Directorio de trabajo
WORKDIR /app

# Copiar el JAR desde la etapa de build
COPY --from=build /app/target/*.jar app.jar

# Crear directorios necesarios para uploads y exports
RUN mkdir -p /app/uploads/attachments /app/uploads/exports && \
    chown -R spring:spring /app

# Cambiar al usuario no-root
USER spring:spring

# Exponer el puerto (Render usa la variable PORT)
EXPOSE 8080

# Variables de entorno por defecto (Render las sobreescribirá)
ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Comando de inicio
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]