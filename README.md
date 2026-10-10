# ucopet-health-processor

Procesador **headless** de Salud para UCOPET (arquitectura CQRS con RabbitMQ).
Atiende los mensajes de: Atenciones (`petcare`), Ordenes (`order`), Citas (`appointment`),
Especialidades (`specialtie`), Procedimientos (`procedure`) y Espacios (`space`).
Cada modulo tiene **dos colas**: `save` (crear + actualizar) y `deactivate` = 12 colas. Consume de RabbitMQ y escribe en la BD
(consumidor -> service -> repository -> BD). No expone controllers.

## Colas (12)

6 modulos x 2 operaciones: `petcare`, `order`, `appointment`, `specialty`, `procedure`, `space`,
cada uno con `save` y `deactivate`. Formato: `ucopet.health.<modulo>.<operacion>.<ex|qu|rk>`.

## Estado

- [x] Proyecto base + properties (`health.properties`)
- [ ] Declaracion de exchanges / colas / bindings
- [ ] Consumidores (`@RabbitListener`)
- [ ] Service + Repository por modulo

## Configuracion

| Archivo | Contenido |
|---|---|
| `application.properties` | Datasource (misma BD que `ucopetapi`) e import de `health.properties` |
| `health.properties` | Conexion a RabbitMQ y nombres de exchange/cola/routing key |

Variables de entorno:

| Variable | Obligatoria | Por defecto |
|---|---|---|
| `DB_PASSWORD` | **si** | — |
| `DB_URL` / `DB_USER` | no | `jdbc:postgresql://localhost:5432/ucopetdb` / `postgres` |
| `RABBIT_HOST` / `RABBIT_PORT` | no | `localhost` / `5672` |
| `RABBIT_USER` / `RABBIT_PASSWORD` | no | `ucopet` / `ucopet_dev` (solo desarrollo) |

## Correr

Requiere Postgres y RabbitMQ arriba (ver `docker-compose.yml` de `ucopetapi`).

```bash
# PowerShell
$env:DB_PASSWORD="<la de tu Postgres>"
./gradlew bootRun
```

Al arrancar debe imprimir `[HEALTH] modulos cargados: 6` y 12 lineas `[HEALTH] <modulo> save|deactivate -> exchange=... queue=... routingKey=...`.
