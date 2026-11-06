# -------- Etapa 1: compilar --------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /build

# Primero solo el pom: Docker guarda esta capa y no vuelve a bajar las
# dependencias mientras el pom no cambie (los rebuilds quedan rapidos)
COPY pom.xml mvnw ./
COPY .mvn .mvn

# Por si git en Windows dejo el mvnw con saltos de linea CRLF (en Linux no corre)
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw
RUN ./mvnw -B dependency:go-offline

COPY src src

# Sin tests: los de integracion necesitan Docker y dentro de esta build no hay.
# Los tests se corren antes, en nuestra maquina o en CI
RUN ./mvnw -B package -DskipTests

# -------- Etapa 2: imagen final --------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Usuario sin privilegios: si alguien vulnera la app, no es root dentro del contenedor
RUN useradd --system --no-create-home appuser
USER appuser

COPY --from=build /build/target/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# MaxRAMPercentage: la JVM usa hasta el 75% de la RAM que le asigne el contenedor
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]