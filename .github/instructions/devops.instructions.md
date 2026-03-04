---
applyTo: "**/Dockerfile,**/docker-compose*,.github/workflows/**"
---

# Especialista: DevOps – Docker + CI/CD + GitHub Actions

> Estas instrucciones se activan automáticamente al editar Dockerfiles, docker-compose o workflows de GitHub Actions.

---

## Docker

### Dockerfiles (Multi-Stage Build)

Los 3 servicios backend (auth, billing, gateway) siguen el mismo patrón:

```dockerfile
# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY pom.xml mvnw ./
COPY .mvn .mvn
RUN ./mvnw dependency:go-offline -B
COPY src src
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE {puerto}
ENTRYPOINT ["java", "-jar", "app.jar"]
```

| Servicio | Puerto | Imagen base |
|---|---|---|
| Auth Service | 8081 | `eclipse-temurin:21-jre-alpine` |
| Billing Service | 8082 | `eclipse-temurin:21-jre-alpine` |
| API Gateway | 8080 | `eclipse-temurin:21-jre-alpine` |

**Reglas del Dockerfile:**
- Siempre **multi-stage** (builder + runtime)
- Builder: `eclipse-temurin:21-jdk-alpine`
- Runtime: `eclipse-temurin:21-jre-alpine` (más ligero)
- Caché de dependencias: `dependency:go-offline` antes de copiar el código
- Tests **omitidos** en Docker build: `-DskipTests`
- Flags de Maven: `-B` (batch mode, sin logs interactivos)
- `WORKDIR /app` en ambas stages
- Un solo JAR: `*.jar` → `app.jar`

---

### Docker Compose (Desarrollo Local)

Cada servicio tiene su propio `docker-compose.yaml` para la base de datos:

#### Auth Service (`auth-service/docker-compose.yaml`):
```yaml
services:
  auth-postgres:
    image: postgres:16
    ports:
      - "5434:5432"
    environment:
      POSTGRES_DB: auth_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - auth-data:/var/lib/postgresql/data

volumes:
  auth-data:
```

#### Billing Service (`billing-service/docker-compose.yaml`):
```yaml
services:
  billing-postgres:
    image: postgres:16
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: billing_db
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    volumes:
      - billing-data:/var/lib/postgresql/data

volumes:
  billing-data:
```

**Reglas de Docker Compose:**
- PostgreSQL 16 (sin tag alpine en compose)
- Puertos mapeados: 5434 (auth) y 5433 (billing) para evitar conflictos
- Volúmenes nombrados para persistencia
- Variables de entorno directas (no `.env`)
- Spring Boot auto-gestiona el ciclo de vida (`spring-boot-docker-compose`)

---

## CI/CD con GitHub Actions

### CI – Integración Continua (3 workflows)

| Workflow | Archivo | Trigger |
|---|---|---|
| Auth CI | `.github/workflows/auth-ci.yaml` | PR a `main` |
| Billing CI | `.github/workflows/billing-ci.yaml` | PR a `main` |
| Gateway CI | `.github/workflows/api-gateway-ci.yaml` | PR a `main` |

**Estructura estándar de CI:**

```yaml
name: {servicio} CI

on:
  pull_request:
    branches: [main]
    paths:
      - '{servicio}/**'

concurrency:
  group: ${{ github.workflow }}-${{ github.ref }}
  cancel-in-progress: true

jobs:
  build:
    runs-on: ubuntu-latest
    defaults:
      run:
        working-directory: {servicio}

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Build and Test
        run: ./mvnw -B -ntp clean verify
```

**Reglas de CI:**
- `paths` filter: solo se ejecuta si hay cambios en el servicio correspondiente
- Concurrencia: cancela runs anteriores en la misma rama
- JDK 21 Temurin con cache Maven
- Comando: `./mvnw -B -ntp clean verify` (batch mode, no transfer progress)

---

