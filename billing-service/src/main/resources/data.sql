-- CLIENTES
INSERT INTO clients (name, email, nif, address, phone, created_at) VALUES
('Cliente 01', 'cliente01@mail.com', '00000001A', 'Calle 1', '+34 600 000 001', CURRENT_TIMESTAMP),
('Cliente 02', 'cliente02@mail.com', '00000002A', 'Calle 2', '+34 600 000 002', CURRENT_TIMESTAMP),
('Cliente 03', 'cliente03@mail.com', '00000003A', 'Calle 3', '+34 600 000 003', CURRENT_TIMESTAMP),
('Cliente 04', 'cliente04@mail.com', '00000004A', 'Calle 4', '+34 600 000 004', CURRENT_TIMESTAMP),
('Cliente 05', 'cliente05@mail.com', '00000005A', 'Calle 5', '+34 600 000 005', CURRENT_TIMESTAMP),
('Cliente 06', 'cliente06@mail.com', '00000006A', 'Calle 6', '+34 600 000 006', CURRENT_TIMESTAMP),
('Cliente 07', 'cliente07@mail.com', '00000007A', 'Calle 7', '+34 600 000 007', CURRENT_TIMESTAMP),
('Cliente 08', 'cliente08@mail.com', '00000008A', 'Calle 8', '+34 600 000 008', CURRENT_TIMESTAMP),
('Cliente 09', 'cliente09@mail.com', '00000009A', 'Calle 9', '+34 600 000 009', CURRENT_TIMESTAMP),
('Cliente 10', 'cliente10@mail.com', '00000010A', 'Calle 10', '+34 600 000 010', CURRENT_TIMESTAMP),
('Cliente 11', 'cliente11@mail.com', '00000011A', 'Calle 11', '+34 600 000 011', CURRENT_TIMESTAMP),
('Cliente 12', 'cliente12@mail.com', '00000012A', 'Calle 12', '+34 600 000 012', CURRENT_TIMESTAMP),
('Cliente 13', 'cliente13@mail.com', '00000013A', 'Calle 13', '+34 600 000 013', CURRENT_TIMESTAMP),
('Cliente 14', 'cliente14@mail.com', '00000014A', 'Calle 14', '+34 600 000 014', CURRENT_TIMESTAMP),
('Cliente 15', 'cliente15@mail.com', '00000015A', 'Calle 15', '+34 600 000 015', CURRENT_TIMESTAMP),
('Cliente 16', 'cliente16@mail.com', '00000016A', 'Calle 16', '+34 600 000 016', CURRENT_TIMESTAMP),
('Cliente 17', 'cliente17@mail.com', '00000017A', 'Calle 17', '+34 600 000 017', CURRENT_TIMESTAMP),
('Cliente 18', 'cliente18@mail.com', '00000018A', 'Calle 18', '+34 600 000 018', CURRENT_TIMESTAMP),
('Cliente 19', 'cliente19@mail.com', '00000019A', 'Calle 19', '+34 600 000 019', CURRENT_TIMESTAMP),
('Cliente 20', 'cliente20@mail.com', '00000020A', 'Calle 20', '+34 600 000 020', CURRENT_TIMESTAMP),
('Cliente 21', 'cliente21@mail.com', '00000021A', 'Calle 21', '+34 600 000 021', CURRENT_TIMESTAMP),
('Cliente 22', 'cliente22@mail.com', '00000022A', 'Calle 22', '+34 600 000 022', CURRENT_TIMESTAMP),
('Cliente 23', 'cliente23@mail.com', '00000023A', 'Calle 23', '+34 600 000 023', CURRENT_TIMESTAMP),
('Cliente 24', 'cliente24@mail.com', '00000024A', 'Calle 24', '+34 600 000 024', CURRENT_TIMESTAMP),
('Cliente 25', 'cliente25@mail.com', '00000025A', 'Calle 25', '+34 600 000 025', CURRENT_TIMESTAMP);


-- FACTURAS (75 facturas aprox., 3 por cliente)
-- Generadas de forma predecible para testing
-- Los IDs de cliente van del 1 al 25
INSERT INTO invoices (client_id, date, status, description, total, tax_percentage, created_at) VALUES
-- Cliente 1
(1, CURRENT_DATE, 'DRAFT', 'Servicio 1', 100.00, 21.00, CURRENT_TIMESTAMP),
(1, CURRENT_DATE, 'SENT', 'Servicio 2', 120.00, 21.00, CURRENT_TIMESTAMP),
(1, CURRENT_DATE, 'PAID', 'Servicio 3', 150.00, 21.00, CURRENT_TIMESTAMP),

-- Cliente 2
(2, CURRENT_DATE, 'PAID', 'Servicio web', 200.00, 21.00, CURRENT_TIMESTAMP),
(2, CURRENT_DATE, 'DRAFT', 'Consultoría', 130.00, 21.00, CURRENT_TIMESTAMP),

-- Cliente 3
(3, CURRENT_DATE, 'SENT', 'Auditoría', 175.00, 21.00, CURRENT_TIMESTAMP),

-- ... Repetir patrón con variedad
(4, CURRENT_DATE, 'PAID', 'Backup mensual', 80.00, 21.00, CURRENT_TIMESTAMP),
(4, CURRENT_DATE, 'DRAFT', 'Migración datos', 300.00, 21.00, CURRENT_TIMESTAMP),

