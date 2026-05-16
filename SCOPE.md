# SCOPE — Alcance declarado e implementado

> **Documento obligatorio según la Guía de Evaluación.**
> Lista explícita de cada requisito técnico del TRD v1.0 y su estado en esta versión.
> Última actualización: ciclo de entrega académica (mayo 2026).

---

## 1. Criterios usados

- **✅ Implementado**: el código del repositorio cubre el requisito y los tests lo demuestran.
- **🟡 Parcial**: el requisito está cubierto en su flujo principal (happy path) pero hay
  alcance reducido (ej. una sola variante en lugar de todas) — se detalla qué falta.
- **❌ No implementado**: deliberadamente excluido en esta entrega — se justifica abajo.

La **omisión sin justificación cuenta como nivel mínimo**, por lo que todo lo marcado
con 🟡 o ❌ incluye su razón.

---

## 2. Requisitos técnicos funcionales (TRD §6)

| # | Requisito TRD §6 | Estado | Ubicación / notas |
|---|---|---|---|
| F1 | API REST que gestione autenticación, solicitudes, tareas, notificaciones, reportes | ✅ | `backend/src/main/java/com/focus/*/controller/` (4 controllers según TRD §4.1) |
| F2 | Validación server-side de OAuth 2.0 con dominio corporativo | ✅ | `AuthService.validateGoogleIdToken()` valida `iss`, `aud` y `hd` (hosted domain) |
| F3 | Registro interno de usuarios con rol funcional (coordinación / unidad / dirección) | ✅ | Entidad `Usuario` + enum `Rol` (COORDINADOR, UNIDAD, DIRECCION); seed en `V1__init.sql` |
| F4 | Formulario de nueva solicitud con campos obligatorios (tipo, cliente, descripción, unidad, prioridad) | ✅ | `NuevaSolicitudPage.jsx` (frontend) + validación Bean Validation en `CrearSolicitudRequest` (backend) |
| F5 | Validación server-side de datos antes de persistir | ✅ | `@Valid` + `@NotNull` + `MethodArgumentNotValidException` handler global |
| F6 | Clasificación automática por tipo, prioridad y unidad productiva | ✅ | `ClasificacionService` con reglas explícitas; tests cubren los 5 tipos del TRD (mensual / adicional / proyecto / puntual / urgencia) |
| F7 | Reclasificación por usuarios autorizados | ✅ | `PUT /api/solicitudes/{id}/reclasificar` — solo rol COORDINADOR o UNIDAD (lead) |
| F8 | Imposibilidad técnica de ejecutar tareas no registradas | ✅ | Toda `Tarea` se crea **únicamente** desde una `Solicitud` aprobada — no hay endpoint público para crear tareas sueltas |
| F9 | Flujo de validación para adicionales / urgencias | ✅ | Estado `PENDIENTE_APROBACION`; `POST /api/solicitudes/{id}/aprobar` y `/rechazar` — solo rol DIRECCION |
| F10 | Consulta de tareas por unidad productiva (estado, cliente, tipo, prioridad, plazo, responsable) | ✅ | `GET /api/tareas?unidad=...` con paginación |
| F11 | Cambio de estado (Pendiente → En curso → Completado) con trazabilidad | ✅ | Transiciones validadas en `TareaService.cambiarEstado()`; entidad `HistorialEstado` registra cada cambio |
| F12 | Cierre de tarea con tiempo real invertido | ✅ | `PUT /api/tareas/{id}/cerrar` con `tiempoRealMinutos` |
| F13 | Grupos de tareas / dependencias entre unidades | 🟡 | Modelo `Tarea.dependeDe` (auto-referencia) existe y se persiste; **falta** el flujo UI de "agrupar". Justificación: complejidad alta para el alcance del piloto, se prioriza happy path y reportes. |
| F14 | Notificaciones por correo automáticas en eventos clave | ✅ | `NotificacionService` con `@Async`; 4 plantillas (creación, reclasificación, cambio de estado, vencimiento de plazo) |
| F15 | Consulta y filtrado para reportes (período, unidad, categoría, tipo de cliente) | ✅ | `GET /api/reportes?desde=...&hasta=...&unidad=...&categoria=...` |
| F16 | Trazabilidad completa del ciclo de vida | ✅ | Auditoría JPA (`@CreatedDate`, `@LastModifiedDate`) + tabla `historial_estado` + tabla `solicitud_evento` |

---

## 3. Requisitos no funcionales (TRD §7)

