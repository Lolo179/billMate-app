#  BillMate

**BillMate** es una aplicación de facturación para pequeños negocios, construida con arquitectura de microservicios, desarrollada como proyecto de TFG en el ciclo de DAW.

---

##  Arquitectura general

- **Auth Service**: Registro y autenticación de usuarios con JWT
- **Billing Service**: Gestión de facturas, clientes y productos (en desarrollo)
- **API Gateway**: Entrada central para las peticiones, responsable de validar JWT
- **Frontend**: Angular + Bootstrap (pendiente, sustituible por Thymeleaf si hay poco tiempo)

---

##  Tecnologías utilizadas

- **Java 17**, **Spring Boot 3.1.5**
- **Spring Security + JWT**
- **Spring Cloud Gateway (reactivo)**
- **Spring Data JPA + PostgreSQL**
- **Maven** para gestión de dependencias
- **Trello** para gestión del proyecto
- **Swagger** para documentación (pendiente de integrar)
- **Git + GitHub** para control de versiones

---

##  Autenticación

- El sistema de login devuelve un token JWT
- Las rutas del `billing-service` están protegidas por el `api-gateway`
- Se valida el JWT directamente en el gateway usando un filtro reactivo personalizado
- El token se debe incluir en el header:
