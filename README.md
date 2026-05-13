# SmartLogix-AMR
Evaluciones FullStack III
# SmartLogix — Backend Microservicios

Sistema de gestión logística construido con arquitectura de microservicios en **Spring Boot 3** y **Java 21**. Permite registrar usuarios, gestionar pedidos y hacer seguimiento de envíos, todo protegido con autenticación JWT centralizada en un API Gateway.

---

## Arquitectura general

```
Cliente (Postman / Frontend)
        │
        ▼
┌─────────────────┐
│   API Gateway   │  :9090  ← valida JWT y enruta
└────────┬────────┘
         │
   ┌─────┼──────────┐
   ▼     ▼          ▼
┌──────┐ ┌────────┐ ┌────────┐
│Usuario│ │Pedido  │ │ Envío  │
│:8081  │ │:8082   │ │:8083   │
└──────┘ └───┬────┘ └────────┘
             │  OpenFeign
             └──────────────▶ servicioenvio
```

| Microservicio   | Puerto | Base de datos  |
|-----------------|--------|----------------|
| `apigateway`    | 9090   | —              |
| `serviciousuario` | 8081 | `usuarios_db`  |
| `serviciopedido`  | 8082 | `pedidos_db`   |
| `servicioenvio`   | 8083 | `envios_db`    |

---

## Tecnologías utilizadas

- **Java 21**
- **Spring Boot 3.5**
- **Spring Security** — seguridad en cada microservicio
- **Spring Cloud Gateway** — API Gateway con filtro JWT
- **Spring Cloud OpenFeign** — comunicación entre microservicios
- **Spring Data JPA + Hibernate** — acceso a datos
- **MySQL** — base de datos relacional
- **JWT (jjwt 0.11.5)** — autenticación stateless
- **Lombok** — reducción de código boilerplate
- **Maven** — gestión de dependencias

---

## Requisitos previos

- Java 21
- Maven 3.9+
- MySQL 8+
- (Opcional) Postman para probar la API

---

## Configuración de bases de datos

Crear las siguientes bases de datos en MySQL antes de ejecutar:

```sql
CREATE DATABASE usuarios_db;
CREATE DATABASE pedidos_db;
CREATE DATABASE envios_db;
```

> Las tablas se generan automáticamente con `spring.jpa.hibernate.ddl-auto=update`.

Cada microservicio conecta con usuario `root` sin contraseña por defecto. Ajusta según tu entorno en el `application.properties` correspondiente:

```properties
spring.datasource.password=tu_contraseña
```

---

## Ejecución del proyecto

Iniciar cada microservicio **en este orden**:

```bash
# 1. Servicio de usuarios
cd serviciousuario
./mvnw spring-boot:run

# 2. Servicio de envíos
cd servicioenvio
./mvnw spring-boot:run

# 3. Servicio de pedidos
cd serviciopedido
./mvnw spring-boot:run

# 4. API Gateway (último)
cd apigateway
./mvnw spring-boot:run
```

---

## Autenticación

El sistema usa **JWT** para autenticar usuarios. El flujo es:

1. Registrar un usuario en `/usuarios`
2. Hacer login en `/usuarios/login` → se obtiene un token JWT
3. Incluir el token en todas las requests protegidas:

```
Authorization: Bearer <token>
```

El **API Gateway** intercepta y valida el token antes de enrutar la petición al microservicio correspondiente.

> Los tokens tienen una expiración de **1 hora**.

---

## Endpoints

Todas las rutas se consumen a través del **Gateway en el puerto 9090**.

### Usuarios — `/usuarios`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/usuarios` | Registrar nuevo usuario | No |
| `GET` | `/usuarios` | Listar todos los usuarios | Sí |
| `POST` | `/usuarios/login` | Login y obtención de JWT | No |

**Ejemplo — Registro:**
```json
POST /usuarios
{
  "nombre": "Juan Pérez",
  "correo": "juan@email.com",
  "contrasena": "1234",
  "rol": "ADMIN"
}
```

**Ejemplo — Login:**
```json
POST /usuarios/login
{
  "correo": "juan@email.com",
  "contrasena": "1234"
}
// Respuesta: "eyJhbGciOiJIUzI1NiJ9..."
```

---

### Pedidos — `/pedidos`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/pedidos` | Crear un pedido (crea envío automáticamente) | Sí |
| `GET` | `/pedidos` | Listar todos los pedidos | Sí |
| `GET` | `/pedidos?cliente={nombre}` | Filtrar pedidos por cliente | Sí |
| `PUT` | `/pedidos/{id}/estado` | Actualizar estado del pedido | Sí |
| `GET` | `/pedidos/{id}/envios` | Obtener pedido con sus envíos | Sí |
| `DELETE` | `/pedidos/{id}` | Eliminar pedido y sus envíos | Sí |

