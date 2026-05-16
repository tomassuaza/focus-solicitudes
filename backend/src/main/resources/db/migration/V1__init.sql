-- =============================================================
-- Schema inicial — Sistema Unico de Ingreso y Gestion de Solicitudes
-- Focus Agencia de Contenido
-- =============================================================

CREATE TABLE usuarios (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    email           VARCHAR(180) NOT NULL UNIQUE,
    nombre          VARCHAR(120) NOT NULL,
    rol             VARCHAR(40)  NOT NULL,
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE unidades_productivas (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    activa BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE clientes (
    id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(180) NOT NULL,
    tipo   VARCHAR(40)  NOT NULL,
    activo BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE solicitudes (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    cliente_id            BIGINT       NOT NULL,
    unidad_id             BIGINT       NOT NULL,
    creador_id            BIGINT       NOT NULL,
    tipo                  VARCHAR(40)  NOT NULL,
    prioridad             VARCHAR(20)  NOT NULL,
    descripcion           VARCHAR(2000) NOT NULL,
    estado                VARCHAR(40)  NOT NULL,
    plazo                 TIMESTAMP    NULL,
    fecha_creacion        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_solicitud_cliente  FOREIGN KEY (cliente_id) REFERENCES clientes(id),
    CONSTRAINT fk_solicitud_unidad   FOREIGN KEY (unidad_id)  REFERENCES unidades_productivas(id),
    CONSTRAINT fk_solicitud_creador  FOREIGN KEY (creador_id) REFERENCES usuarios(id)
);

CREATE INDEX idx_solicitudes_estado_unidad ON solicitudes(estado, unidad_id);
CREATE INDEX idx_solicitudes_fecha ON solicitudes(fecha_creacion);

CREATE TABLE tareas (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id        BIGINT       NOT NULL,
    responsable_id      BIGINT       NULL,
    depende_de_id       BIGINT       NULL,
    estado              VARCHAR(40)  NOT NULL,
    tiempo_real_minutos INT          NULL,
    fecha_inicio        TIMESTAMP    NULL,
    fecha_cierre        TIMESTAMP    NULL,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tarea_solicitud   FOREIGN KEY (solicitud_id)   REFERENCES solicitudes(id),
    CONSTRAINT fk_tarea_responsable FOREIGN KEY (responsable_id) REFERENCES usuarios(id),
    CONSTRAINT fk_tarea_dep         FOREIGN KEY (depende_de_id)  REFERENCES tareas(id)
);

CREATE INDEX idx_tareas_estado ON tareas(estado);
CREATE INDEX idx_tareas_responsable ON tareas(responsable_id);

CREATE TABLE historial_estado (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    tarea_id       BIGINT      NOT NULL,
    estado_anterior VARCHAR(40),
    estado_nuevo   VARCHAR(40) NOT NULL,
    usuario_id     BIGINT      NOT NULL,
    fecha          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    motivo         VARCHAR(500),
    CONSTRAINT fk_hist_tarea   FOREIGN KEY (tarea_id)   REFERENCES tareas(id),
    CONSTRAINT fk_hist_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios(id)
);

CREATE TABLE aprobaciones (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    solicitud_id   BIGINT      NOT NULL,
    aprobador_id   BIGINT      NOT NULL,
    decision       VARCHAR(20) NOT NULL,
    motivo         VARCHAR(500),
    fecha          TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_aprob_solicitud FOREIGN KEY (solicitud_id) REFERENCES solicitudes(id),
    CONSTRAINT fk_aprob_usuario   FOREIGN KEY (aprobador_id) REFERENCES usuarios(id)
);

CREATE TABLE notificaciones (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    destinatario    VARCHAR(180) NOT NULL,
    asunto          VARCHAR(240) NOT NULL,
    cuerpo          VARCHAR(4000) NOT NULL,
    estado          VARCHAR(40)  NOT NULL,
    intentos        INT          NOT NULL DEFAULT 0,
    error_mensaje   VARCHAR(2000),
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_envio     TIMESTAMP    NULL
);

-- =============================================================
-- Datos iniciales (seed) - unidades productivas y usuarios demo
-- =============================================================

INSERT INTO unidades_productivas (nombre) VALUES
    ('DISENO'),
    ('VIDEO'),
    ('TRAFFICKER'),
    ('SOCIAL_MEDIA'),
    ('ADMINISTRATIVO');

INSERT INTO clientes (nombre, tipo) VALUES
    ('Cliente Demo Mensual', 'MENSUAL'),
    ('Cliente Demo Puntual', 'PUNTUAL');

INSERT INTO usuarios (email, nombre, rol) VALUES
    ('coordinador@focusagency.co', 'Coordinador Demo', 'COORDINADOR'),
    ('disenador@focusagency.co',   'Disenador Demo',   'UNIDAD'),
    ('director@focusagency.co',    'Director Demo',    'DIRECCION');