### CD – Despliegue Continuo (3 workflows)

| Workflow | Archivo | Trigger (tag) |
|---|---|---|
| Auth CD | `.github/workflows/auth-cd.yaml` | `auth-v*.*.*` |
| Billing CD | `.github/workflows/billing-cd.yaml` | `billing-v*.*.*` |
| Gateway CD | `.github/workflows/api-gateway-cd.yaml` | `gateway-v*.*.*` |

**Estructura estándar de CD:**

```yaml
name: {servicio} CD

on:
  push:
    tags:
      - '{prefijo}-v*.*.*'

jobs:
  test:
    # Mismo job que CI (build + verify)

  release:
    needs: test
    runs-on: ubuntu-latest
    permissions:
      contents: read
      packages: write

    steps:
      - uses: actions/checkout@v4

      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: 'maven'

      - name: Extract version from tag
        id: version
        run: echo "VERSION=${GITHUB_REF_NAME#*-v}" >> $GITHUB_OUTPUT

      - name: Build JAR
        working-directory: {servicio}
        run: ./mvnw -B -ntp clean package -DskipTests

      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3

      - name: Login to GHCR
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: ./{servicio}
          push: true
          tags: |
            ghcr.io/${{ github.repository_owner }}/{servicio}:${{ steps.version.outputs.VERSION }}
            ghcr.io/${{ github.repository_owner }}/{servicio}:latest
```

**Reglas de CD:**
- Dos jobs: `test` (verificación) → `release` (solo si test pasa)
- Extrae versión del tag: `auth-v1.2.3` → `1.2.3`
- Build JAR con `-DskipTests` (ya se testeó en job anterior)
- Docker Buildx para builds multiplataforma
- Push a **GitHub Container Registry (GHCR)**
- Dos tags por imagen: versión exacta + `latest`
- Permissions: `packages: write` para push a GHCR

---

## Estrategia de Releases

### Versionado Semántico por Servicio:

```bash
# Formato de tags
auth-v{MAJOR}.{MINOR}.{PATCH}       # Ej: auth-v1.0.0
billing-v{MAJOR}.{MINOR}.{PATCH}    # Ej: billing-v2.1.0
gateway-v{MAJOR}.{MINOR}.{PATCH}    # Ej: gateway-v1.3.2
```

### Flujo de Release:

```bash
# 1. Asegurar que main está actualizado
git checkout main && git pull

# 2. Crear tag de versión
git tag billing-v1.2.0

# 3. Push del tag (dispara CD)
git push origin billing-v1.2.0
```

---

## Imágenes Docker publicadas

```
ghcr.io/{owner}/auth-service:{version}
ghcr.io/{owner}/auth-service:latest
ghcr.io/{owner}/billing-service:{version}
ghcr.io/{owner}/billing-service:latest
ghcr.io/{owner}/api-gateway:{version}
ghcr.io/{owner}/api-gateway:latest
```

---

## Checklist para Cambios DevOps

### Nuevo servicio:
1. [ ] Crear `Dockerfile` (multi-stage, temurin:21)
2. [ ] Crear `docker-compose.yaml` si necesita DB
3. [ ] Crear workflow CI (`.github/workflows/{servicio}-ci.yaml`)
4. [ ] Crear workflow CD (`.github/workflows/{servicio}-cd.yaml`)
5. [ ] Definir patrón de tag en CD trigger

### Cambio en Dockerfile:
1. [ ] Verificar que el puerto `EXPOSE` coincide con `application.yaml`
2. [ ] Verificar que no se copian archivos innecesarios (`.dockerignore`)
3. [ ] Build local de prueba: `docker build -t test .`

### Cambio en workflow:
1. [ ] Verificar `paths` filter en CI
2. [ ] Verificar `tags` pattern en CD
3. [ ] Verificar `working-directory` correcto
4. [ ] Verificar que permissions incluyen `packages: write` en CD
