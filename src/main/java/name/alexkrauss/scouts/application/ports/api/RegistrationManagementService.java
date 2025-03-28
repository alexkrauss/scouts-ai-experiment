package name.alexkrauss.scouts.application.ports.api;

import name.alexkrauss.scouts.domain.model.Registration;

import java.util.List;

/**
 * Service for managing registrations of scouts to events.
 */
public interface RegistrationManagementService {

    /**
     * Creates a new registration.
     *
     * @param registration The registration to create.
     * @return The created registration.
     */
    Registration createRegistration(Registration registration);

    /**
     * Updates an existing registration.
     * Note: The scout and event of a registration cannot be changed.
     * To change these, delete the existing registration and create a new one.
     *
     * @param registration The registration to update.
     * @return The updated registration.
     * @throws IllegalArgumentException if the registration doesn't exist or has been modified concurrently.
     */
    Registration updateRegistration(Registration registration);

    /**
     * Deletes a registration.
     *
     * @param registrationId The ID of the registration to delete.
     * @throws IllegalArgumentException if the registration doesn't exist.
     */
    void deleteRegistration(Long registrationId);

    /**
     * Gets a registration by its ID.
     *
     * @param registrationId The ID of the registration.
     * @return The registration.
     * @throws IllegalArgumentException if the registration doesn't exist.
     */
    Registration getRegistration(Long registrationId);

    /**
     * Gets all registrations for a given event.
     *
     * @param eventId The ID of the event.
     * @return A list of registrations for the event.
     */
    List<Registration> getRegistrationsByEvent(Long eventId);

    /**
     * Gets all registrations for a given scout.
     *
     * @param scoutId The ID of the scout.
     * @return A list of registrations for the scout.
     */
    List<Registration> getRegistrationsByScout(Long scoutId);
}