# Arquitectura Propuesta

## Vision General

La solucion se divide en dos aplicaciones independientes:

1. **Backend (Java + Spring Boot)**  
   - Responsabilidad: exponer APIs REST para gestionar empresas, productos, categorias, clientes, ordenes e inventario.  
   - Modulos principales:
     - `auth`: autenticacion con JWT y manejo de usuarios (Administrador y Externo).
     - `catalog`: CRUD de empresas, productos y categorias.
     - `orders`: registro de clientes y ordenes, asociacion de productos.
     - `inventory`: generacion de PDF y envio de correos (SendGrid en produccion, stub local en desarrollo, AWS SES opcional).
   - Persistencia: Spring Data JPA con PostgreSQL (produccion) y H2 en memoria (desarrollo/CI).
   - Seguridad: Spring Security con contrasenas bcrypt y validaciones declarativas.

2. **Frontend (React + Vite + TypeScript)**  
   - Responsabilidad: vistas solicitadas y formularios que consumen las APIs REST.
   - State management ligero con React Query y contexto para el token JWT.
   - UI basada en CSS modular co-localizado por feature.

## Modelo de Datos (ER)

- **Empresa** (`empresa`)
  - `nit` (PK)
  - `nombre`
  - `direccion`
  - `telefono`
  - Relaciones: `productos` (1:N)

- **Producto** (`producto`)
  - `id` (PK, UUID)
  - `codigo` (unico por empresa)
  - `nombre`
  - `caracteristicas`
  - `empresa_nit` (FK -> Empresa)
  - Relaciones: `categorias` (N:M), `ordenes` (N:M), `precios` (1:N)

- **PrecioProducto** (`precio_producto`)
  - `id` (PK, UUID)
  - `moneda` (ISO 4217)
  - `valor`
  - `producto_id` (FK -> Producto)

- **Categoria** (`categoria`)
  - `id` (PK, UUID)
  - `nombre`
  - Relaciones: `productos` (N:M)

- **Cliente** (`cliente`)
  - `id` (PK, UUID)
  - `nombre`
  - `correo` (unico)
  - Relaciones: `ordenes` (1:N)

- **Orden** (`orden`)
  - `id` (PK, UUID)
  - `fecha`
  - `cliente_id` (FK -> Cliente)
  - Relaciones: `items` (1:N), `productos` (N:M mediante `orden_item`)

- **OrdenItem** (`orden_item`)
  - `id` (PK, UUID)
  - `orden_id` (FK -> Orden)
  - `producto_id` (FK -> Producto)
  - `cantidad`
  - `precio_unitario`

- **Usuario** (`usuario`)
  - `id` (PK, UUID)
  - `correo` (unico)
  - `password` (bcrypt)
  - `rol` (`ADMIN` | `VIEWER`)

## Flujo de Autenticacion

1. Usuario envia correo y contrasena a `/api/auth/login`.
2. Backend valida credenciales con `UserDetailsService`.
3. Si es valido, se genera JWT firmado (HS256) con el rol.
4. El frontend guarda el token en contexto y lo adjunta a futuras peticiones.
5. Rutas protegidas (crear/editar/eliminar) exigen rol `ADMIN`.

## Generacion y Envio de PDF

1. Endpoint `GET /api/inventory/{empresaNit}/pdf` genera un PDF del inventario usando iText.
2. El PDF se devuelve como `application/pdf` y se almacena en disco (o en S3 si se habilita).
3. Endpoint `POST /api/inventory/{empresaNit}/email` recibe el correo destino.
4. `MailService` decide la estrategia segun `MAIL_PROVIDER`:
   - `sendgrid`: envia el PDF adjunto mediante la API REST de SendGrid.
   - `aws-ses`: usa AWS SES (opcional).
   - `local`: solo registra la operacion en logs para desarrollo.
5. Se registra el resultado y, si se habilito S3, se incluye la ubicacion de respaldo en el mensaje.

## Despliegue Recomendado (Render + Netlify + SendGrid)

1. **Base de datos:** instancia PostgreSQL gestionada por Render (addon) o proveedor equivalente.
2. **Backend:** servicio Web en Render construyendo `backend/` con `mvn clean package` y ejecutando el JAR resultante. Variables de entorno: `SPRING_DATASOURCE_*`, `JWT_SECRET`, `MAIL_PROVIDER=sendgrid`, `SENDGRID_API_KEY`, `MAIL_SENDER`, etc.
3. **Correo:** SendGrid con remitente verificado. Para entornos sin acceso externo, conservar `MAIL_PROVIDER=local`.
4. **Frontend:** deploy estatico en Netlify o Vercel construyendo `frontend/` (`npm run build`) y seteando `VITE_API_URL` apuntando al backend publicado.
5. **Infra opcional:** el directorio `infra/terraform` mantiene ejemplos para AWS; solo se requiere si se decide migrar la plataforma a ese proveedor.

## Buenas Practicas

- Aplicar principios SOLID en servicios y controladores.
- Usar DTOs y MapStruct para aislar entidades de persistencia.
- Validar entradas con `jakarta.validation`.
- Pruebas unitarias con JUnit y Mockito cubriendo flujos felices y errores.
- Documentar APIs con Swagger (Springdoc).

## Siguientes Pasos

1. Completar pruebas de integracion (PostgreSQL, SendGrid mockeado).
2. Automatizar pipelines de build/test en GitHub Actions.
3. Anadir monitoreo en Render (health checks) y alarmas de cuota en SendGrid.
4. Preparar migraciones Flyway/Liquibase para evolucionar el esquema sin recrear datos.
