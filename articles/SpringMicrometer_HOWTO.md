# Spring Boot Actuator & Micrometer Metrics in WebAPI

## Overview

WebAPI uses **Spring Boot Actuator** and **Micrometer** to expose application metrics for performance monitoring and diagnostics. This document describes the setup and how to access the metrics.

### What is Spring Boot Actuator?

Spring Boot Actuator provides production-ready features for monitoring and managing your application. It exposes a set of HTTP endpoints under `/actuator/` that give insight into the running application: health status, environment properties, cache statistics, loaded beans, and — most relevant here — **application metrics**.

Actuator is included via `spring-boot-starter-actuator` in `pom.xml`. By default, only the `/actuator/health` endpoint is exposed over HTTP. Additional endpoints must be explicitly enabled in `application.yaml`.

### What is Micrometer?

Micrometer is the metrics facade that ships with Spring Boot Actuator (similar to how SLF4J is a facade for logging). It provides a vendor-neutral API for recording metrics — timers, counters, gauges, distribution summaries — and can export them to many backends (Prometheus, Grafana, Datadog, CloudWatch, etc.).

By default, Micrometer stores metrics in an in-memory registry and serves them via the Actuator `/actuator/metrics` endpoint. No external backend is required to get started.

---

## What We Instrument

### `security.user_authorizations.build`

This timer measures the time to build a `UserAuthorizations` object from the database. This is the **cache-miss** path — when a user's authorization info is not in the JCache `authorizationInfo` cache, `EntityAccessService.buildUserAuthorizations()` runs and:

1. Queries global wildcard permissions from `sec_permission` via user roles
2. Queries per-entity access grants from `sec_cohort_definition` and `sec_source`
3. Queries owned entity IDs from `cohort_definition` (by `created_by_id`)
4. Assembles the complete `UserAuthorizations` object

The method is annotated with `@Timed`:

```java
@Timed(value = "security.user_authorizations.build",
    description = "Time to build UserAuthorizations (cache miss)")
public UserAuthorizations buildUserAuthorizations(Long userId) { ... }
```

The `@Timed` annotation is processed by a `TimedAspect` bean registered in `MetricsConfig.java`:

```java
@Configuration
public class MetricsConfig {
  @Bean
  public TimedAspect timedAspect(MeterRegistry registry) {
    return new TimedAspect(registry);
  }
}
```

> **Note:** Without `TimedAspect` registered as a Spring bean, `@Timed` annotations on arbitrary beans are ignored. Spring Boot auto-configures `@Timed` for web request handlers, but not for service-layer methods.

---

## Logging Output

In addition to Micrometer metrics, `EntityAccessService` logs a DEBUG-level message on each cache miss:

```
Built UserAuthorizations for userId=1 in 12ms (permissions=5, cohortDefs=3, sources=2)
```

To see this output, set the log level for the class in `application.yaml`:

```yaml
logging:
  level:
    org.ohdsi.webapi.security.authz.access.EntityAccessService: DEBUG
```

Or to enable DEBUG for all authorization code:

```yaml
logging:
  level:
    org.ohdsi.webapi.security.authz: DEBUG
```

---

## Enabling Actuator Endpoints

By default, Spring Boot only exposes the `/actuator/health` endpoint over HTTP. To enable the metrics and cache endpoints, add the following to `application.yaml`:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,caches
```

### Available Endpoints

| Endpoint | Description |
|---|---|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application info (build number, version, etc.) |
| `/actuator/metrics` | Lists all available metric names |
| `/actuator/metrics/{metric.name}` | Detailed stats for a specific metric |
| `/actuator/caches` | Lists all active caches and their statistics |

### Security Considerations

Actuator endpoints can expose sensitive information. In production, you should either:

- Restrict access via Spring Security (e.g., require an admin role)
- Run actuator on a separate management port:

```yaml
management:
  server:
    port: 9090  # separate from the main app port
```

- Or limit exposure to only safe endpoints:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,metrics
```

---

## Accessing Metrics in the Browser

Once endpoints are enabled, you can browse metrics directly.

### List all metrics

```
GET http://localhost:8080/actuator/metrics
```

Returns a JSON list of all registered metric names:

```json
{
  "names": [
    "security.user_authorizations.build",
    "cache.gets",
    "cache.puts",
    "jvm.memory.used",
    ...
  ]
}
```

### View the UserAuthorizations build timer

```
GET http://localhost:8080/actuator/metrics/security.user_authorizations.build
```

Returns:

```json
{
  "name": "security.user_authorizations.build",
  "description": "Time to build UserAuthorizations (cache miss)",
  "baseUnit": "seconds",
  "measurements": [
    { "statistic": "COUNT", "value": 42.0 },
    { "statistic": "TOTAL_TIME", "value": 1.256 },
    { "statistic": "MAX", "value": 0.087 }
  ],
  "availableTags": [
    { "tag": "class", "values": ["org.ohdsi.webapi.security.authz.access.EntityAccessService"] },
    { "tag": "method", "values": ["buildUserAuthorizations"] },
    { "tag": "exception", "values": ["none"] }
  ]
}
```

Key statistics:
- **COUNT** — how many cache misses have occurred
- **TOTAL_TIME** — cumulative seconds spent building authorizations
- **MAX** — the slowest single invocation (resets periodically)

You can derive the **average** as `TOTAL_TIME / COUNT`.

### View JCache hit/miss rates

Spring Boot auto-instruments JCache caches when Micrometer is present (and `statisticsEnabled=true`, which is already configured for the `authorizationInfo` cache):

```
GET http://localhost:8080/actuator/metrics/cache.gets?tag=cache:authorizationInfo&tag=result:hit
GET http://localhost:8080/actuator/metrics/cache.gets?tag=cache:authorizationInfo&tag=result:miss
```

A high miss rate means the build timer fires frequently — worth investigating whether cache eviction is too aggressive.

---

## Adding New Timers

To instrument another method, annotate it with `@Timed`:

```java
import io.micrometer.core.annotation.Timed;

@Timed(value = "my.custom.timer", description = "Description of what this measures")
public MyResult myExpensiveMethod() { ... }
```

The method must be on a Spring-managed bean (so the AOP proxy wraps it). The timer will automatically appear at `/actuator/metrics/my.custom.timer`.

---

## Exporting to External Systems (Future)

Micrometer supports exporting metrics to external monitoring systems by adding a registry dependency. For example, to export to Prometheus:

```xml
<dependency>
  <groupId>io.micrometer</groupId>
  <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

This would expose a `/actuator/prometheus` endpoint in Prometheus exposition format, ready for scraping. Similar registries exist for Datadog, CloudWatch, Graphite, InfluxDB, and others. No code changes are needed — just add the dependency and configure credentials in `application.yaml`.
