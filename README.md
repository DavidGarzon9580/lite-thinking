# Lite Thinking Platform 2024

Aplicación full-stack desarrollada para la prueba técnica Lite Thinking. Permite gestionar empresas, productos, clientes y órdenes, junto con funcionalidades de autenticación, inventario en PDF y envío de correos a través de AWS SES.

## 🧱 Arquitectura

- **Backend:** Java 17, Spring Boot 3, Spring Security, Spring Data JPA, JWT, OpenAPI, iText PDF, AWS SDK (SES/S3), H2/PostgreSQL.
- **Frontend:** React 18 + Vite + TypeScript, React Router, React Query, Axios.
- **Persistencia:** Modelo entidad-relación con las entidades requeridas (Empresa, Producto, Categoría, Cliente, Orden, OrdenItem, Usuario).
- **Autenticación:** JWT (roles `ADMIN` y `VIEWER`). Contraseñas encriptadas con BCrypt.
- **Inventario:** Generación de PDF con iText. Integración con AWS SES desacoplada mediante `MailService` (stub local por defecto, implementación SES opcional).

Consulta `docs/architecture.md` para ver el diseño detallado y el modelo ER.

## 🚀 Funcionalidades clave

- Login y registro de usuarios (rol asignable).
- Gestión completa de empresas (CRUD) con permisos administradores.
- Gestión de productos por empresa con categorías múltiples y precios en distintas monedas.
- Registro de clientes y órdenes con asociación N:M entre órdenes y productos.
- Descarga de inventario en PDF y envío por correo (stub local + implementación AWS SES).
- Swagger UI disponible en `/swagger-ui`.

## 🧰 Requisitos previos

- Java 17+
- Maven 3.9+
- Node.js 18+ y npm
- Base de datos PostgreSQL (opcional; H2 se usa por defecto en desarrollo)

## 🗃️ Ajustes de entorno

Las propiedades principales se encuentran en `backend/src/main/resources/application.yml`.

Configura variables para producción:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://<host>:5432/litethinking
export SPRING_DATASOURCE_USERNAME=<usuario>
export SPRING_DATASOURCE_PASSWORD=<password>
export JWT_SECRET=<cadena_de_32_caracteres_o_base64>
export AWS_REGION=us-east-1
export AWS_SES_SENDER=notificaciones@tu-dominio.com
export AWS_S3_BUCKET=litethinking-inventory
export AWS_SES_ENABLED=true # Solo si se desea usar la integración real
```

## 🧪 Credenciales de ejemplo

| Rol      | Usuario                   | Contraseña |
|----------|---------------------------|------------|
| ADMIN    | `admin@litethinking.com`  | `Admin123*`|
| VIEWER   | `viewer@litethinking.com` | `Viewer123*` |

## ▶️ Ejecución local

### Backend

```bash
cd backend
mvn spring-boot:run
```

El backend iniciará en `http://localhost:8080`. Swagger UI: `http://localhost:8080/swagger-ui`.

### Frontend

```bash
cd frontend
npm install
npm run dev
```

La aplicación quedará disponible en `http://localhost:5173`. El proxy Vite redirige `/api` al backend.

## 🐘 Migración a PostgreSQL

1. Crea una base de datos.
2. Actualiza las variables `spring.datasource.*` en `application.yml` o usa variables de entorno.
3. Ajusta `spring.jpa.hibernate.ddl-auto=validate` (recomendado para producción).

## 🧾 Inventario: PDF + Correo

- `GET /api/inventory/{nit}/pdf`: descarga el PDF.
- `POST /api/inventory/{nit}/email`: envía el PDF al correo recibido (requiere rol ADMIN).
- El bean `LocalMailService` escribe trazas simulando el envío. Para activar AWS SES:
  - Configura credenciales AWS (IAM con permisos `ses:SendRawEmail`).
  - Define `aws.ses.enabled=true` y `aws.region`, `aws.ses.sender`.
  - El bean `SesMailService` tomará prioridad automáticamente.

## 🌐 Despliegue en AWS (guía rápida)

1. **Backend**
   - Empaqueta: `mvn clean package`.
   - Despliega el `jar` en Elastic Beanstalk (Java 17) o ECS Fargate.
   - Configura RDS PostgreSQL y variables de entorno JWT/AWS.
   - Expone puertos 80/443 mediante ALB.

2. **Frontend**
   - Build: `npm run build`.
   - Sube la carpeta `dist/` a un bucket S3 con hosting estático.
   - Configura CloudFront + SSL y redirige `/api` al backend.

3. **SES**
   - Verifica dominio y correos en SES.
   - Sal del modo sandbox (opcional).
   - Actualiza `aws.ses.sender` y credenciales IAM.

## 🧪 Pruebas

- Ejecuta `mvn test` para comprobar el contexto de Spring Boot.
- Se pueden añadir pruebas unitarias con JUnit5/Mockito para servicios críticos.
- Para el frontend, agrega `vitest` + `testing-library/react` (no incluidos por tiempo).
- Nuevas pruebas:
  - Backend: `mvn test` cubre servicios clave (empresa, inventario).
  - Frontend: `npm run test -- --run` ejecuta pruebas con Vitest y Testing Library (requiere `npm install` previo).

## 📄 Estructura relevante

```
backend/
  src/main/java/com/litethinking/platform/
    auth/          # Seguridad y autenticación (JWT, usuarios)
    catalog/       # Empresas, productos, categorías
    orders/        # Clientes, órdenes y sus DTOs
    inventory/     # Generación de PDF y mail service
    common/        # Config, excepciones y manejadores
frontend/
  src/
    pages/         # Vistas (Login, Empresas, Productos, Inventario, Home)
    context/       # Contexto de autenticación
    hooks/         # Hook para Axios con token
    services/      # Llamadas HTTP a la API
docs/
  architecture.md  # Documento de arquitectura y modelo ER
.github/workflows/ci.yml   # Pipeline CI para backend/frontend
infra/terraform/           # Plantilla IaC (S3 inventario + SES identity opcional)
```

## ✅ Próximos pasos sugeridos

- Añadir pruebas unitarias/mocks para servicios críticos del backend.
- Incorporar validaciones adicionales y manejo de errores refinado en el frontend.
- Asegurar pipeline CI/CD (GitHub Actions) para build + despliegue automatizado.
- Añadir internacionalización y diseño responsivo más elaborado.

> CI listo: el workflow `.github/workflows/ci.yml` ejecuta `npm run test` y `mvn test` en cada push/PR.
>
> Infraestructura base: en `infra/terraform/` hay un plan para crear bucket S3 y verificar remitente SES (personaliza `variables.tf` antes de `terraform apply`).

---

> _Lista para desplegar. Ajusta las credenciales y variables en AWS y tendrás la plataforma funcionando end-to-end._
