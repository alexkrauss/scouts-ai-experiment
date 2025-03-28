package name.alexkrauss.scouts.domain.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.time.LocalDateTime;

/**
 * A registration represents a scout's intent to participate in an event.
 * It tracks the registration status and related information.
 */
@Data
@Builder(toBuilder = true)
public class Registration {
    Long id;
    long version;

    /**
     * The scout who is registering for the event.
     */
    @NonNull
    @NotNull
    Scout scout;

    /**
     * The event the scout is registering for.
     */
    @NonNull
    @NotNull
    Event event;

    /**
     * Additional notes regarding the registration, such as special requirements.
     */
    @NonNull
    String note;

    /**
     * The current status of the registration.
     */
    @NonNull
    @NotNull
    RegistrationStatus status;

    /**
     * The timestamp when the registration was created.
     */
    @NonNull
    @NotNull
    LocalDateTime registrationDate;

    /**
     * The account that created this registration.
     * This would typically be a parent's account.
     */
    @NonNull
    String accountId; // Using String for now, can be replaced with Account entity later
}