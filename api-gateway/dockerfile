# -------- STAGE 1: Build --------
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# 1️ Copiar solo archivos necesarios para dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw

# 2️ Descargar dependencias (se cachea)
RUN ./mvnw dependency:go-offline

# 3️ Ahora copiar el código fuente
COPY src src

# 4️ Compilar
RUN ./mvnw clean package -DskipTests


# -------- STAGE 2: Runtime --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
