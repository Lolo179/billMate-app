# Frontend Service – Thymeleaf SSR + Vanilla JS

## Arquitectura

Aplicación **Spring Boot + Thymeleaf** con rendering del lado del servidor (SSR). Lógica del cliente con **JavaScript vanilla** (sin React, Angular ni Vue).

```
com.billMate.frontend
├── FrontendServiceApplication.java    # @SpringBootApplication
├── WebConfig.java                     # Configuración de recursos estáticos
└── controller/auth/AuthController.java # @Controller — sirve vistas Thymeleaf
```

## Estructura de Recursos

```
src/main/resources/
├── templates/                    # Vistas Thymeleaf (.html)
│   ├── auth/login.html, usuarios.html
│   ├── dashboard.html, clientes.html, cliente-nuevo.html
│   ├── facturas.html, facturas-cliente.html, factura-nueva.html, factura-editar.html
│   └── fragments/               # sidebar, navbar, header, footer
└── static/
    ├── js/                      # JavaScript vanilla
    │   ├── common.js            # Funciones compartidas (logout, token, navbar)
    │   ├── auth/login.js, auth/usuarios.js
    │   ├── dashboard.js, clientes.js, cliente-nuevo.js
    │   └── facturas.js, facturas-cliente.js, factura-nueva.js, factura-editar.js
    └── css/
```

## Rutas

El controller usa `@Controller` (no `@RestController`) porque sirve vistas Thymeleaf:

| GET | Vista |
|---|---|
| `/login` | `auth/login` |
| `/dashboard` | `dashboard` |
| `/clientes` | `clientes` |
| `/clientes/nuevo` | `cliente-nuevo` |
| `/clientes/{clientId}/facturas` | `facturas-cliente` |
| `/facturas` | `facturas` |
| `/facturas/nueva/{clientId}` | `factura-nueva` |
| `/facturas/{invoiceId}/editar` | `factura-editar` |
| `/usuarios` | `auth/usuarios` |

Los IDs dinámicos se pasan al modelo con `model.addAttribute()`.

## Comunicación con la API

Todas las llamadas van al **API Gateway** (puerto 8080):

```javascript
const BASE_URL = "http://localhost:8080";

const response = await fetch(`${BASE_URL}/billing/clients`, {
    headers: {
        'Authorization': `Bearer ${localStorage.getItem("jwt")}`,
        'Content-Type': 'application/json'
    }
});
```

> Ruta en gateway: `/billing/clients` → `StripPrefix=1` → llega como `/clients` al billing-service.

## Gestión del Token JWT

```javascript
// Almacenamiento
localStorage.setItem("jwt", token);
localStorage.getItem("jwt");
localStorage.removeItem("jwt");

// Decodificar payload
const payload = JSON.parse(atob(token.split(".")[1]));
const roles = payload.roles || [];
const email = payload.sub;
```

## Detección de Rol

```javascript
const roles = payload.roles || [];
if (roles.includes("ADMIN")) {
    document.getElementById("gestion-usuarios-menu").style.display = "block";
}
```

## Reglas de Código

- `@Controller` (nunca `@RestController`) — sirve vistas, no JSON
- JavaScript **vanilla** — sin frameworks JS, sin jQuery
- Todas las llamadas API pasan por el gateway (`localhost:8080`)
- JWT almacenado en `localStorage`
- `common.js` centraliza funciones compartidas (logout, token check, navbar)
- Thymeleaf fragments para componentes reutilizables (sidebar, navbar, header, footer)
- Puerto: **8083**
