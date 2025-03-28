# CLAUDE.md - Scout AI Experiment Helper

## Build/Test Commands
- Build: `./gradlew build`
- Run tests: `./gradlew test`
- Run single test: `./gradlew test --tests "name.alexkrauss.scouts.package.TestClassName.testMethodName"`
- Generate API code: `./gradlew openApiGenerate`
- Database migration: `./gradlew flywayMigrate`
- Generate jOOQ classes: `./gradlew generateJooq`

## Code Style Guidelines
- **Architecture**: Hexagonal/Onion architecture (enforced by ArchUnit)
- **Domain Models**: Use Lombok `@Data` + `@Builder(toBuilder = true)`
- **Non-nullability**: Use `@NonNull`, initialize collections to empty
- **Naming**: Repository interfaces end with "Repository", implementations start with "Db"
- **Dependencies**: application → domain, infrastructure → application/domain
- **Testing**: Use `@ActiveProfiles("db-mock")` for tests with mocked DB

## Project Structure
- **domain/model**: Core business entities
- **application/ports**: Service interfaces and repository contracts
- **infrastructure/db**: jOOQ-based database implementations
- **infrastructure/rest**: REST controllers and API endpoints

## Technology Stack
- Java 21, Spring Boot 3.4.0, jOOQ, Flyway, PostgreSQL/H2
- Contract-first API design with OpenAPI