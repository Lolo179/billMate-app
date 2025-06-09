## ⚙️ Cómo ejecutar

### Requisitos

- JDK 17 o superior
- Maven 3.8+
- Servicios `auth-service` y `billing-service` en ejecución

### Instrucciones

```bash
mvn spring-boot:run
```
### Acceso
```http://localhost:8081/login```

🔐 Seguridad
La autenticación es vía JWT. El token se guarda en localStorage y se envía en cada petición a /billing/** y /auth/**.
