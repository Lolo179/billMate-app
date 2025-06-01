-- CLIENTES
INSERT INTO clients (name, email, nif, address, created_at) VALUES
('Juan Pérez', 'juan@mail.com', '12345678Z', 'Calle Falsa 123', CURRENT_TIMESTAMP),
('Ana Gómez', 'ana@mail.com', '87654321T', 'Avenida Siempre Viva 742', CURRENT_TIMESTAMP),
('Carlos Ruiz', 'carlos@mail.com', '56781234A', 'Calle Luna 45', CURRENT_TIMESTAMP);

-- FACTURAS
INSERT INTO invoices (client_id, date, status, description, total, tax_percentage, created_at) VALUES
(1, CURRENT_DATE, 'DRAFT', 'Primera factura', 200.00, 21.00, CURRENT_TIMESTAMP),
(2, CURRENT_DATE, 'PAID', 'Segunda factura', 150.00, 21.00, CURRENT_TIMESTAMP);

-- LÍNEAS DE FACTURA para las anteriores
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(1, 'Servicio A', 1.00, 200.00, 200.00),
(2, 'Servicio B', 2.00, 75.00, 150.00);
