# Lite Thinking Platform 2024

Aplicacion full-stack desarrollada como evaluacion tecnica para Lite Thinking. Cubre la gestion de empresas, productos, clientes y ordenes, ademas de autenticacion basada en JWT y generacion/envio de inventarios en PDF.

## Arquitectura

- **Backend:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA, JWT, OpenAPI, iText PDF, integraciones con SendGrid (prod) y S3/SES opcionales, H2 (dev) y PostgreSQL (prod).
- **Frontend:** React 18 + Vite + TypeScript, React Router, TanStack Query, Axios.
- **Infraestructura opcional:** RDS PostgreSQL, S3 para almacenamiento de inventarios, SES para correo, despliegue en Elastic Beanstalk/App Runner + S3/CloudFront.

Documento de arquitectura y modelo ER: `docs/architecture.md`.

## Estructura relevante

```
backend/
  src/main/java/com/litethinking/platform/
    auth/        # Seguridad, JWT, usuarios
    catalog/     # Empresas, productos, categorias
    inventory/   # Generacion PDF + integraciones S3/SES
    orders/      # Clientes y ordenes
    common/      # Configuracion compartida, excepciones, seeds
  src/test/java/com/litethinking/platform/
    auth/        # Pruebas unitarias Auth/JWT
    catalog/     # Pruebas ProductoService, EmpresaService
    inventory/   # Pruebas InventoryService

frontend/
  src/
    pages/
      Home/        # Landing interna
      Login/
      Empresas/
      Productos/
      Inventario/  # Cada carpeta contiene componente + CSS + test
    components/    # Header, rutas protegidas, etc.
    context/       # AuthContext (manejo JWT + localStorage)
    hooks/         # useApi para inyectar token en Axios
    services/      # Clientes HTTP por feature
docs/
  architecture.md
  testing.md       # Guía de pruebas y comandos
.github/workflows/
  ci.yml           # Ejecuta lint/tests backend/frontend
```

## Prerrequisitos

- Java 17+
- Maven 3.9+
- Node.js 18+ y npm
- (Produccion) PostgreSQL y cuenta SendGrid (plan gratuito). Integraciones AWS (S3/SES) son opcionales.

## Variables de entorno claves

### Backend (Spring Boot)

```bash
# Base de datos
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
SPRING_DATASOURCE_USERNAME=<usuario>
SPRING_DATASOURCE_PASSWORD=<password>

# Seguridad
JWT_SECRET=<cadena_de_32_chars_o_base64>

# Correo
MAIL_PROVIDER=sendgrid         # valores: sendgrid | local | aws-ses
MAIL_SENDER=<remitente@dominio.com>
SENDGRID_API_KEY=<api_key>     # requerido si MAIL_PROVIDER=sendgrid

# Opcional AWS (solo si habilitas S3/SES)
AWS_S3_ENABLED=false
AWS_S3_BUCKET=<bucket-para-inventarios>
AWS_SES_ENABLED=false
AWS_SES_SENDER=<correo-verificado@dominio.com>
AWS_REGION=us-east-1
```

### Frontend (Vite)

```bash
VITE_API_URL=https://<tu-backend>.onrender.com/api
```

Credenciales precargadas en el seed (`backend/src/main/java/.../DataInitializer.java`):

| Rol   | Usuario                  | Password   |
|-------|--------------------------|------------|
| ADMIN | admin@litethinking.com   | Admin123*  |
| VIEWER| viewer@litethinking.com  | Viewer123* |

## Ejecucion local

### Backend

```bash
cd backend
mvn spring-boot:run
```

Servicio disponible en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Vite sirve la SPA en `http://localhost:5173` y reenvia `/api` al backend local.

## Correo electrònico

`MailService` se selecciona segun `MAIL_PROVIDER`:

- `local` (default): solo registra en logs.
- `sendgrid`: utiliza SendGrid REST (`SENDGRID_API_KEY`, `MAIL_SENDER`).
- `aws-ses`: mantiene la implementacion previa (requiere credenciales AWS + `AWS_SES_ENABLED=true`).

