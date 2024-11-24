# Domain modeling

We use the following conventions for domain modeling, roughly following the
DDD approach:

## Entities

Entities are objects that have an identity and a lifecycle. They are mutable.
They are modelled in Java using the Lombok `@Data` annotation.

Example:

```java
/**
 * A person representing ...(describe the meaning in the domain).
 */
@Data
@Builder(toBuilder = true)
public class Person {
    Long id;
    long version; 

    /**
     * Full name. 
     */
    @NonNull
    String name;
    
    /**
     * Date of birth, which can be used to calculate the age.
     */
    @NonNull
    LocalDate birthDate;
}
```

There are no other annotations on the entity classes, in particular none referring to
persistence or serialization.

Entities always have the following special attributes:

* `id`: The unique identifier of the entity. It is of type `Long`.
* `version`: The version of the entity. It is of type `Long`. It is used
    for optimistic locking.

The class itself and all fields (except for id and version) must be
commented in a helpful way. Just repeating the field name is not helpful.

## Value Objects

Value objects are objects that have no identity and are immutable.
They are modelled in Java using records.

Example:

```java
import lombok.NonNull;
import lombok.Builder;

/**
 * An address.
 * @param street
 * @param city
 * @param zipCode
 */
@Builder(toBuilder = true)
public record Address(
    @NonNull
    String street,
    
    @NonNull
    String city,
    
    @NonNull
    String zipCode
) {}
```

## Constraints

We annotate constraints on fields as follows:

* `@NonNull`: (lombok.NonNull) The field must not be null. This annotation
  ensures that a null check is generated.
* Annotations from JEE validation. These are checked at runtime.
  Examples are `@Size`, `@Min`, `@Max`, `@Pattern`, `@Email`.

## Special types

Many things can be modeled as strings, but actually carry extra invariants, and it is helpful
to show this in the type system, e.g. 'Email'. In this case, we define a value type that
models the extra invariants.





