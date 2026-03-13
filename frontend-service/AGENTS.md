# Frontend Service - React + TypeScript

## Arquitectura

Aplicacion frontend independiente basada en React + TypeScript + Vite. No usa Spring ni SSR. El objetivo es desacoplar por completo la UI de los microservicios internos.

```
src/
|- app/                 # Router y punto de composicion
|- domain/              # Modelos puros (Client, Invoice, User, AuthSession)
|- application/         # Ports y use cases
|- infrastructure/      # HTTP, config, storage y adapters concretos
|- ui/                  # Paginas, layout y componentes React
|- mocks/               # MSW para desarrollo aislado
|- test/                # Setup de Vitest
```

## Regla principal

El frontend solo conoce el API Gateway mediante `VITE_API_BASE_URL`. Nunca debe conocer `auth-service`, `billing-service`, puertos internos ni nombres de contenedor.

## Puertos frontend

- `AuthApiPort`
- `BillingApiPort`
- `TokenStoragePort`

Los casos de uso dependen de estos puertos. React consume casos de uso o adapters inyectados desde `infrastructure/container.ts`.

## Testing

- Unit tests: `Vitest`
- UI tests: `Testing Library`
- E2E: `Playwright`
- Mock backend: `MSW`

Con `VITE_USE_MSW=true` la aplicacion puede ejecutarse sin levantar ningun microservicio.

## Scripts

```bash
npm install
npm run dev
npm run build
npm run test
cd ../e2e && npm test
```

## Configuracion

Variables en `.env`:

```bash
VITE_API_BASE_URL=http://localhost:8080
VITE_USE_MSW=false
```

## Reglas de codigo

- TypeScript estricto siempre
- Componentes pequenos y sin logica HTTP embebida
- Toda llamada remota pasa por `infrastructure/http/ApiClient.ts`
- Validacion de inputs en use cases con `zod`
- No usar acceso directo a `fetch` desde `ui/` salvo justificacion excepcional
- Mantener lenguaje de dominio en ingles dentro del codigo