## Pruebas

Guia ampliada en `docs/testing.md`. Resumen:

- **Backend:** `cd backend && mvn -B test`
- **Frontend:** `cd frontend && npm run test -- --run --reporter=dot`
  - Si necesitas omitir Vitest temporalmente: `npm run test -- --run --reporter=dot --passWithNoTests`
- Verifica el build antes de desplegar: `npm run build`

## Plan de despliegue recomendado (Render + Netlify + SendGrid)

### 1. Base de datos
- Crea un servicio PostgreSQL en Render (o Railway). Copia URL, usuario y contraseña para las variables `SPRING_DATASOURCE_*`.

### 2. Backend (Render Web Service)
1. Empaqueta: `cd backend && mvn clean package`.
2. Sube el repo a GitHub y crea un **Web Service** en Render apuntando al directorio `backend/` (puedes usar el `render.yaml` incluido y desplegarlo como Blueprint).
3. Build command: `./mvnw clean package` (Render instala Maven automáticamente) y Start command: `java -jar target/platform-backend-0.0.1-SNAPSHOT.jar`.
4. Define las variables de entorno descritas arriba (`SPRING_DATASOURCE_*`, `JWT_SECRET`, `MAIL_*`, etc.).
5. Añade el add-on de Postgres desde Render si no usaste uno externo y conecta la URL generada.

### 3. Correo (SendGrid)
1. Crea una cuenta gratuita en [SendGrid](https://sendgrid.com/free/).
2. Genera un API Key con permisos "Full Access" o "Mail Send".
3. Configura un remitente verificado.
4. En Render añade `MAIL_PROVIDER=sendgrid`, `MAIL_SENDER=correo@verificado.com`, `SENDGRID_API_KEY=<token>`.

### 4. Frontend (Netlify o Vercel)
1. `cd frontend && npm run build` (garantiza artefactos).
2. Conecta el repo y selecciona `frontend/` como directorio (Netlify detectara el `netlify.toml` incluido).
3. Build command: `npm run build`; Publish directory: `frontend/dist`.
4. Define la variable `VITE_API_URL=https://<render-backend>.onrender.com/api`. (o usa un proxy si mantienes el mismo path `/api` y configuras rewrite).
5. Actualiza `frontend/netlify.toml` reemplazando `https://backend-ejemplo.onrender.com` por el dominio real de tu backend para que las rutas `/api/*` se redirijan correctamente.

### 5. Integraciones opcionales
- **Inventario en S3:** mantiene la implementación anterior (`AWS_S3_ENABLED=true` + credenciales) aunque no es necesario para Render/Netlify; también puedes usar servicios como Backblaze o Cloudflare R2 ajustando `DocumentStorageService`.
- **Infra con Terraform:** `infra/terraform/` contiene ejemplos para AWS; adáptalo si decides ir por allí.

## Notas adicionales

- `docs/testing.md` recoge los comandos y objetivos de cobertura.
- `docs/architecture.md` contiene modelo ER, diagrama de componentes y decisiones de diseño.
- El seed crea usuarios admin/viewer y una empresa/producto de ejemplo.
- El `MailService` selecciona automaticamente la implementacion (local, SendGrid o AWS SES) segun `MAIL_PROVIDER`.
- La nueva estructura por features simplifica el mantenimiento del frontend y evita estilos/globales heredados indeseados.

## Siguientes pasos recomendados

1. Automatizar despliegue con GitHub Actions (Render deploy hook + Netlify build hook).
2. Integrar Jacoco + Sonar para cobertura backend y establecer thresholds en Vitest.
3. Agregar pruebas de integracion end-to-end (Playwright) para los flujos criticos (login, CRUD, inventario).
4. Preparar scripts de migracion para datos preexistentes y parametrizar seeds por perfil.

> Una vez valides localmente (`mvn test`, `npm run build`), continua con la configuracion en Render/Netlify (o tu nube elegida) y publica los servicios. La aplicacion esta lista para operar end-to-end siguiendo los pasos anteriores.
