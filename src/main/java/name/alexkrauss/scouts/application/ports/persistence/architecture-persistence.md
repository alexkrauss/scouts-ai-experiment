# Persistence port

This port is used to access the database. The corresponding interfaces are always called "Repository",
and there is typically one repository per entity.

## CRUD interface conventions

For typical CRUD operations, the following conventions are used:

* Method "create" creates a record, given an entity, returning the new entity (with the id set).
* Method "update" updates a record, given an entity, returning the updated entity.
* Method "delete" deletes a record, given just the id.
* Method "findById" finds a record by id, returning the entity. It returns an optional.
* Method "findAll" finds all records, returning a list of entities.

There may be further methods, e.g. for finding by specific attributes or criteria.

Like all interfaces, the repositories have full javadoc.

## Optimistic locking

All repositories use optimistic locking, using the version field.

When a locking error occurs, an OptimisticLockingFailureException is thrown, which is the Spring default.

This exception should also be documented in the javadoc of the repository method.

## Other errors

All other errors are wrapped in a DataAccessException, which is the Spring default. These need not be
documented, since they can always occur and are typically not handled explicitly.
