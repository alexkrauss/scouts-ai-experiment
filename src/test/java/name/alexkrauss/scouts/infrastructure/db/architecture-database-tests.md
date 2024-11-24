# Tests for database access

Repository implementations are tested using the following conventions:

* We test for the optimistic locking cases, to make sure that it is detected properly.
* Tests may not assume anything about the database content. In particular, they must tolerate
  other, unrelated records. For example, a test for the 'findAll' method must tolerate that the
  result contains records from other tests.

* When tests require specific data, they must take care of creating it. Cleaning up everything
  is not strictly required (as it is hard to ensure anyway in the presence of exceptions). But
  leaving behind large amounts of data is discouraged.