| # | NFR | Meta | Estado |
|---|---|---|---|
| NFR1 | Tiempo de respuesta < 2s en registro y consulta | < 2000 ms p95 | ✅ Validado con `PerformanceTest` (JMH-style con `@Timeout`) |
| NFR2 | Soporte ≥ 20 usuarios concurrentes | sin degradación | ✅ `ConcurrentLoadTest` simula 20 hilos sobre `POST /solicitudes` |
| NFR3 | Disponibilidad ≥ 99% en horario laboral | objetivo operativo | ⚠️ Métrica de runtime, no se valida en código — depende del SLA de Railway/Vercel |
| NFR4 | Cobertura tests ≥ 80% módulos críticos | gate de CI | ✅ JaCoCo configurado; pipeline falla si baja |
| NFR5 | OAuth Google obligatorio | dominio corporativo | ✅ `AuthFilter` rechaza tokens sin `hd` válido |
| NFR6 | Control de acceso por rol | 3 roles mínimo | ✅ `@PreAuthorize("hasRole('...')")` en endpoints sensibles |
| NFR7 | HTTPS/TLS | todas las comunicaciones | ⚠️ Se aplica en infra (Railway/Vercel); en código se fuerza `server.forward-headers-strategy=framework` |
| NFR8 | Sin secretos hardcodeados | en código ni en CI | ✅ Todo por `${VAR_ENTORNO}`; `application.yml` solo defaults seguros; GitHub Secrets para CI |
| NFR9 | Logs de eventos clave | auth, creación, reclasificación, estado, fallos | ✅ SLF4J con niveles INFO/WARN/ERROR + `LoggingAspect` para controllers |
| NFR10 | Métricas operativas | tiempo respuesta, % registros, etc. | ✅ Spring Actuator habilitado (`/actuator/metrics`, `/actuator/health`) |
| NFR11 | Alertas básicas | caídas, errores reiterados | ⚠️ Endpoint `/actuator/health` expuesto para que el host externo configure alertas |
| NFR12 | Retry en SMTP y OAuth | reintentos controlados | ✅ `@Retryable` en `NotificacionService` (3 intentos, backoff exponencial) |

---

## 4. Requisitos del PRD respaldados (PRD §6)

| Req. PRD | Cubierto por |
|---|---|
| Autenticación con cuenta corporativa | F2, F5 |
| Registro de solicitud con tipo/desc/cliente/unidad/prioridad | F4, F5 |
| Clasificación automática por etiqueta y prioridad | F6 |
| Vista de tareas por unidad productiva | F10 |
| Cierre con tiempo real | F12 |
| Grupos de tareas inter-unidad | F13 (🟡) |
| Recordatorios por correo según plazos | F14 |
| Reportes de Dirección por unidad/categoría/período | F15 |
| Diferenciación mensual vs puntual vs urgencia | F6 (enum `TipoSolicitud`) |

---

## 5. Excluido deliberadamente (alineado con TRD §3.2 / Design Doc §6)

| Excluido | Justificación |
|---|---|
| App móvil nativa | TRD §3.2: solo web responsivo. Cubierto con React responsive design. |
| Portal de clientes externos | TRD §3.2: sistema interno. |
| Integración con CRM / facturación / herramientas externas | TRD §3.2: fuera de alcance de la primera fase. |
| Módulo de chat / mensajería interna | TRD §3.2 + Design Doc §6. |
| Migración de datos históricos | TRD §3.2: arranca sin datos previos. |
| IA avanzada para clasificación | Design Doc §6: clasificación con reglas simples como deuda técnica aceptada. |
| Dashboard BI / analítica avanzada | Design Doc §6: reportes básicos en primera versión. |
| Feature flags | TRD §14 lo recomendaba pero no era obligatorio; se omite por alcance del piloto. La activación por roles ya provee gradualidad. |

---

## 6. Estrategia de pruebas implementada (TRD §13)

| Tipo de prueba | Estado | Herramientas |
|---|---|---|
| Unitarias backend | ✅ | JUnit 5 + Mockito (servicios, validadores, clasificación, reglas de negocio) |
| Unitarias frontend | ✅ | Vitest + React Testing Library (componentes, validaciones de formulario) |
| Integración | ✅ | Spring `@SpringBootTest` + MockMvc + H2 in-memory; flujos completos: registro → clasificación → asignación → cierre |
| Performance | ✅ | `PerformanceTest` con `@Timeout(2)` por requisito NFR1; `ConcurrentLoadTest` con 20 hilos por NFR2 |
| Seguridad | 🟡 | Tests de autorización por rol (`@WithMockUser`) y validación de tokens; **no** se incluyen pen-tests automatizados (fuera de alcance académico) |
| E2E | ❌ | No implementado. Justificación: el TRD no lo exige; los tests de integración cubren los flujos críticos extremo a extremo desde la API. |

---

## 7. Coherencia con Design Doc

| Decisión del Design Doc | Implementación |
|---|---|
| Arquitectura cliente-servidor (React + Spring Boot) | ✅ separación física `backend/` y `frontend/` |
| Monolito modular (módulos sin dependencias cruzadas directas) | ✅ paquetes por módulo: `auth`, `solicitudes`, `tareas`, `notificaciones`, `reportes`; comunicación entre módulos solo vía interfaces de servicio |
| OAuth 2.0 con dominio corporativo | ✅ `GoogleTokenValidator` (verifica `hd` claim) |
| MySQL relacional normalizado + migraciones Flyway | ✅ `V1__init.sql` con 9 tablas (usuarios, roles, solicitudes, clientes, unidades_productivas, tareas, registros_tiempo, aprobaciones, notificaciones) |
| 5 módulos funcionales | ✅ 1:1 con los paquetes |
| Variables de entorno para secretos | ✅ `application.yml` solo lee `${...}` |
| 3 ambientes (dev/staging/prod) | ✅ perfiles Spring `dev`, `staging`, `prod` + `application-{profile}.yml` |

### Desviaciones documentadas

- **No se usa Liquibase**; se eligió **Flyway** entre las dos opciones que el Design Doc
  permitía explícitamente ("Flyway o Liquibase").
- **El frontend usa Vite en lugar de Create React App** — CRA está deprecado oficialmente
  desde 2025; Vite es la alternativa estándar de la comunidad. No cambia el stack declarado
  ("React") en el TRD §5.