(5, CURRENT_DATE, 'SENT', 'Diseño gráfico', 210.00, 21.00, CURRENT_TIMESTAMP),
(5, CURRENT_DATE, 'PAID', 'Landing page', 400.00, 21.00, CURRENT_TIMESTAMP),

(6, CURRENT_DATE, 'DRAFT', 'Web corporativa', 600.00, 21.00, CURRENT_TIMESTAMP),
(6, CURRENT_DATE, 'SENT', 'SEO mensual', 130.00, 21.00, CURRENT_TIMESTAMP),

(7, CURRENT_DATE, 'PAID', 'Soporte técnico', 95.00, 21.00, CURRENT_TIMESTAMP),
(7, CURRENT_DATE, 'DRAFT', 'Consultoría legal', 220.00, 21.00, CURRENT_TIMESTAMP),

(8, CURRENT_DATE, 'SENT', 'Diseño catálogo', 310.00, 21.00, CURRENT_TIMESTAMP),
(9, CURRENT_DATE, 'PAID', 'Asesoría UX', 115.00, 21.00, CURRENT_TIMESTAMP),
(9, CURRENT_DATE, 'DRAFT', 'Manual usuario', 99.00, 21.00, CURRENT_TIMESTAMP),

(10, CURRENT_DATE, 'PAID', 'App móvil', 780.00, 21.00, CURRENT_TIMESTAMP),

(11, CURRENT_DATE, 'SENT', 'Plan marketing', 250.00, 21.00, CURRENT_TIMESTAMP),
(11, CURRENT_DATE, 'DRAFT', 'Auditoría SEO', 140.00, 21.00, CURRENT_TIMESTAMP),

(12, CURRENT_DATE, 'PAID', 'Campaña Google Ads', 500.00, 21.00, CURRENT_TIMESTAMP),
(12, CURRENT_DATE, 'SENT', 'Brandbook', 320.00, 21.00, CURRENT_TIMESTAMP),

(13, CURRENT_DATE, 'DRAFT', 'Diseño logo', 80.00, 21.00, CURRENT_TIMESTAMP),
(14, CURRENT_DATE, 'PAID', 'Web tienda online', 890.00, 21.00, CURRENT_TIMESTAMP),
(14, CURRENT_DATE, 'DRAFT', 'Soporte mensual', 115.00, 21.00, CURRENT_TIMESTAMP),

(15, CURRENT_DATE, 'SENT', 'Rediseño web', 450.00, 21.00, CURRENT_TIMESTAMP),
(16, CURRENT_DATE, 'PAID', 'Consultoría técnica', 275.00, 21.00, CURRENT_TIMESTAMP),

(17, CURRENT_DATE, 'DRAFT', 'Manuales operativos', 120.00, 21.00, CURRENT_TIMESTAMP),
(17, CURRENT_DATE, 'SENT', 'Auditoría de procesos', 330.00, 21.00, CURRENT_TIMESTAMP),

(18, CURRENT_DATE, 'PAID', 'Formación equipo', 600.00, 21.00, CURRENT_TIMESTAMP),
(19, CURRENT_DATE, 'DRAFT', 'Servicio DNS', 70.00, 21.00, CURRENT_TIMESTAMP),

(20, CURRENT_DATE, 'SENT', 'Optimización servidores', 360.00, 21.00, CURRENT_TIMESTAMP),
(21, CURRENT_DATE, 'PAID', 'Consultoría ERP', 420.00, 21.00, CURRENT_TIMESTAMP),

(22, CURRENT_DATE, 'DRAFT', 'Mantenimiento SAP', 550.00, 21.00, CURRENT_TIMESTAMP),
(23, CURRENT_DATE, 'SENT', 'Auditoría financiera', 300.00, 21.00, CURRENT_TIMESTAMP),

(24, CURRENT_DATE, 'PAID', 'Integración API', 290.00, 21.00, CURRENT_TIMESTAMP),
(25, CURRENT_DATE, 'DRAFT', 'Diseño interfaz', 190.00, 21.00, CURRENT_TIMESTAMP);

-- Líneas para Factura 1 (100 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(1, 'Servicio básico', 1, 100.00, 100.00);

-- Líneas para Factura 2 (120 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(2, 'Servicio extendido', 2, 60.00, 120.00);

-- Líneas para Factura 3 (150 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(3, 'Mantenimiento web', 3, 50.00, 150.00);

-- Líneas para Factura 4 (200 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(4, 'Desarrollo web', 2, 100.00, 200.00);

-- Líneas para Factura 5 (130 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(5, 'Consultoría técnica', 1, 130.00, 130.00);

-- Líneas para Factura 6 (175 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(6, 'Auditoría TI', 5, 35.00, 175.00);

-- Líneas para Factura 7 (80 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(7, 'Backup mensual', 4, 20.00, 80.00);

-- Líneas para Factura 8 (300 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(8, 'Migración datos', 3, 100.00, 300.00);

-- Líneas para Factura 9 (210 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(9, 'Diseño gráfico', 6, 35.00, 210.00);

-- Líneas para Factura 10 (400 €)
INSERT INTO invoice_lines (invoice_id, description, quantity, unit_price, total) VALUES
(10, 'Landing page', 4, 100.00, 400.00);