**Ejemplo — Crear pedido:**
```json
POST /pedidos
{
  "cliente": "Juan Pérez",
  "producto": "Laptop",
  "cantidad": 1,
  "estado": "PENDIENTE",
  "direccion": "Av. Siempre Viva 123"
}
```

---

### Envíos — `/envios`

| Método | Ruta | Descripción | Auth |
|--------|------|-------------|------|
| `POST` | `/envios` | Crear un envío | Sí |
| `GET` | `/envios` | Listar todos los envíos | Sí |
| `GET` | `/envios/pedido/{pedidoId}` | Envíos de un pedido específico | Sí |
| `PUT` | `/envios/{id}/estado` | Actualizar estado del envío | Sí |
| `DELETE` | `/envios/pedido/{pedidoId}` | Eliminar envíos de un pedido | Sí |

---

## Estructura del proyecto

```
SmartLogix/
├── apigateway/                  # API Gateway (puerto 9090)
│   └── src/main/java/.../
│       ├── config/
│       │   ├── SecurityConfig   # Seguridad + CORS
│       │   └── CorsConfig       # Configuración CORS global
│       └── security/
│           ├── JwtUtil          # Validación de tokens
│           └── JwtFilter        # Filtro JWT por petición
│
├── serviciousuario/             # Microservicio de usuarios (puerto 8081)
│   └── src/main/java/.../
│       ├── controller/
│       │   └── UsuarioController
│       ├── service/
│       │   ├── UsuarioService
│       │   └── UsuarioServiceImpl
│       ├── model/
│       │   └── Usuario
│       ├── repository/
│       │   └── UsuarioRepository
│       ├── dto/
│       │   └── LoginRequest
│       ├── security/
│       │   └── JwtUtil          # Generación de tokens
│       └── config/
│           └── SecurityConfig
│
├── serviciopedido/              # Microservicio de pedidos (puerto 8082)
│   └── src/main/java/.../
│       ├── controller/
│       │   └── PedidoController
│       ├── service/
│       │   ├── PedidoService
│       │   └── PedidoServiceImpl
│       ├── model/
│       │   └── Pedido
│       ├── repository/
│       │   └── PedidoRepository
│       └── client/
│           └── EnvioClient      # Feign client → servicioenvio
│
└── servicioenvio/               # Microservicio de envíos (puerto 8083)
    └── src/main/java/.../
        ├── controller/
        │   └── EnvioController
        ├── service/
        │   ├── EnvioService
        │   └── EnvioServiceImpl
        ├── model/
        │   └── Envio
        └── repository/
            └── EnvioRepository
```

---

## Modelos de datos

### Usuario
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Identificador único |
| `nombre` | String | Nombre completo |
| `correo` | String | Email (usado para login) |
| `contrasena` | String | Hash BCrypt (no se expone en respuestas) |
| `rol` | String | Rol del usuario (ej: `ADMIN`, `USER`) |

### Pedido
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Identificador único |
| `cliente` | String | Nombre del cliente |
| `producto` | String | Nombre del producto |
| `cantidad` | int | Cantidad pedida |
| `estado` | String | Estado actual del pedido |
| `direccion` | String | Dirección de entrega |

### Envío
| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | Long | Identificador único |
| `pedidoId` | Long | Referencia al pedido |
| `direccion` | String | Dirección de entrega |
| `estado` | String | Estado del envío |

---

## Seguridad

- Las contraseñas se almacenan con **BCrypt**.
- El **API Gateway** es el único punto de entrada externo y valida el JWT en cada request.
- Los microservicios internos tienen seguridad deshabilitada (`permitAll`) ya que confían en que el Gateway es la capa de protección.
- El token JWT incluye el **correo** y el **rol** del usuario, que el Gateway extrae y asigna al contexto de Spring Security.

---

## Comunicación entre servicios

`serviciopedido` se comunica con `servicioenvio` mediante **OpenFeign**. Al crear o eliminar un pedido, automáticamente se crean o eliminan los envíos asociados sin intervención del cliente.

```
POST /pedidos  →  crea Pedido  →  EnvioClient.crearEnvio()  →  POST /envios
DELETE /pedidos/{id}  →  EnvioClient.eliminarEnviosPorPedido()  →  DELETE /envios/pedido/{id}
