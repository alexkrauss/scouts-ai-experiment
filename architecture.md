# Architecture decisions

This is a web app backend. We record technical decisions here.
Domain information can be found in the file domain.md.

Note that each subfolder may have its own architecture-*.md file,
describing the architecture of that subfolder and the code patterns
used there.

## Frameworks and Libraries

### Used frameworks and libraries

* Java 21
* Spring Boot
* jOOQ for data access
* Flyway for database migrations

#### Details on Spring Boot

We use the following Spring Boot modules:
* Spring Boot Web
* jOOQ integration
* Spring Boot Actuator
* Spring Boot Starter Test

## Persistence

We use PostgreSQL as the primary database.
For testing, we use an in-memory H2 database.

## Build

The build system is Gradle, using the gradle wrapper to ensure
the correct Gradle version.

The result of the build is a fat jar, as well as a docker image.

## APIs

* The APIs are given using an OpenAPI specification. We follow
  a contract-first approach where code is generated from the API
  spec using the OpenAPI generator.

## Observability

For logging, we use Spring-boot's defaults.

We also collect metrics using Spring Boot Actuator, using the
micrometer API. We expose a prometheus endpoint.


## Testing Strategy

* Unit tests with JUnit 5
* Integration tests with Spring Boot Test
* API tests with REST Assured
* Code coverage tracked with JaCoCo


## Internal service architecture.

We use hexagonal architecture. The layout is as follows

    root
    |-- domain
        |-- model
    |-- application
        |-- ports
            |-- persistence
        |-- service
    |-- infrastructure
        |-- db
        |-- rest

Only the following dependencies are allowed:
* application -> domain
* infrastructure -> application
* infrastructure -> domain

We do not distinguish between primary/inbound and secondary/outbound ports.

