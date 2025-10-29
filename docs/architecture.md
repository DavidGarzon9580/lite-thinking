# Arquitectura Propuesta

## Visión General

La solución se dividirá en dos aplicaciones independientes:

1. **Backend (Java + Spring Boot)**  
   - Responsabilidad: Exponer APIs REST para gestionar empresas, productos, categorías, clientes, órdenes e inventario.  
   - Módulos principales:
     - `auth`: autenticación con JWT y manejo de usuarios (Administrador y Externo).
     - `catalog`: CRUD de empresas, productos y categorías.
     - `orders`: registro de clientes y órdenes, asociación de productos.
     - `inventory`: generación de PDF y envío a AWS SES (o simulación/local stub en caso de restricciones).
   - Persistencia: Spring Data JPA con PostgreSQL/MySQL (configurable).
   - Seguridad: Spring Security con BCrypt para contraseñas.

2. **Frontend (React + Vite + TypeScript)**  
   - Responsabilidad: Vistas y formularios solicitados, consumo de APIs REST.
   - State management simple con React Query + Context.
   - UI: Chakra UI o Tailwind (dependiendo de disponibilidad sin internet; se dejará CSS modular si no se pueden descargar dependencias).

## Modelo de Datos (ER)

- **Empresa** (`empresa`)
  - `nit` (PK)
  - `nombre`
  - `direccion`
  - `telefono`
  - Relaciones: `productos` (1:N)

- **Producto** (`producto`)
  - `id` (PK, UUID)
  - `codigo` (único por empresa)
  - `nombre`
  - `caracteristicas`
  - `precios` (relación con tabla `precio_producto`)
  - `empresa_nit` (FK → Empresa)
  - Relaciones: `categorias` (N:M), `ordenes` (N:M)

- **PrecioProducto** (`precio_producto`)
  - `id` (PK, UUID)
  - `moneda` (ISO 4217)
  - `valor`
  - `producto_id` (FK → Producto)

- **Categoria** (`categoria`)
  - `id` (PK, UUID)
  - `nombre`
  - Relaciones: `productos` (N:M)

- **Cliente** (`cliente`)
  - `id` (PK, UUID)
  - `nombre`
  - `correo` (único)
  - Relaciones: `ordenes` (1:N)

- **Orden** (`orden`)
  - `id` (PK, UUID)
  - `fecha`
  - `cliente_id` (FK → Cliente)
  - Relaciones: `items` (1:N), `productos` (N:M mediante `orden_item`)

- **OrdenItem** (`orden_item`)
  - `id` (PK, UUID)
  - `orden_id` (FK → Orden)
  - `producto_id` (FK → Producto)
  - `cantidad`
  - `precio_unitario`

- **Usuario** (`usuario`)
  - `id` (PK, UUID)
  - `correo` (único)
  - `password` (BCrypt)
  - `rol` (`ADMIN` | `VIEWER`)

## Flujo de Autenticación

1. Usuario envía correo y contraseña a `/api/auth/login`.
2. Backend valida credenciales con `UserDetailsService`.
3. Si es válido, se genera JWT firmado (HS256) con rol.
4. El frontend guarda el token en memoria segura (contexto) y lo adjunta a futuras peticiones.
5. Rutas protegidas (guardar/editar/eliminar) requieren rol `ADMIN`.

## Generación y Envío de PDF

1. Endpoint `/api/inventory/{empresaNit}/pdf` genera un PDF del inventario de una empresa usando iText o JasperReports (dependiendo de librerías disponibles offline).
2. El PDF se devuelve como `application/pdf` y se almacena temporalmente.
3. Endpoint `/api/inventory/{empresaNit}/email` recibe un correo destino.
4. Servicio de AWS SES (o stub local) envía el PDF generado.  
   - Si no se puede acceder a AWS por restricciones, se implementa una interfaz `MailService` con:
     - `SesMailService` (cuando se despliega realmente).
     - `LocalMailService` (para desarrollo, guarda el archivo en el filesystem y registra logs).

## Despliegue en AWS (Plan)

1. **Backend**: Empaquetado con Maven/Gradle en `jar` y desplegado en AWS Elastic Beanstalk o ECS Fargate. Base de datos gestionada en RDS.  
2. **Frontend**: Build estático y deploy en S3 + CloudFront.  
3. **Infra**: Terraform o CloudFormation opcional (fuera del alcance inmediato por tiempo). Se documentarán pasos manuales mínimos.

## Buenas Prácticas

- Principios SOLID aplicados en servicios y controladores.  
- DTOs para comunicación externa.  
- MapStruct (si está disponible) o mapeo manual.  
- Validaciones con `javax.validation`.  
- Pruebas unitarias básicas con JUnit y Mockito.  
- Documentación con OpenAPI/Swagger.

## Siguientes Pasos

1. Inicializar proyecto Spring Boot estructurado.  
2. Implementar entidades, repositorios, servicios y controladores.  
3. Configurar seguridad JWT.  
4. Implementar generación de PDF y servicio de correo stub.  
5. Crear frontend React con vistas y consumo de API.  
6. Documentar en README y guías de despliegue.

