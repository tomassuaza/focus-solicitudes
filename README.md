# Focus — Sistema Único de Ingreso y Gestión de Solicitudes

Aplicativo web interno para **Focus Agencia de Contenido**: centraliza el registro,
clasificación, asignación, ejecución y reporte de solicitudes operativas.

Repositorio único con backend, frontend, pruebas y configuración de CI.

> Documentos de referencia: RFC-001, PRD v1.0, TRD v1.0, Design Doc.
> Estado: implementación piloto.

---

## Equipo

| Integrante | Rol |
|---|---|
| Andrés Ospina | Tech Lead / Producto |
| Daniel Herrera | Desarrollo Backend |
| Juan Esteban Cuervo | Desarrollo Backend / Seguridad / SRE |
| Tomás Suaza | Desarrollo Frontend / UX |

---

## Stack

- **Backend**: Java 21 + Spring Boot 3.4 + Spring Security + Spring Data JPA
- **Persistencia**: MySQL 8 (Flyway para migraciones versionadas). H2 in-memory en tests.
- **Frontend**: React 18 + Vite + React Router + Vitest + React Testing Library
- **Auth**: Google OAuth 2.0 (validación server-side de ID tokens)
- **Notificaciones**: Gmail SMTP (JavaMailSender)
- **CI**: GitHub Actions (build + test + coverage + análisis estático)

---

## Estructura

```
focus-solicitudes/
├── backend/        # API REST Spring Boot
├── frontend/       # Aplicación React
├── docs/           # Documentos del proyecto
├── .github/
│   └── workflows/  # Pipeline de CI
├── SCOPE.md        # Alcance declarado (qué del TRD está implementado y qué no)
└── README.md
```

---

## Cómo correr el backend

Requisitos: Java 21+, Maven 3.9+, MySQL 8 (opcional para dev — usa H2 si no hay perfil prod).

```bash
cd backend
# Variables de entorno (NUNCA commitear)
cp .env.example .env   # editar con tus valores reales
mvn spring-boot:run
```

El backend arranca por defecto en `http://localhost:8080` con perfil `dev` (H2 en memoria,
auth OAuth desactivada para desarrollo local rápido).

### Variables de entorno requeridas (producción)

| Variable | Descripción |
|---|---|
| `DB_URL` | JDBC URL de MySQL (ej. `jdbc:mysql://...:3306/focus`) |
| `DB_USER` / `DB_PASSWORD` | Credenciales MySQL |
| `GOOGLE_OAUTH_CLIENT_ID` | Client ID del proyecto Google Cloud |
| `GOOGLE_ALLOWED_DOMAIN` | Dominio corporativo permitido (ej. `focusagency.co`) |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` | Credenciales SMTP de Gmail |
| `JWT_SECRET` | Secreto para firmar JWTs propios (mínimo 256 bits) |

> ⚠️ **Nunca hardcodear secretos en código.** Usar variables de entorno.

---

## Cómo correr el frontend

Requisitos: Node 20+, npm 10+.

```bash
cd frontend
npm install
cp .env.example .env
npm run dev          # http://localhost:5173
```

---

## Pruebas

### Backend (JUnit 5 + Mockito + MockMvc + Testcontainers)

```bash
cd backend
mvn test                # unitarias + integración
mvn verify              # incluye cobertura JaCoCo
mvn test -Dtest=*PerformanceTest    # solo performance
```

Umbral mínimo de cobertura: **80%** en módulos críticos (lo bloquea el CI).

### Frontend (Vitest)

```bash
cd frontend
npm test
npm run test:coverage
```

---

## CI / CD

Pipeline en [.github/workflows/ci.yml](.github/workflows/ci.yml).

En cada push y PR a `main` se ejecuta:

1. **Build backend** (`mvn -B compile`)
2. **Tests backend** (unitarios + integración, JUnit + JaCoCo)
3. **Análisis estático backend** (Checkstyle + SpotBugs)
4. **Quality gate**: cobertura ≥ 80% en módulos críticos → **bloquea merge**
5. **Build frontend** (`npm run build`)
6. **Tests frontend** (Vitest)
7. **Análisis estático frontend** (ESLint)

No se hace deploy automático — el despliegue a Railway/Vercel es manual desde la rama `main`.

---

## Alcance declarado

Ver [SCOPE.md](SCOPE.md). Lista explícita de cada requisito del TRD con su estado:
implementado / parcial / no implementado, con justificación.
