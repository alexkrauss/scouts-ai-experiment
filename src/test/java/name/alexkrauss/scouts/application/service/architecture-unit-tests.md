# Unit tests for the service layer.

* We use mocked repositories for the service layer tests, never the actual repositories.
* They are not using Mockito but have an explicit implementation in the codebase in the package dbmock.
* The tests are annotated with `@ActiveProfiles("db-mock")` to use the mocked repositories.
* In test data classes (e.g. `ScoutsTestData`, `EventsTestData`), we can define static test objects as simple java objects that can be used for unit tests.
* Assertions should be easily reviewable. We use assert4j.
* Tests should be documented just like production code.