-- Base de datos de pruebas para Microservicio Usuario (Test Database)
-- Este archivo inserta los registros semilla (seed) solicitados en el IL3.1

INSERT INTO roles (id, name) VALUES (1, 'ROLE_USER');
INSERT INTO roles (id, name) VALUES (2, 'ROLE_ADMIN');

INSERT INTO users (id, username, password, email, nombre, apellido, enabled) VALUES 
(1, 'usuario_test1', '$2a$10$xyz', 'usuario1@test.com', 'Usuario', 'Uno', true);

INSERT INTO users (id, username, password, email, nombre, apellido, enabled) VALUES 
(2, 'usuario_test2', '$2a$10$xyz', 'usuario2@test.com', 'Usuario', 'Dos', true);

INSERT INTO users_roles (user_id, role_id) VALUES (1, 1);
INSERT INTO users_roles (user_id, role_id) VALUES (2, 2);

-- Perfiles simulados para pruebas de integración
INSERT INTO user_profiles (id, user_id, descripcion, edad, deporte_principal, atributos_deportivos) VALUES 
(1, 1, 'Jugador amateur', 25, 'BASKET', '{"altura": 180, "posicion": "Base"}');
