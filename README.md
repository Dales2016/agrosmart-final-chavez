# AgroSmart — Plataforma de Comercialización Agrícola

Proyecto del Examen Final Práctico de Programación Avanzada de la Universidad de las Fuerzas Armadas ESPE.

**Estudiante:** Daniel Alfonso Chavez Tamayo  
**NRC:** 30405  
**Tecnologías principales:** Java 21, Spring Boot 4.1.0, Spring WebFlux, Project Reactor, JPA/Hibernate, PostgreSQL, Docker Compose y LangChain4j.

## Descripción

AgroSmart es una API reactiva que administra productos agrícolas almacenados en PostgreSQL. La aplicación publica únicamente productos comercializables y puede solicitar una frase publicitaria a un modelo de lenguaje.

La persistencia mediante JPA y la llamada a la IA son operaciones bloqueantes. Para impedir que bloqueen el *event loop* de Netty, ambas se ejecutan mediante `Mono.fromCallable(...)` y `Schedulers.boundedElastic()`.

## Semilla personal

Los dos últimos dígitos de mi cédula son `68`. A partir de ellos obtuve los parámetros del proyecto:

| Parámetro | Obtención | Valor utilizado |
|---|---|---|
| `NN` | Dos últimos dígitos de la cédula | `68` |
| Tabla | `tbl_productos_base_` + `NN` | `tbl_productos_base_68` |
| Puerto | `81` + `NN` | `8168` |
| Categoría | Último dígito `8` | Quinua |
| Audiencia | Categoría Quinua | tiendas de alimentación saludable |
| Base de datos | Valor fijo del examen | `agrosmart_db` |
| Siembra | Valor fijo del examen | 3 productos válidos y 2 inválidos |

La tabla y el puerto terminan en los mismos dos dígitos, por lo que la semilla cumple la autocomprobación solicitada.

## Arquitectura

```text
Petición HTTP / Netty
        |
        v
AgroSmartController
        |
        v
ProductoService
   |                 |
   v                 v
ProductoRepository   AgroSmartAIService
JPA/Hibernate        LangChain4j
   |                 |
   +---- boundedElastic ----+
        |
        v
PostgreSQL / proveedor de IA
```

La estructura principal del código es:

```text
src/main/java/ec/edu/espe/agrosmart/
├── controller/AgroSmartController.java
├── domain/Producto.java
├── domain/ProductoFilters.java
├── entity/ProductoEntity.java
├── exception/ProductoNoEncontradoException.java
├── mapper/ProductoMapper.java
├── repository/ProductoRepository.java
├── service/AgroSmartAIService.java
├── service/ProductoService.java
└── AgrosmartApplication.java
```

`ProductoEntity` es mutable porque Hibernate necesita construir y completar la entidad. En cambio, `Producto` representa el dominio y es inmutable: no tiene setters y realiza copias defensivas de la lista de correos tanto al recibirla como al devolverla.

## Requisitos

- Java 21.
- Docker Desktop con Docker Compose.
- Git.
- PowerShell o una terminal equivalente.

## Configuración y ejecución

### 1. Clonar el repositorio

```powershell
git clone https://github.com/Dales2016/agrosmart-final-chavez.git
cd agrosmart-final-chavez
```

### 2. Levantar PostgreSQL

```powershell
docker compose up -d
```

El archivo `compose.yaml` crea la base `agrosmart_db`, el usuario `agrosmart` y publica PostgreSQL en el puerto local `5433`. Se utilizó ese puerto porque el `5432` ya estaba ocupado por otro proyecto; dentro del contenedor PostgreSQL continúa escuchando en `5432`.

Para comprobar el contenedor:

```powershell
docker ps --filter "name=agrosmart-postgres"
```

### 3. Configurar el proveedor de IA

La aplicación contiene el valor de demostración solicitado en el examen. Para utilizar una clave real sin guardarla en Git, se puede definir una variable de entorno en PowerShell:

```powershell
$env:LANGCHAIN4J_OPEN_AI_CHAT_MODEL_API_KEY="TU_CLAVE"
```

Si no existe una credencial válida, la aplicación permanece disponible y `onErrorResume` devuelve el mensaje de respaldo comprobado durante las pruebas.

### 4. Ejecutar la aplicación

```powershell
.\mvnw.cmd spring-boot:run
```

La aplicación activa el perfil `prod` y queda disponible en:

```text
http://localhost:8168
```

