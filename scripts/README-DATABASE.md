# Instrucciones de Instalación de Base de Datos

## Requisitos Previos
- PostgreSQL instalado y corriendo
- Usuario con permisos de creación de bases de datos (por defecto: `admin` con password `admin123`)

## Pasos de Instalación

### 1. Crear las Bases de Datos

Conecta a PostgreSQL como superusuario:

```bash
psql -U postgres
```

Ejecuta:

```sql
CREATE DATABASE auth_db;
CREATE DATABASE billing_db;
```

Sal de psql:
```
\q
```

### 2. Ejecutar el Script de Creación de Tablas

#### Opción A: Desde línea de comandos (Recomendado)

```bash
# Para auth_db
psql -U admin -d auth_db -f scripts/create-tables.sql

# Para billing_db  
psql -U admin -d billing_db -f scripts/create-tables.sql
```

#### Opción B: Desde psql interactivo

```bash
psql -U admin -d auth_db
```

Dentro de psql:
```sql
\i scripts/create-tables.sql
```

Repite para `billing_db`.

### 3. Verificar las Tablas Creadas

#### Auth DB:
```bash
psql -U admin -d auth_db
```

```sql
\dt                  -- Lista todas las tablas
\d app_users        -- Describe tabla app_users
\d user_roles       -- Describe tabla user_roles
```

Deberías ver:
- `app_users`
- `user_roles`

#### Billing DB:
```bash
psql -U admin -d billing_db
```

```sql
\dt                  -- Lista todas las tablas
\d clients          -- Describe tabla clients
\d invoices         -- Describe tabla invoices
\d invoice_lines    -- Describe tabla invoice_lines
```

Deberías ver:
- `clients`
- `invoices`
- `invoice_lines`

## Variables de Entorno (Opcional)

Si quieres usar credenciales diferentes, configura estas variables de entorno antes de iniciar los servicios:

```bash
# Para auth-service
export DB_URL=jdbc:postgresql://localhost:5432/auth_db
export DB_USER=tu_usuario
export DB_PASSWORD=tu_password

# Para billing-service (usa las mismas variables)
```

## Configuración Hibernate

Los servicios están configurados con `ddl-auto: validate`, lo que significa:
- ✅ Hibernate **VALIDA** que las tablas existen y coinciden con las entidades
- ❌ Hibernate **NO CREA** ni **MODIFICA** tablas automáticamente
- ✅ La estructura de la base de datos está bajo control manual/versionado

## Solución de Problemas

### Error: "no existe la relación"
- Verifica que ejecutaste el script `create-tables.sql` en la base de datos correcta
- Verifica que las tablas existen: `\dt` en psql

### Error: "schema validation failed"
- Significa que la estructura de las tablas no coincide con las entidades JPA
- Re-ejecuta el script `create-tables.sql`

### Error: "database does not exist"
- Crea las bases de datos manualmente: `CREATE DATABASE auth_db;` y `CREATE DATABASE billing_db;`
