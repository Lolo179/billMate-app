# Instrucciones de Instalación de Base de Datos

## Requisitos Previos

- PostgreSQL 12+ instalado y corriendo
- Usuario con permisos de creación de bases de datos (por defecto: `admin` con password `admin123`)
- La carpeta `scripts/` accesible desde tu ruta actual

---

## ⚡ Instalación Rápida

Desde el directorio raíz del proyecto:

```bash
# Windows
psql -U postgres -f scripts/create-tables.sql

# Linux/Mac
psql -U postgres -f scripts/create-tables.sql
```

Este comando crea las dos bases de datos y todas las tablas necesarias de una vez.

---

## Pasos de Instalación Manual

### 1. Crear las Bases de Datos

Conecta a PostgreSQL como superusuario:

```bash
psql -U postgres
```

Ejecuta:

```sql
CREATE DATABASE billmate_auth;
CREATE DATABASE billmate_billing;
```

Sal de psql:

```
\q
```

### 2. Ejecutar el Script de Creación de Tablas

#### Opción A: Desde línea de comandos (Recomendado)

```bash
psql -U postgres -f scripts/create-tables.sql
```

#### Opción B: Desde psql interactivo

```bash
psql -U postgres
```

Dentro de psql:

```sql
\i scripts/create-tables.sql
```

---

## 3. Verificar las Tablas Creadas

### Auth DB

```bash
psql -U admin -d billmate_auth
```

```sql
\dt                  -- Lista todas las tablas
\d app_users         -- Describe tabla app_users
\d user_roles        -- Describe tabla user_roles
```

Deberías ver:
- `app_users`
- `user_roles`

### Billing DB

```bash
psql -U admin -d billmate_billing
```

```sql
\dt                  -- Lista todas las tablas
\d clients           -- Describe tabla clients
\d invoices          -- Describe tabla invoices
\d invoice_lines     -- Describe tabla invoice_lines
```

Deberías ver:
- `clients`
- `invoices`
- `invoice_lines`

---

## 🔐 Variables de Entorno (Opcional)

Si quieres usar credenciales diferentes, configura estas variables de entorno antes de iniciar los servicios:

```bash
# Para auth-service
export DB_URL=jdbc:postgresql://localhost:5432/billmate_auth
export DB_USER=admin
export DB_PASSWORD=admin123

# Para billing-service
export DB_URL=jdbc:postgresql://localhost:5432/billmate_billing
export DB_USER=admin
export DB_PASSWORD=admin123
```

---

## ⚙️ Configuración Hibernate

Los servicios están configurados con `ddl-auto: validate`, lo que significa:

- ✅ Hibernate **VALIDA** que las tablas existen y coinciden con las entidades
- ❌ Hibernate **NO CREA** ni **MODIFICA** tablas automáticamente
- ✅ La estructura de la base de datos está bajo control manual/versionado

---

## 🐳 Docker Compose

Si usas Docker Compose, las bases de datos se crean automáticamente:

```bash
docker-compose -f auth-service/docker-compose.yaml up -d
docker-compose -f billing-service/docker-compose.yaml up -d
```

---

## 🔧 Solución de Problemas

### Error: "no existe la relación"

- Verifica que ejecutaste el script `create-tables.sql` en la base de datos correcta
- Verifica que las tablas existen: `\dt` en psql

### Error: "schema validation failed"

- Significa que la estructura de las tablas no coincide con las entidades JPA
- Re-ejecuta el script `create-tables.sql`
- Asegúrate de estar usando las credenciales correctas

### Error: "database does not exist"

- Crea las bases de datos manualmente:

```sql
CREATE DATABASE billmate_auth;
CREATE DATABASE billmate_billing;
```

### Error: "FATAL: role 'admin' does not exist"

- Crea el usuario admin:

```bash
psql -U postgres -c "CREATE USER admin WITH PASSWORD 'admin123';"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE billmate_auth TO admin;"
psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE billmate_billing TO admin;"
```

---

## 📚 Referencias

- [BillMate Principal README](../README.md)
- [Auth Service README](../auth-service/README.md)
- [Billing Service README](../billing-service/README.md)