El esquema se genera mediante Hibernate con `spring.jpa.hibernate.ddl-auto=update`. La siembra es idempotente: solo inserta los cinco productos cuando el repositorio está vacío.

### 5. Ejecutar las pruebas

```powershell
.\mvnw.cmd test
```

Resultado verificado: **11 pruebas ejecutadas, 0 fallos, 0 errores y 0 omitidas**.

### 6. Detener PostgreSQL

```powershell
docker compose stop
```

## Endpoints

| Método | Ruta | Descripción | Respuesta esperada |
|---|---|---|---|
| GET | `/api/productos` | Obtiene los productos comercializables | `200 OK` y un `Flux<Producto>` con 3 elementos |
| GET | `/api/productos/{id}` | Busca un producto por identificador | `200 OK` o `404 Not Found` |
| GET | `/api/agrosmart/publicidad` | Genera publicidad según producto y audiencia | `200 OK` con texto generado o mensaje de respaldo |

## Ejemplos reales con curl

### Productos comercializables

```powershell
curl.exe http://localhost:8168/api/productos
```

Salida obtenida:

```json
[
  {
    "id": 1,
    "nombre": "QUINUA ORGÁNICA DE ALTURA",
    "categoria": "Quinua",
    "precioUsd": 125.50,
    "correosNotificacion": ["ventas@agrosmart.ec", "exportaciones@agrosmart.ec"]
  },
  {
    "id": 2,
    "nombre": "QUINUA ROJA PREMIUM",
    "categoria": "Quinua",
    "precioUsd": 98.75,
    "correosNotificacion": ["comercial@agrosmart.ec"]
  },
  {
    "id": 3,
    "nombre": "QUINUA NEGRA ANDINA",
    "categoria": "Quinua",
    "precioUsd": 110.00,
    "correosNotificacion": ["pedidos@agrosmart.ec"]
  }
]
```

Los otros dos productos sembrados son descartados: uno tiene precio `0.00` y el otro no posee correos de notificación.

### Producto existente

```powershell
curl.exe http://localhost:8168/api/productos/1
```

Salida obtenida:

```json
{
  "id": 1,
  "nombre": "Quinua orgánica de altura",
  "categoria": "Quinua",
  "precioUsd": 125.50,
  "correosNotificacion": ["ventas@agrosmart.ec", "exportaciones@agrosmart.ec"]
}
```

### Producto inexistente

```powershell
curl.exe -i http://localhost:8168/api/productos/9999
```

Salida obtenida:

```text
HTTP/1.1 404 Not Found
Content-Type: application/json

{"timestamp":"2026-07-31T15:39:21.083Z","path":"/api/productos/9999","status":404,"error":"Not Found","requestId":"484b1692-3"}
```

### Publicidad con IA

```powershell
curl.exe "http://localhost:8168/api/agrosmart/publicidad?producto=Quinua%20organica%20de%20altura&audiencia=tiendas%20de%20alimentacion%20saludable"
```

Durante la ejecución, el proveedor rechazó la credencial de demostración. El flujo capturó el error real y respondió:

```text
Publicidad no disponible en este momento (AuthenticationException)
```

Este resultado comprueba que el fallo del proveedor no finaliza la API con un error no controlado.

## Operadores reactivos utilizados

| Operador | Uso en la solución | Justificación |
|---|---|---|
| `Mono.fromCallable(...)` | Consulta JPA y llamada al servicio de IA | Difiere la operación bloqueante hasta que existe una suscripción y permite capturar sus errores dentro del flujo. |
| `subscribeOn(Schedulers.boundedElastic())` | Después de cada `fromCallable` bloqueante | Traslada JPA y la llamada HTTP de IA a un conjunto de hilos apropiado para trabajo bloqueante. |
| `flatMapMany(Flux::fromIterable)` | Resultado de `repository.findAll()` | Convierte el `Mono<List<ProductoEntity>>` en un `Flux<ProductoEntity>` para procesar cada registro. |
| `map(...)` | Mapeo de entidad a dominio y transformación a mayúsculas | Aplica transformaciones y devuelve elementos nuevos sin mutar el producto recibido. |
| `filter(ProductoFilters.IS_VALID)` | Flujo de productos | Conserva únicamente productos con precio mayor que cero y al menos un correo. |
| `doOnNext(ProductoFilters.LOG_PRODUCTO)` | Trazabilidad del flujo | Ejecuta un efecto lateral de registro sin cambiar el elemento emitido. |
| `defaultIfEmpty(PRODUCTO_GENERICO)` | Listado comercializable | Si todos los productos son descartados, emite un producto de respaldo en lugar de dejar el `Flux` vacío. |
| `switchIfEmpty(Mono.error(...))` | Búsqueda por identificador | Sustituye el `Mono` vacío por un error `ProductoNoEncontradoException`, que WebFlux convierte en 404. |
| `timeout(Duration.ofSeconds(30))` | Generación de publicidad | Evita que una respuesta lenta mantenga el flujo abierto indefinidamente. |
| `onErrorResume(...)` | Generación de publicidad | Convierte errores de autenticación, cuota, conexión o tiempo en una respuesta controlada. |

