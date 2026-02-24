# Auth Service – BillMate

Microservicio encargado de la gestión de usuarios y autenticación mediante tokens JWT dentro del sistema BillMate.

---

## 🔐 Descripción

Este módulo se encarga de:

- Registrar nuevos usuarios en PostgreSQL
- Autenticar credenciales de login válidas
- Generar y validar tokens JWT
- Controlar el acceso al resto de microservicios protegidos

---

## 🧰 Stack Tecnológico

- Java 21 (LTS)
- Spring Boot 3.3.0
- Spring Security + JWT
- PostgreSQL
- Maven

---

## 🔧 Configuración por Defecto

El servicio se levanta en el puerto:

```
http://localhost:8081
```

Y utiliza la base de datos PostgreSQL `billmate_auth`. Puedes ajustar estos valores desde el archivo:

```
src/main/resources/application.yaml
```

---

## 🚀 Endpoints Disponibles

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/auth/register` | Registrar nuevo usuario |
| POST | `/auth/login` | Autenticar y devolver JWT |

---

## 📋 Ejemplo de Payload para Login

```json
{
  "email": "admin@mail.com",
  "password": "admin123"
}
```

La respuesta será un JWT en este formato:

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

## 🔑 Cómo Usar el Token

Una vez tengas el token, debes incluirlo en el encabezado `Authorization` en tus peticiones a microservicios protegidos:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

---

## 📊 CI/CD

Este servicio dispone de un workflow automático en GitHub Actions:

- **Archivo**: `.github/workflows/auth-ci.yaml`
- **Trigger**: 
  - PR a rama `develop`
  - Push a rama `main`
- **Acciones**:
  - ✅ Ejecución de tests (`mvn clean verify`)
  - ✅ Build con Java 21 y cache Maven
  - ✅ Construcción de imagen Docker en push a `main`

---

## 🧪 Testing

Para ejecutar los tests del servicio:

```bash
cd auth-service
mvn clean verify
```

---

## 🐳 Docker

Para construir la imagen Docker:

```bash
docker build -t billmate/auth-service:latest .
```

Para ejecutar con docker-compose:

```bash
docker-compose up -d
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [Database Setup](../scripts/README-DATABASE.md)
- [API Gateway](../api-gateway/README.md)
