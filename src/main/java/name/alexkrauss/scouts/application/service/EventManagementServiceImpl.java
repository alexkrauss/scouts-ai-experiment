package name.alexkrauss.scouts.application.service;

import name.alexkrauss.scouts.application.ports.api.EventManagementService;
import name.alexkrauss.scouts.application.ports.persistence.EventRepository;
import name.alexkrauss.scouts.application.ports.persistence.GroupRepository;
import name.alexkrauss.scouts.domain.model.Event;
import name.alexkrauss.scouts.domain.model.Group;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of the EventManagementService interface.
 * Provides functionality for managing events and their associations with groups.
 */
@Service
@Transactional
public class EventManagementServiceImpl implements EventManagementService {

    private final EventRepository eventRepository;
    private final GroupRepository groupRepository;

    /**
     * Constructs a new EventManagementServiceImpl with the required repositories.
     *
     * @param eventRepository the repository for managing events
     * @param groupRepository the repository for managing groups
     */
    public EventManagementServiceImpl(EventRepository eventRepository, GroupRepository groupRepository) {
        this.eventRepository = eventRepository;
        this.groupRepository = groupRepository;
    }

    @Override
    public Event createEvent(Event event) {
        return eventRepository.create(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Event> getEvent(Long id) {
        return eventRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public Event updateEvent(Event event) {
        return eventRepository.update(event);
    }

    @Override
    public void deleteEvent(Long id) {
        eventRepository.delete(id);
    }

    @Override
    public Event assignGroupToEvent(Long eventId, Long groupId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " does not exist"));
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group with id " + groupId + " does not exist"));
        
        Set<Group> updatedGroups = new HashSet<>(event.getParticipatingGroups());
        updatedGroups.add(group);
        
        Event updatedEvent = event.toBuilder()
                .participatingGroups(updatedGroups)
                .build();
        
        return eventRepository.update(updatedEvent);
    }

    @Override
    public Event removeGroupFromEvent(Long eventId, Long groupId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Event with id " + eventId + " does not exist"));
        
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group with id " + groupId + " does not exist"));
        
        Set<Group> updatedGroups = new HashSet<>(event.getParticipatingGroups());
        updatedGroups.removeIf(g -> g.getId().equals(group.getId()));
        
        Event updatedEvent = event.toBuilder()
                .participatingGroups(updatedGroups)
                .build();
        
        return eventRepository.update(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> getEventsForGroup(Long groupId) {
        // Verify group exists
        groupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("Group with id " + groupId + " does not exist"));
        
        return eventRepository.findEventsByGroupId(groupId);
    }
}