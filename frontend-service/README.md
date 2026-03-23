# Frontend Service – BillMate

Aplicación frontend independiente construida con **React + TypeScript + Vite**. Se comunica con el resto de microservicios exclusivamente a través del **API Gateway** (puerto 8080).

---

## 🏗️ Arquitectura

Sigue una arquitectura hexagonal en el lado cliente:

```
src/
├── app/                 # Router y punto de composición
├── domain/              # Modelos puros (Client, Invoice, User, AuthSession)
├── application/         # Ports y use cases
├── infrastructure/      # Adaptadores HTTP, storage y configuración
│   └── container.ts     # Inyección de dependencias
├── ui/                  # Páginas, layout y componentes React
├── mocks/               # MSW para desarrollo aislado (sin backend)
└── test/                # Setup de Vitest
```

El frontend **solo conoce el API Gateway** mediante `VITE_API_BASE_URL`. Nunca accede directamente a `auth-service`, `billing-service` ni puertos internos.

---

## 🛠️ Stack Tecnológico

- React + TypeScript
- Vite (bundler y dev server)
- Vitest (tests unitarios)
- Testing Library (tests de UI)
- Playwright (tests E2E, directorio `e2e/`)
- MSW (Mock Service Worker — desarrollo sin backend)
- Nginx (servidor en producción vía Docker)

---

## 🔧 Puertos

| Entorno | Puerto | URL |
|---|---|---|
| Desarrollo (Vite dev server) | 5173 | http://localhost:5173 |
| Producción (contenedor Nginx) | 8083 | http://localhost:8083 |

---

## 🚀 Scripts

```bash
npm install           # Instalar dependencias
npm run dev           # Iniciar dev server (http://localhost:5173)
npm run build         # Build de producción
npm run test          # Tests unitarios con Vitest
```

### Con mock backend (sin microservicios)

```bash
VITE_USE_MSW=true npm run dev
```

Con `VITE_USE_MSW=true` la aplicación funciona completamente sin levantar ningún microservicio — MSW intercepta las peticiones HTTP.

### E2E con Playwright

```bash
cd ../e2e
npm install
npm test              # Ejecutar tests Playwright contra http://localhost:5173
```

---

## 📊 CI/CD

### CI — `.github/workflows/frontend-ci.yaml`

- **Trigger**: PR a `main` + `workflow_dispatch`
- **Acciones**:
  - ✅ Tests unitarios con Vitest
  - ✅ Build Vite de producción
  - ✅ Build de imagen Docker (sin push)

### CI E2E — `.github/workflows/e2e-ci.yaml`

- Levanta el entorno completo (DBs + Kafka en Docker, servicios Java como procesos JVM)
- Arranca el frontend con Vite dev server (`VITE_USE_MSW=false`)
- Ejecuta los tests Playwright contra `http://127.0.0.1:5173`

### CD — `.github/workflows/deploy.yaml` (pipeline global)

Activado en push a `main` si CI pasa:
- Construye y publica la imagen `ghcr.io/{owner}/frontend-service:latest` en GHCR
- Despliega a EC2 junto al resto de servicios

---

## 🐳 Docker

Imagen multi-stage con Nginx para servir el build estático:

```bash
docker build -t billmate/frontend-service:latest .
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [API Gateway](../api-gateway/README.md) – Punto de entrada del frontend
- [E2E Tests](../e2e/) – Tests Playwright del flujo completo
