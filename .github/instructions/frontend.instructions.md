---
applyTo: "frontend-service/**"
---

# Especialista: Frontend Service – Thymeleaf SSR + Vanilla JS

> Estas instrucciones se activan automáticamente al editar cualquier archivo dentro de `frontend-service/`.

---

## Arquitectura

El frontend es una aplicación **Spring Boot + Thymeleaf** con rendering del lado del servidor (SSR). La lógica del cliente se implementa con **JavaScript vanilla** (sin frameworks JS como React, Angular o Vue).

```
com.billMate.frontend
├── FrontendServiceApplication.java        # @SpringBootApplication(scanBasePackages = "com.billMate")
├── WebConfig.java                         # Configuración de recursos estáticos (/js/**)
└── controller/auth/
    └── AuthController.java                # @Controller — sirve vistas Thymeleaf
```

---

## Estructura de Archivos

```
src/main/
├── java/com/billMate/frontend/
│   ├── FrontendServiceApplication.java
│   ├── WebConfig.java
│   └── controller/auth/AuthController.java
└── resources/
    ├── application.yaml
    ├── templates/                           # Vistas Thymeleaf (.html)
    │   ├── auth/
    │   │   ├── login.html
    │   │   └── usuarios.html
    │   ├── dashboard.html
    │   ├── clientes.html
    │   ├── cliente-nuevo.html
    │   ├── facturas.html
    │   ├── facturas-cliente.html
    │   ├── factura-nueva.html
    │   ├── factura-editar.html
    │   └── fragments/                      # Fragmentos reutilizables
    │       ├── sidebar.html
    │       ├── navbar.html
    │       ├── header.html
    │       └── footer.html
    └── static/
        ├── js/                             # JavaScript vanilla
        │   ├── common.js                   # Funciones compartidas (logout, token, navbar)
        │   ├── auth/login.js
        │   ├── auth/usuarios.js
        │   ├── dashboard.js
        │   ├── clientes.js
        │   ├── cliente-nuevo.js
        │   ├── facturas.js
        │   ├── facturas-cliente.js
        │   ├── factura-nueva.js
        │   └── factura-editar.js
        └── css/                            # Estilos CSS
```

---

## Rutas y Vistas

```java
@Controller
public class AuthController {

    @GetMapping("/login")                           → "auth/login"
    @GetMapping("/clientes")                        → "clientes"
    @GetMapping("/facturas")                        → "facturas"
    @GetMapping("/dashboard")                       → "dashboard"
    @GetMapping("/clientes/{clientId}/facturas")     → "facturas-cliente" (model: clientId)
    @GetMapping("/facturas/{invoiceId}/editar")      → "factura-editar" (model: invoiceId)
    @GetMapping("/facturas/nueva/{clientId}")         → "factura-nueva" (model: clientId)
    @GetMapping("/usuarios")                         → "auth/usuarios"
    @GetMapping("/clientes/nuevo")                   → "cliente-nuevo"
}
```

- El controller usa `@Controller` (no `@RestController`) porque sirve vistas Thymeleaf
- Los IDs dinámicos se pasan al modelo con `model.addAttribute()`

---

## Patrón de JavaScript

### Comunicación con la API

Todas las llamadas API van a través del **API Gateway** (puerto 8080):

```javascript
const BASE_URL = "http://localhost:8080";

// Llamada autenticada al billing-service (vía gateway)
const response = await fetch(`${BASE_URL}/billing/clients`, {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem("jwt")}`,
        'Content-Type': 'application/json'
    }
});
```

> **Ruta en gateway:** `/billing/clients` → el gateway aplica `StripPrefix=1` → llega como `/clients` al billing-service.

### Gestión del Token JWT

```javascript
// Almacenamiento: localStorage
localStorage.setItem("jwt", token);
localStorage.getItem("jwt");
localStorage.removeItem("jwt");

// Decodificar payload del JWT
const payload = JSON.parse(atob(token.split(".")[1]));
const roles = payload.roles || [];
const email = payload.sub;
```

### Logout

```javascript
function logout() {
    localStorage.removeItem("jwt");
    window.location.href = "/login";
}
```

### Detección de Rol

```javascript
document.addEventListener("DOMContentLoaded", () => {
    const token = localStorage.getItem("jwt");
    if (!token) return;

    const payload = JSON.parse(atob(token.split(".")[1]));
    const roles = payload.roles || [];

    // Mostrar elementos solo para ADMIN
    if (roles.includes("ADMIN")) {
        document.getElementById("gestion-usuarios-menu").style.display = "block";
    }
});
```

### Funciones Compartidas (common.js)

```javascript
function getUserFromToken() {
    const token = localStorage.getItem("jwt");
    if (!token) return null;
    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        return { username: payload.sub, roles: payload.roles || [] };
    } catch (e) {
        console.error("Token mal formado", e);
        return null;
    }
}

function mostrarInfoUsuarioNavbar() {
    const user = getUserFromToken();
    if (!user) return;
    const span = document.getElementById("user-info");
    if (span) span.textContent = `👤 ${user.username} (${user.roles[0] || "N/A"})`;
}
```

---

## Configuración

```yaml
server:
  port: 8083

spring:
  application:
    name: frontend-service
```

---

## Reglas Importantes

1. **NO usar frameworks JS** — solo JavaScript vanilla (fetch API, DOM manipulation)
2. **Token JWT siempre en `localStorage`** — se envía en header `Authorization: Bearer {token}`
3. **Todas las llamadas API van al gateway** (`localhost:8080`), nunca directamente a los microservicios
4. **Prefijo `/billing/`** obligatorio para endpoints del billing-service vía gateway
5. **Thymeleaf fragments** para reutilizar sidebar, navbar, header, footer
6. **`common.js`** debe cargarse en todas las páginas (gestiona logout, info de usuario, detección de roles)
7. **Las rutas del frontend están registradas como públicas** en el gateway (no requieren JWT para cargar la vista, pero sí para llamar a la API)

---

## Checklist para Nueva Vista

1. [ ] Crear template Thymeleaf en `templates/`
2. [ ] Incluir fragments (sidebar, navbar, header, footer)
3. [ ] Añadir ruta `@GetMapping` en `AuthController`
4. [ ] Crear archivo JS en `static/js/`
5. [ ] Incluir `common.js` para funciones compartidas
6. [ ] Registrar la ruta como pública en el gateway (`AuthenticationFilter`)
7. [ ] Si necesita enlace en sidebar, actualizar fragment `sidebar.html`
