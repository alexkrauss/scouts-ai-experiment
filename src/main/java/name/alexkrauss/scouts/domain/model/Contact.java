package name.alexkrauss.scouts.domain.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.NonNull;

/**
 * A contact represents a person who can be reached regarding a scout, typically a parent or guardian.
 *
 * @param name Full name of the contact person
 * @param phoneNumber Phone number for immediate contact in case of emergencies
 * @param email Email address for regular communications and notifications
 * @param relationship Describes the relationship between the contact and the scout (e.g. mother, father, guardian)
 */
@Builder(toBuilder = true)
public record Contact(
    @NonNull
    @NotEmpty
    String name,

    @NonNull
    @NotEmpty
    String phoneNumber,

    @NonNull
    @Email
    String email,

    @NonNull
    String relationship
) {}