# Auth Service – BillMate

Microservicio encargado de la gestión de usuarios y autenticación mediante tokens JWT dentro del sistema BillMate.

---

## 🔐 Descripción

Este módulo se encarga de:

- Registrar nuevos usuarios en la base de datos PostgreSQL
- Autenticar credenciales de login válidas
- Generar y validar tokens JWT
- Controlar el acceso al resto de microservicios protegidos

---

## 🧰 Stack tecnológico

- Java 21 (LTS)
- Spring Boot 3.3.0
- Spring Security + JWT
- PostgreSQL
- Maven

---

## 🔧 Configuración por defecto

El servicio se levanta en el puerto:
```
http://localhost:8081
```

Y utiliza la base de datos PostgreSQL `billmate_auth`.

Puedes ajustar estos valores desde el archivo:  
`src/main/resources/application.yaml`

---

## 🚀 Endpoints disponibles

| Método | Ruta           | Descripción             |
|--------|----------------|-------------------------|
| POST   | `/auth/register` | Registrar nuevo usuario |
| POST   | `/auth/login`    | Autenticar y devolver JWT |

---

## 📋 Ejemplo de payload para login

```json
{
  "email": "admin@mail.com",
  "password": "admin123"
}
```
La respuesta será un JWT en este formato:
```
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6..."
}
```
🔑 Cómo usar el token
Una vez tengas el token, debes incluirlo en el encabezado Authorization en tus peticiones a microservicios protegidos (por ejemplo, billing-service):
```
Authorization: Bearer <tu-token>
```
