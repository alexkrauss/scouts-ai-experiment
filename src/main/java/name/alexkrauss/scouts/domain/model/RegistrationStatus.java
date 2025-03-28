package name.alexkrauss.scouts.domain.model;

/**
 * Represents the possible states of a scout's registration for an event.
 */
public enum RegistrationStatus {
    /**
     * The registration has been submitted but not yet confirmed.
     */
    PENDING,

    /**
     * The registration has been approved and confirmed.
     */
    CONFIRMED,

    /**
     * The registration has been cancelled and is no longer valid.
     */
    CANCELLED
}