package name.alexkrauss.scouts.application.ports.api;

import name.alexkrauss.scouts.domain.model.Event;

import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing events.
 * Provides operations for creating, retrieving, and updating events,
 * as well as managing scout participation.
 */
public interface EventManagementService {
    /**
     * Creates a new event.
     * 
     * @param event the event to create
     * @return the created event with assigned id
     */
    Event createEvent(Event event);

    /**
     * Retrieves an event by its id.
     * 
     * @param id the event id
     * @return the event if found, empty otherwise
     */
    Optional<Event> getEvent(Long id);

    /**
     * Retrieves all events.
     * 
     * @return list of all events
     */
    List<Event> getAllEvents();

    /**
     * Updates an existing event.
     * 
     * @param event the event with updated information
     * @return the updated event
     */
    Event updateEvent(Event event);

    /**
     * Deletes an event by its id.
     * 
     * @param id the id of the event to delete
     */
    void deleteEvent(Long id);

    /**
     * Assigns a group to an event, making it eligible for participation.
     * 
     * @param eventId the id of the event
     * @param groupId the id of the group to assign
     * @return the updated event
     * @throws IllegalArgumentException if the event or group does not exist
     */
    Event assignGroupToEvent(Long eventId, Long groupId);

    /**
     * Removes a group from an event, making it ineligible for participation.
     * 
     * @param eventId the id of the event
     * @param groupId the id of the group to remove
     * @return the updated event
     * @throws IllegalArgumentException if the event or group does not exist
     */
    Event removeGroupFromEvent(Long eventId, Long groupId);

    /**
     * Gets all events that a specific group can participate in.
     * 
     * @param groupId the id of the group
     * @return list of events available to the group
     */
    List<Event> getEventsForGroup(Long groupId);

}