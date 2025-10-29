# Lite Thinking Platform 2024

Aplicacion full-stack desarrollada como evaluacion tecnica para Lite Thinking. Cubre la gestion de empresas, productos, clientes y ordenes, ademas de autenticacion basada en JWT y generacion/envio de inventarios en PDF.

## Arquitectura

- **Backend:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA, JWT, OpenAPI, iText PDF, AWS SDK (SES/S3), H2 (dev) y PostgreSQL (prod).
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
- (Produccion) PostgreSQL, cuenta AWS con SES y S3 habilitados

## Configuracion de entorno

Variables principales (puedes exportarlas o definirlas en el runtime):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/<db>
export SPRING_DATASOURCE_USERNAME=<usuario>
export SPRING_DATASOURCE_PASSWORD=<password>
export JWT_SECRET=<cadena_de_32_chars_o_base64>
export AWS_REGION=us-east-1
export AWS_S3_BUCKET=<bucket-para-inventarios>
export AWS_SES_ENABLED=true        # Solo si usaras SES real
export AWS_SES_SENDER=<correo-verificado@dominio.com>
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

## Pruebas

Guia ampliada en `docs/testing.md`. Resumen:

- **Backend:** `cd backend && mvn -B test`
- **Frontend:** `cd frontend && npm run test -- --run --reporter=dot`
  - Si necesitas omitir Vitest temporalmente: `npm run test -- --run --reporter=dot --passWithNoTests`
- Verifica el build antes de desplegar: `npm run build`

## Despliegue sugerido en AWS

1. **Base de datos:** crea RDS PostgreSQL, habilita seguridad VPC y registra variables `SPRING_DATASOURCE_*`.
2. **Backend:**
   - Empaqueta con `mvn clean package`.
   - Despliega el JAR en Elastic Beanstalk (Corretto 17) o App Runner (ECR).
   - Configura IAM para acceso a S3/SES y Secrets Manager si guardas credenciales ahi.
3. **Frontend:**
   - `npm run build` -> sincroniza `dist/` con un bucket S3 (hosting estatico privado).
   - Crea una distribucion CloudFront con OAC y redireccion 403/404 -> `index.html`.
   - Ajusta la variable `VITE_API_URL` si necesitas apuntar a un dominio distinto.
4. **SES / S3:**
   - Verifica remitente y dominio en SES, sal del sandbox de ser necesario.
   - Crea el bucket de inventarios (mismo nombre que `AWS_S3_BUCKET`).

Terraform base para S3/SES disponible en `infra/terraform/` (personaliza variables antes de aplicar).

## Notas adicionales

- `docs/testing.md` recoge los comandos y objetivos de cobertura.
- `docs/architecture.md` contiene modelo ER, diagrama de componentes y decisiones de diseño.
- El seed crea usuarios admin/viewer y una empresa/producto de ejemplo.
- `MailService` usa `LocalMailService` por defecto (solo deja logs). Activa `aws.ses.enabled=true` para usar `SesMailService`.
- La nueva estructura por features simplifica el mantenimiento del frontend y evita estilos/globales heredados indeseados.

## Siguientes pasos recomendados

1. Automatizar despliegue con GitHub Actions (build + push a ECR + invalidacion CloudFront).
2. Integrar Jacoco + Sonar para cobertura backend y establecer thresholds en Vitest.
3. Agregar pruebas de integracion end-to-end (Playwright) para los flujos criticos (login, CRUD, inventario).
4. Preparar scripts de migracion para datos preexistentes y parametrizar seeds por perfil.

> Una vez valides localmente (`mvn test`, `npm run build`), continua con la configuracion AWS y despliega. La aplicacion esta lista para operar end-to-end siguiendo los pasos anteriores.