`defaultIfEmpty` y `switchIfEmpty` no son intercambiables en este caso. El listado necesita un valor genérico cuando no existen productos comercializables; la búsqueda por identificador debe informar que el recurso solicitado no existe.

## Puente entre código bloqueante y WebFlux

WebFlux atiende solicitudes mediante un número reducido de hilos del *event loop* de Netty. JPA/Hibernate y la interfaz de LangChain4j utilizada en este proyecto son APIs síncronas: el hilo que las ejecuta espera hasta recibir el resultado.

El puente se implementó de la siguiente manera:

```java
Mono.fromCallable(() -> repository.findAll())
    .subscribeOn(Schedulers.boundedElastic());
```

El mismo patrón se aplicó a `aiService.generarPublicidad(...)`. `fromCallable` difiere la ejecución y `subscribeOn(boundedElastic())` hace que la espera ocurra fuera del *event loop*. Si se eliminara `subscribeOn`, la consulta o la llamada externa podrían ejecutarse en un hilo de Netty, bloquearlo y reducir la capacidad de atender solicitudes concurrentes.

No se utiliza `block()`, `blockFirst()`, `blockLast()` ni `toStream()` en las capas públicas del servicio o del controlador.

## Pruebas unitarias

Las pruebas están aisladas de PostgreSQL e internet. `ProductoRepository` y `AgroSmartAIService` se reemplazan por *mocks*, lo que permite verificar de forma rápida y determinista:

- Valores del modelo inmutable.
- Copias defensivas de entrada y salida.
- Predicado de validación y transformación a mayúsculas.
- Emisión de los tres productos válidos.
- Error para un identificador inexistente.
- Camino exitoso y recuperación ante fallo del proveedor de IA.

Los flujos se verifican con `StepVerifier` y finalizan con `verifyComplete()` o `verify()`.

## Evidencias

| Evidencia | Archivo |
|---|---|
| Commit de identidad | [00_commit_identidad.png](docs/evidencias/00_commit_identidad.png) |
| Arranque con perfil `prod` y puerto 8168 | [01_arranque_perfil_prod_puerto_8168.png](docs/evidencias/01_arranque_perfil_prod_puerto_8168.png) |
| Estructura de `tbl_productos_base_68` | [02_estructura_tbl_productos_base_68.png](docs/evidencias/02_estructura_tbl_productos_base_68.png) |
| Cinco productos sembrados | [03_cinco_productos_quinua.png](docs/evidencias/03_cinco_productos_quinua.png) |
| Cuatro solicitudes curl | [04_cuatro_curl_api_agrosmart.png](docs/evidencias/04_cuatro_curl_api_agrosmart.png) |
| Tres productos comercializables | [04_curl_tres_productos_comercializables.png](docs/evidencias/04_curl_tres_productos_comercializables.png) |
| Producto existente y error 404 | [05_curl_producto_id_y_error_404.png](docs/evidencias/05_curl_producto_id_y_error_404.png) |
| Publicidad y recuperación del error de IA | [06_curl_publicidad_ia.png](docs/evidencias/06_curl_publicidad_ia.png) |
| Once pruebas unitarias en verde | [07_pruebas_unitarias_11_en_verde.png](docs/evidencias/07_pruebas_unitarias_11_en_verde.png) |

## Flujo de trabajo con Git

El desarrollo se realizó en ramas independientes por fase y se integró a `main` mediante Pull Requests sin *squash*. Esto conserva los commits semánticos y permite seguir la evolución del proyecto desde la configuración hasta las pruebas y la documentación.

## Autor

**Daniel Alfonso Chavez Tamayo**  
Universidad de las Fuerzas Armadas ESPE — Programación Avanzada, NRC 30405.
