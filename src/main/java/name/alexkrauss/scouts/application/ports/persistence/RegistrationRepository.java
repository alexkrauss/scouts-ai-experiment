package name.alexkrauss.scouts.application.ports.persistence;

import name.alexkrauss.scouts.domain.model.Registration;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing scout event registrations in the persistence layer.
 */
public interface RegistrationRepository {

    /**
     * Creates a new registration record.
     *
     * @param registration the registration to create
     * @return the created registration with id set
     */
    Registration create(Registration registration);

    /**
     * Updates an existing registration record.
     *
     * @param registration the registration to update
     * @return the updated registration
     * @throws OptimisticLockingFailureException if the version number has changed
     */
    Registration update(Registration registration);

    /**
     * Deletes a registration by its id.
     *
     * @param id the id of the registration to delete
     */
    void delete(long id);

    /**
     * Finds a registration by its id.
     *
     * @param id the id of the registration to find
     * @return the registration if found, empty optional otherwise
     */
    Optional<Registration> findById(long id);

    /**
     * Finds all registrations for a specific event.
     *
     * @param eventId the id of the event
     * @return list of registrations for the event
     */
    List<Registration> findByEventId(long eventId);

    /**
     * Finds all registrations for a specific scout.
     *
     * @param scoutId the id of the scout
     * @return list of registrations for the scout
     */
    List<Registration> findByScoutId(long scoutId);

}