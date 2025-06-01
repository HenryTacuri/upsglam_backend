############################################
# Etapa 1: Construcción con Maven + JDK 17 #
############################################
FROM maven:3.8.4-openjdk-17 AS build

# Directorio de trabajo para la compilación
WORKDIR /app

# 1) Copiamos pom.xml y el código fuente completo (incluyendo src/main/resources)
COPY pom.xml .
COPY src ./src

# 2) Ejecutamos mvn package (saltando tests para que sea más rápido)
RUN mvn clean package -DskipTests

##########################################
# Etapa 2: Imagen de producción (runtime) #
##########################################
FROM openjdk:17-oracle

# Creamos el directorio /app dentro del contenedor y establecemos el working dir
WORKDIR /app

# 3) Copiamos el JAR generado en la etapa anterior
#    Ajusta el nombre del JAR según tu archivo final en target/
COPY --from=build /app/target/*.jar app.jar

# 4) Para que tu configuración Java pueda leer
#    new FileInputStream("src/main/resources/serviceAccountKey.json"),
#    necesitamos recrear la misma estructura de carpetas dentro de /app.
RUN mkdir -p /app/src/main/resources

# 5) Copiamos el archivo serviceAccountKey.json a /app/src/main/resources
COPY src/main/resources/serviceAccountKey.json \
     /app/src/main/resources/serviceAccountKey.json

# 6) Copiamos el application.properties (opcional, ya está dentro del JAR,
#    pero no hace daño si lo añades también así)
#    NOTA: si tu código carga application.properties desde el classpath, no es necesario copiarlo.
#COPY src/main/resources/application.properties \
#     /app/src/main/resources/application.properties

# 7) Exponemos el puerto 8080 (Spring Boot)
EXPOSE 8080

# 8) Comando por defecto para ejecutar la aplicación
ENTRYPOINT ["java", "-jar", "app.jar"]
