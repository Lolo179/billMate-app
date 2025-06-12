-- Crear base de datos principal para todos los microservicios
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT FROM pg_database WHERE datname = 'billmate_app'
    ) THEN
        CREATE DATABASE billmate_app;
    END IF;
END
$$;

