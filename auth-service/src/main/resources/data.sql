-- Limpieza por si queda algo de ejecuciones anteriores
DELETE FROM user_roles;
DELETE FROM users;

-- Insertar usuario con contraseña hash de "admin123"
INSERT INTO users (id, username, email, password)
VALUES (
  1,
  'admin',
  'admin@mail.com',
  '$2a$10$6s7FT4nTPHfHvCg1/6qBfuHzLOvSK8R2RtFjxHZm1vBDFIbHUaQYi'
);

-- Asignar rol ADMIN al usuario
INSERT INTO user_roles (user_id, roles)
VALUES (1, 'ADMIN');


