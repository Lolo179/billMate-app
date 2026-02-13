-- =============================================================================
-- SCRIPT DE CREACIÓN DE TABLAS PARA BILLMATE
-- =============================================================================
-- Este script crea todas las tablas necesarias para los microservicios
-- auth-service y billing-service de forma permanente
-- =============================================================================
CREATE TABLE IF NOT EXISTS auth_db;
CREATE TABLE IF NOT EXISTS billing_db;
-- =============================================================================
-- AUTH SERVICE - Base de datos: auth_db
-- =============================================================================

-- Conectar a la base de datos auth_db
-- \c auth_db;

-- Tabla: app_users
CREATE TABLE IF NOT EXISTS app_users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- Tabla: user_roles (relación many-to-many con roles)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role VARCHAR(255) NOT NULL,
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) 
        REFERENCES app_users(id) ON DELETE CASCADE
);

-- Índices para auth_db
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);
CREATE INDEX IF NOT EXISTS idx_app_users_email ON app_users(email);
CREATE INDEX IF NOT EXISTS idx_app_users_username ON app_users(username);


-- =============================================================================
-- BILLING SERVICE - Base de datos: billing_db
-- =============================================================================

-- Conectar a la base de datos billing_db
-- \c billing_db;

-- Tabla: clients
CREATE TABLE IF NOT EXISTS clients (
    client_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(255),
    nif VARCHAR(255),
    address VARCHAR(255),
    -- ELIMINAR: user_id BIGINT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- Tabla: invoices
CREATE TABLE IF NOT EXISTS invoices (
    invoice_id BIGSERIAL PRIMARY KEY,
    client_id BIGINT NOT NULL,
    date DATE NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    description VARCHAR(255),
    total NUMERIC(10, 2) NOT NULL DEFAULT 0.00,
    tax_percentage NUMERIC(5, 2) NOT NULL DEFAULT 21.00,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoices_client FOREIGN KEY (client_id) 
        REFERENCES clients(client_id) ON DELETE CASCADE
);

-- Tabla: invoice_lines
CREATE TABLE IF NOT EXISTS invoice_lines (
    id BIGSERIAL PRIMARY KEY,
    invoice_id BIGINT NOT NULL,
    description VARCHAR(100) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    total NUMERIC(10, 2) NOT NULL,
    CONSTRAINT fk_invoice_lines_invoice FOREIGN KEY (invoice_id) 
        REFERENCES invoices(invoice_id) ON DELETE CASCADE
);

-- Índices para billing_db
CREATE INDEX IF NOT EXISTS idx_clients_email ON clients(email);
CREATE INDEX IF NOT EXISTS idx_invoices_client_id ON invoices(client_id);
CREATE INDEX IF NOT EXISTS idx_invoices_status ON invoices(status);
CREATE INDEX IF NOT EXISTS idx_invoices_date ON invoices(date);
CREATE INDEX IF NOT EXISTS idx_invoice_lines_invoice_id ON invoice_lines(invoice_id);


-- =============================================================================
-- VERIFICACIÓN
-- =============================================================================
-- Ejecuta estos comandos para verificar que las tablas se crearon correctamente:
--
-- Para auth_db:
-- \c auth_db;
-- \dt
-- \d app_users
-- \d user_roles
--
-- Para billing_db:
-- \c billing_db;
-- \dt
-- \d clients
-- \d invoices
-- \d invoice_lines
-- =============================================================================
