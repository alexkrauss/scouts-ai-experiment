# Persistence Implementation

This package contains the jOOQ implementation of the persistence port. For each repository, it contains an implementation
class. 

Naming convention: The name of repository implementations is `DbFooRepository` if it implements the interface `FooRepository`.

The implementation classes are annotated with the `@Repository` annotation from Spring.

We consider repository implementations as straight-forward transformation code. It should convert domain entities to the
respective jooq records and queries.

We assume a Postgresql database and make no effort to formulate queries in a portable way.
