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
- Java 21, Spring Boot 3.4.0, jOOQ, Flyway, Postgres
- Contract-first API design with OpenAPI

## Architecture conventions
- Observe all architecture conventions in architecture*.md files in the codebase. There are files for different aspects of the architecture.

## Workflow
- Always proceed in the following order:
  1. Define data model, double checking to conform to domain.md precisely. Then ask for user review.
  2. Define APIs of the respective components throughout the architecture. Ask for user review for every part that has high influence on the following code.
  3. Write tests, focusing on the properties that we care about on each layer.
  4. Write the implementation and make the tests pass
- When implementing the persistence layer,
  1. Start with the repository interfaces.
  2. Define the DB migration, so that the schema is in place.
  3. Generate the jOOQ classes.
  4. Implement the repository.
  5. Also generate a mock implementation of the repository, based on in-memory data structures, to be used by unit tests.
- After each step, the build must work, including all tests. If this is not the case, never proceed to the next step. Instead, analyze and fix the problem. If you cannot fix it, ask for help with a concise summary of the problem.

