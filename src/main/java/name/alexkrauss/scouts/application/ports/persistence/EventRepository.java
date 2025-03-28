package name.alexkrauss.scouts.application.ports.persistence;

import name.alexkrauss.scouts.domain.model.Event;
import org.springframework.dao.OptimisticLockingFailureException;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing events in the persistence layer.
 */
public interface EventRepository {

    /**
     * Creates a new event record.
     *
     * @param event the event to create
     * @return the created event with id set
     */
    Event create(Event event);

    /**
     * Updates an existing event record.
     *
     * @param event the event to update
     * @return the updated event
     * @throws OptimisticLockingFailureException if the version number has changed
     */
    Event update(Event event);

    /**
     * Deletes an event by its id.
     *
     * @param id the id of the event to delete
     */
    void delete(long id);

    /**
     * Finds an event by its id.
     *
     * @param id the id of the event to find
     * @return the event if found, empty optional otherwise
     */
    Optional<Event> findById(long id);

    /**
     * Returns all events.
     *
     * @return list of all events
     */
    List<Event> findAll();

    /**
     * Finds all events that a specific group can participate in.
     * This includes events where the group is explicitly assigned, 
     * as well as events with no group restrictions.
     *
     * @param groupId the id of the group
     * @return list of events available to the group
     */
    List<Event> findEventsByGroupId(long groupId);
}